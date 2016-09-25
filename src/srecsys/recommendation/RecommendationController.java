package srecsys.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import srecsys.model.Game;
import srecsys.scraper.UserFriendScraper;
import srecsys.scraper.UserGameScraper;

public class RecommendationController {
    
    private List<String> stopWords = new ArrayList<>();
    private List<String> allTerms = new ArrayList<>();
    public Map<String, List<String>> loadedTerms = new HashMap<>();
    private List<String> ownedGames = new ArrayList<>();
    private Map<String, Map<String, Double>> weightedTerm = new HashMap<>();
    private Map<String, Map<String, Double>> TFIDFTerm = new HashMap<>();
    
    public RecommendationController(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("data/stopwords.txt"));
            String currentLine;

            while((currentLine = br.readLine()) != null){
               stopWords.add(currentLine);
            }
        } catch (Exception ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveTermsToFile(String ifLocation, Games steamgames){
        
        try {
            Writer output = new BufferedWriter(new FileWriter(new File(ifLocation)));

            Map<String, Game> sortedGames = new TreeMap<>(steamgames.gameList);
            for (String s : sortedGames.keySet()) {
                Map<String, Double> invertedTerms = new TreeMap<>(sortedGames.get(s).game_terms);

                for (String key : invertedTerms.keySet()) {
                    output.write(key.toLowerCase() + ' ' + s + ' ' + (1+Math.log10(invertedTerms.get(key))) + "\n");
                }
            }

            output.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public void saveTermswithTFIDFToFile(String ifLocation){
        
        try {
            Writer output = new BufferedWriter(new FileWriter(new File(ifLocation)));
            
            for(Map.Entry<String, Map<String, Double>> wTerms : weightedTerm.entrySet()){
                for(Map.Entry<String, Double> wTerm : weightedTerm.get(wTerms.getKey()).entrySet()){
                    output.write(wTerms.getKey() + ' ' + wTerm.getKey() + ' ' + wTerm.getValue() + "\n");
                }
            }

            output.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }    
    
    public Map<String, Map<String, Double>> loadInvertedFile(String ifLocation){
        Map<String, Map<String, Double>> invertedTerms = new HashMap<>();
        Scanner input;
        String[] temp;
        Map<String, Double> invTermTemp;
        
        try {
            input = new Scanner(new FileReader(ifLocation));
            while (input.hasNextLine()){
                temp = input.nextLine().split(" ");
                if (invertedTerms.containsKey(temp[0])) {
                    invTermTemp = invertedTerms.get(temp[0]);
                    invTermTemp.put(String.valueOf(temp[1]), Double.valueOf(temp[2]));
                    invertedTerms.put(temp[0], invTermTemp);
                } else {
                    invTermTemp = new HashMap<>();
                    invTermTemp.put(String.valueOf(temp[1]), Double.valueOf(temp[2]));
                    invertedTerms.put(temp[0], invTermTemp);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
//        System.out.println(invertedTerms.toString());
        
        return invertedTerms;
    }
    
    public void printDocResult(SortedMap<Double, Set<String[]>> rankedDocuments){
        
        if (rankedDocuments!=null) {
            for (Map.Entry<Double, Set<String[]>> rankedDocument : rankedDocuments.entrySet()) {
                for (String[] docs : rankedDocument.getValue()) {
                    System.out.print(rankedDocument.getKey() + " - ");
                    System.out.print(docs[0]);
                    System.out.println(" (" + docs[4] + ")");
                }
            }
        }
        else{
            System.out.println("error!!");
        }
    }

    public void removeOwnedGames2(Games steamgames, UserGameScraper ugs){
        Iterator<String> iter = steamgames.gameList.keySet().iterator();   
        
        while(iter.hasNext()){
            String appID = iter.next();
            
            for(int i = 0; i < ugs.games.size(); i++){
                if(ugs.games.get(i).getAppID().equals(appID)){
                    iter.remove();
                }
            }
        }    
    }
    
    public void removeOwnedGames(Map<String, Double> result, UserGameScraper ugs){
        
        for(Iterator<Map.Entry<String,Double>> it = result.entrySet().iterator(); it.hasNext();){
            Map.Entry<String, Double> entry = it.next();
            for(int i = 0; i < ugs.games.size(); i++){            
                if(entry.getKey().equals(ugs.games.get(i).getAppID())){
                    it.remove();
                }
            }
        }
    }
    
    public Set<String> getOwnedGenres(UserGameScraper ugs){
        Set<String> genres = new HashSet<>();
        
        for(int i = 0; i < ugs.games.size(); i++){
            for(int j = 0; j < ugs.games.get(i).genres.size(); j++){
                if(!ugs.games.get(i).genres.get(j).isEmpty())
                    genres.add(ugs.games.get(i).genres.get(j));
            }
        }
        
        return genres;
    }
    
    public void removeNonGenreGames(Set<String> ownedGenres, Games steamgames){
        Iterator<String> iter = steamgames.gameList.keySet().iterator();
        List<String> genres;
        
        while(iter.hasNext()){
            String appID = iter.next();
            genres = steamgames.gameList.get(appID).genres;
            
            for(int i = 0; i < genres.size(); i++){
                if(!ownedGenres.contains(genres.get(i))){
                    iter.remove();
                }
            }
        }
    }    
    
    public Map<String, Double> computeIDF(){
        
        Map<String, Double> termIDF = new HashMap<>();
        loadTerms("data/terms.txt");
        
        for(int i = 0; i < allTerms.size(); i++){
            int counter = 0;
            for(Map.Entry<String, List<String>> tempTerms : loadedTerms.entrySet()){
                if(tempTerms.getValue().contains(allTerms.get(i))){
                    counter++;
                }
            }
            termIDF.put(allTerms.get(i), 1 + Math.log10(loadedTerms.size()/counter));
        }
        
//        System.out.println("IDFLIST " + termIDF.toString());
        return termIDF;
    }
    
    public void computeTFIDF(){
        Map<String, Double> IDFList = new HashMap<>();
        IDFList = computeIDF();
        double IDF = 0.0;
        double TF = 0.0;
        
        weightedTerm = loadInvertedFile("data/terms.txt");
        //            term        appID   logTF        
        for(Map.Entry<String, Map<String, Double>> termEntry : weightedTerm.entrySet()){
            //            appID  logTF*IDF
            for(Map.Entry<String, Double> termWeight : weightedTerm.get(termEntry.getKey()).entrySet()){
                IDF = IDFList.get(termEntry.getKey());
                TF = termWeight.getValue();
                termWeight.setValue(TF * IDF);
            }
        }
        
        saveTermswithTFIDFToFile("data/termscounted.txt");
        
//        System.out.println("TFIDF "+weightedTerm.toString());
    }
    
    public void loadTerms(String iflocation){
        
        BufferedReader br = null;
        boolean gotTerm;
        boolean gotAppID;
        Map<String, List<String>> appIDandTerms = new HashMap<>();
        List<String> terms = new ArrayList<>();
        String tempTerms = "";
        String tempAppID = "";
        
        try {

            String sCurrentLine;
            
            br = new BufferedReader(new FileReader("data/terms.txt"));

            String pastAppID = "";
            while ((sCurrentLine = br.readLine()) != null) {
//                System.out.println("sCurrentLine: " + sCurrentLine);
                
                tempTerms = "";
                tempAppID = "";
                gotTerm = false;
                gotAppID = false;
         
                for(int i = 0; i < sCurrentLine.length(); i++){
                    
                    if(sCurrentLine.charAt(i) != ' ' && !gotTerm && !gotAppID){
                        tempTerms += sCurrentLine.charAt(i);
                    }
                    
                    if(sCurrentLine.charAt(i) == ' ' && !gotTerm){
                        gotTerm = true;
                        terms.add(tempTerms);
                        if(!allTerms.contains(tempTerms)){
                            allTerms.add(tempTerms);
                        }
                    }
                    
                    if(sCurrentLine.charAt(i) != ' ' && gotTerm && !gotAppID){
                        tempAppID += sCurrentLine.charAt(i);
                    }
                    
                    if(sCurrentLine.charAt(i) == ' ' && !tempAppID.equals("")){
                        gotAppID = true;
                    }
                }
                
                if(!pastAppID.equals(tempAppID) && !pastAppID.equals("")){
                    terms.remove(terms.size()-1);
                    appIDandTerms.put(pastAppID, terms);
                    terms = new ArrayList<>();
                    terms.add(tempTerms);
                    pastAppID = tempAppID;
                }
                if(pastAppID.equals("")){
                    pastAppID = tempAppID;
                }
                
                
            }
            appIDandTerms.put(tempAppID, terms);
        } catch (IOException e) {
                e.printStackTrace();
        } finally {
                try {
                        if (br != null)br.close();
                } catch (IOException ex) {
                        ex.printStackTrace();
                }
        }
        
        loadedTerms = appIDandTerms;
    }
    
    public double computeCosineSimilarity(String appID1, String appID2){
        
        TFIDFTerm = loadInvertedFile("data/termscounted.txt");
        double dotProduct = dotProductTwoVectors(appID1, appID2);
        double length1 = countVectorLength(appID1);
        double length2 = countVectorLength(appID2);
        
        double cosine = dotProduct / (length1 * length2);
        
        if(length1 * length2 == 0.0)
            cosine = 0.0;
        
        return cosine;
    }
    
    public double dotProductTwoVectors (String appID1, String appID2){
        
        double tempSum = 0.0;
        
        for(Map.Entry <String, Map<String, Double>> entryGame: TFIDFTerm.entrySet()){
            Map<String, Double> tempValue = entryGame.getValue();
            if(tempValue.keySet().contains(appID1) && tempValue.keySet().contains(appID2)){
                double tempMultiplication = tempValue.get(appID1) * tempValue.get(appID2);
                tempSum += tempMultiplication;
            }
        }
        
        return tempSum;
    }
    
    public double countVectorLength (String appID){

        double tempLength = 0.0;        
        
        for(Map.Entry <String, Map<String, Double>> entryGame: TFIDFTerm.entrySet()){
            Map<String, Double> tempValue = entryGame.getValue();
            if(tempValue.keySet().contains(appID)){
                tempLength += tempValue.get(appID)*tempValue.get(appID);
            }else{
                tempLength += 0.0;
            }
        }
        
        return Math.sqrt(tempLength);
    }
       
    public double computeJaccardSimilarity (String appID1, String appID2){

        double irisan = computeIrisan(appID1, appID2);        
        double jumlahAppID1 = computejumlahTerm(appID1);        
        double jumlahAppID2 = computejumlahTerm(appID2);
        
        if(jumlahAppID1 + jumlahAppID2 - irisan != 0){
            return irisan / (jumlahAppID1 + jumlahAppID2 - irisan);
        }
        else{
            return -999.0;
        }
    }
    
    public double computeIrisan(String appID1, String appID2){
        
        List<String> tempTermsAppID1 = loadedTerms.get(appID1);
        List<String> tempTermsAppID2 = loadedTerms.get(appID2);
                
        double counterIrisan = 0.0;
        
        if(tempTermsAppID1!=null && tempTermsAppID2!=null){
            for (String s : tempTermsAppID1) {
                if(tempTermsAppID2.contains(s)){
                    counterIrisan = counterIrisan + 1.0;
                }
            }
        }
        
        return counterIrisan;
    }
    
    public double computejumlahTerm (String appID){
        
        List<String> tempTermsAppID = new ArrayList<>();
        
        if(loadedTerms.get(appID) != null){
            tempTermsAppID = loadedTerms.get(appID);
        }
        
        return tempTermsAppID.size();
    }

    public Map<String, Map<String, Double>> computeCosineScore(Games steamgames, UserGameScraper ugs){
        // Map<steam_appID, Map<owned_appID, similarity_score>>
        
        double tempScore;
        Map<String, Double> gameScore;
        Map<String, Map<String, Double>> gameScores = new HashMap<>();
                    
        for(Map.Entry<String, Game> steam : steamgames.gameList.entrySet()){
            gameScore = new HashMap<>();
            for(int i = 0; i < ugs.games.size(); i++){
                tempScore = computeCosineSimilarity(steam.getKey(), ugs.games.get(i).appID);
                gameScore.put(ugs.games.get(i).appID, tempScore);
            }
            gameScores.put(steam.getKey(), gameScore);
        }
        
        return gameScores;
    }
    
    public Map<String, Map<String, Double>> computeCosineScore2(Games steamgames, UserGameScraper ugs){
        // Map<owned_appID, Map<steam_appID, similarity_score>>
        
        double tempScore;
        Map<String, Double> gameScore;
        Map<String, Double> sortedGameScore = new HashMap<>();
        Map<String, Double> top50GameScore = new HashMap<>();
        Map<String, Double> sorted50GameScore = new HashMap<>();
        Map<String, Map<String, Double>> gameScores = new HashMap<>();
        
        for(int i = 0; i < ugs.games.size(); i++){
            gameScore = new HashMap<>();
            for(Map.Entry<String, Game> steam : steamgames.gameList.entrySet()){
                tempScore = computeCosineSimilarity(ugs.games.get(i).appID, steam.getKey());
                System.out.println("counting cosine score...");
                gameScore.put(steam.getKey(), tempScore);
            }
            sortedGameScore = sortMapByValues(gameScore);
            top50GameScore = getTopNValues(sortedGameScore, 12);
            sorted50GameScore = sortMapByValues(top50GameScore);
            
            gameScores.put(ugs.games.get(i).appID, sorted50GameScore);
        }
        
        System.out.println("skor cosine : " + gameScores.toString());
        
        return gameScores;
    } 
        
    public Map<String, Map<String, Double>> computeJaccardScore1(Games steamgames, UserGameScraper ugs){
        
        double tempScore;
        Map<String, Double> gameScore;
        Map<String, Map<String, Double>> gameScores = new HashMap<>();
                    
        for(Map.Entry<String, Game> steam : steamgames.gameList.entrySet()){
            gameScore = new HashMap<>();
            for(int i = 0; i < ugs.games.size(); i++){
                tempScore = computeJaccardSimilarity(steam.getKey(), ugs.games.get(i).appID);
                gameScore.put(ugs.games.get(i).appID, tempScore);
            }
            gameScores.put(steam.getKey(), gameScore);
        }
        
        return gameScores;
    }
    
    public Map<String, Map<String, Double>> computeJaccardScore(Games steamgames, UserGameScraper ugs){
        // Map<owned_appID, Map<steam_appID, similarity_score>>
        
        double tempScore;
        Map<String, Double> gameScore;
        Map<String, Double> sortedGameScore = new HashMap<>();
        Map<String, Double> top12GameScore = new HashMap<>();
        Map<String, Double> sorted12GameScore = new HashMap<>();
        Map<String, Map<String, Double>> gameScores = new HashMap<>();
        
        for(int i = 0; i < ugs.games.size(); i++){
            gameScore = new HashMap<>();
            for(Map.Entry<String, Game> steam : steamgames.gameList.entrySet()){
                tempScore = computeJaccardSimilarity(ugs.games.get(i).appID, steam.getKey());
//                System.out.println("counting jaccard score...");
                gameScore.put(steam.getKey(), tempScore);
            }
            sortedGameScore = sortMapByValues(gameScore);
            top12GameScore = getTopNValues(sortedGameScore, 12);
            sorted12GameScore = sortMapByValues(top12GameScore);
            
            gameScores.put(ugs.games.get(i).appID, sortedGameScore);
        }
        
//        System.out.println("skor jaccard : " + gameScores.toString());
        
        return gameScores;
    }
    
     public Map<String, Map<String, Double>> computeJaccardAppearance(Games steamgames, UserGameScraper ugs){
        // Map<owned_appID, Map<steam_appID, similarity_score>>
        
        double tempScore;
        Map<String, Double> gameScore;
        Map<String, Double> sortedGameScore = new HashMap<>();
        Map<String, Double> top12GameScore = new HashMap<>();
        Map<String, Double> sorted12GameScore = new HashMap<>();
        Map<String, Map<String, Double>> gameScores = new HashMap<>();
        
        for(int i = 0; i < ugs.games.size(); i++){
            gameScore = new HashMap<>();
            for(Map.Entry<String, Game> steam : steamgames.gameList.entrySet()){
                tempScore = computeJaccardSimilarity(ugs.games.get(i).appID, steam.getKey());
//                System.out.println("counting jaccard score...");
                gameScore.put(steam.getKey(), tempScore);
            }
            sortedGameScore = sortMapByValues(gameScore);
            top12GameScore = getTopNValues(sortedGameScore, 12);
            sorted12GameScore = sortMapByValues(top12GameScore);
            
            gameScores.put(ugs.games.get(i).appID, sorted12GameScore);
        }
        
//        System.out.println("skor jaccard : " + gameScores.toString());
        
        return gameScores;
    }  
    
    public Map<String, Double> computeFinalScore(Map<String, Map<String, Double>> gameScores){
        
        Map<String, Double> finalScore = new HashMap<>();
        double score = 0.0;
        
        for(Map.Entry <String, Map<String, Double>> gameScore : gameScores.entrySet()){
            for(Map.Entry <String, Double> tempScore : gameScore.getValue().entrySet()){
                score += tempScore.getValue();
            }
            finalScore.put(gameScore.getKey(), score);
        }
        
        return finalScore;
    }
    
    public Map<String, Double> computeFinalScore2(Map<String, Map<String, Double>> gameScores){
        // mau jadi Map<appID, skor>
        // kalo ada suatu game yang muncul di beberapa, kasih prioritas
        
        Map<String, Double> finalScore = new HashMap<>();
        double score = 0.0;
        
        for(Map.Entry <String, Map<String, Double>> gameScore : gameScores.entrySet()){
            for(Map.Entry <String, Double> tempScore : gameScore.getValue().entrySet()){
                score += tempScore.getValue();
            }
            finalScore.put(gameScore.getKey(), score);
        }
        
        return finalScore;
    }    

    public List<String> loadAllGames(Games steamgames){
        
        List<String> gamelist = new ArrayList<>();
        
        for(Map.Entry<String, Game> glist : steamgames.gameList.entrySet()){
            gamelist.add(glist.getKey());
        }
        
        return gamelist;
    }
    
    public Map<String, Set<String>> getFriendGames(UserFriendScraper ufs, UserGameScraper ugs, String steam64id) throws IOException, Exception{
        
        Map<String, Set<String>> friendGames = new HashMap<>();
        List<String> friendList = new ArrayList<>();
        Set<String> gameList = new HashSet<>();
        
        ufs.scrape(steam64id);
        friendList = ufs.friendlist;
        
        for(int i = 0; i < friendList.size(); i++){
            ugs.scrapeAppIDOnly(friendList.get(i));
            gameList = new HashSet<>();
            for(int j = 0; j < ugs.gamesAppID.size(); j++){
                gameList.add(ugs.gamesAppID.get(j).appID);
            }
            friendGames.put(friendList.get(i), gameList);
        }
                
        return friendGames;
    }
        
    public Map<String, Integer> getCommonGames(List<String> gamelist, UserFriendScraper ufs, UserGameScraper ugs, String steam64id) throws Exception{
        
        Map<String, Integer> gameCount = new HashMap<>();
        Map<String, Set<String>> friendGames = new HashMap<>();
        int counter;
        
        friendGames = getFriendGames(ufs, ugs, steam64id);
                        
        for(int i = 0; i < gamelist.size(); i++){
            counter = 0;
            for(Map.Entry<String, Set<String>> tempGames : friendGames.entrySet()){
//                System.out.println("tempgames dari si " + tempGames.getKey() + " adalahh " + tempGames.getValue().toString());
//                System.out.println("gamelist.get " + gamelist.get(i));
                
                if(tempGames.getValue().contains(gamelist.get(i))){
                    counter++;
//                    System.out.println("counter " + counter + " gamelist.geti " + gamelist.get(i));
                }
                
            }
//            System.out.println("counter sebelum dimasukin " + counter);
            gameCount.put(gamelist.get(i), counter);
        }
        
        return gameCount;
    }
    
    public Map<String, Double> getTopNValues(Map<String, Double> map, int limit){
        
        Map<String, Double> newmap = new HashMap<>();
        int counter = 0;
                
        for(Map.Entry<String, Double> mapcontent : map.entrySet()){
            newmap.put(mapcontent.getKey(), mapcontent.getValue());
            counter++;
            if(counter >= limit){
                break;
            }
        }
        
        return newmap;
    }
    
    public Map<String, Double> recommendbyScore(Map<String, Map<String, Double>> scoremap, UserGameScraper ugs){
        // Map<String, Map<String, Double>> mau jadi Map<String, Double>
        // Map<owned_app, Map<steam_app, similarity>> mau jadi Map<steam_app, similarity>
        
        Map<String, Double> finalRecommendation = new HashMap<>();
        
        for(Map.Entry<String, Map<String, Double>> scores : scoremap.entrySet()){
            for(Map.Entry<String, Double> score : scoremap.get(scores.getKey()).entrySet()){
                
                if(scores.getKey().equals(score.getKey())){
                    // untuk menghilangkan perbandingan similarity dengan appid sendiri
                    score.setValue(-999.0);
                }
                
                if(finalRecommendation.keySet().contains(score.getKey())){
                    // compare
                    if(finalRecommendation.get(score.getKey()) < score.getValue()){
                        finalRecommendation.remove(score.getKey());
                        finalRecommendation.put(score.getKey(), score.getValue());
                    }
                }
                else{
                    // masukin
                    finalRecommendation.put(score.getKey(), score.getValue());
                }
            }
        }
        
        //ilangin game yang udah dipunya
        
        for(Map.Entry<String, Double> rec : finalRecommendation.entrySet()){
            for(int i = 0; i < ugs.games.size(); i++){
                if(rec.getKey().contains(ugs.games.get(i).appID)){
                    rec.setValue(-999.0);
                }
            }
        }
                
        return finalRecommendation;
    }
    
    public Map<String, Double> recommendbyScorewithFriend(Map<String, Double> recommendation, Map<String, Double> bonusScore){
        
        Map<String, Double> sortedFinalRecommendation = new HashMap<>();
        Map<String, Double> top12FinalRecommendation = new HashMap<>();
        Map<String, Double> sorted12FinalRecommendation = new HashMap<>();
        
        for(Map.Entry<String, Double> rec : recommendation.entrySet()){
            if(bonusScore.keySet().contains(rec.getKey())){
                recommendation.replace(rec.getKey(), rec.getValue() + bonusScore.get(rec.getKey()));
            }
        }
        
        sortedFinalRecommendation = sortMapByValues(recommendation);
        top12FinalRecommendation = getTopNValues(sortedFinalRecommendation, 12);
        sorted12FinalRecommendation = sortMapByValues(top12FinalRecommendation);
        
        return sortedFinalRecommendation;
    }
    
    public Map<String, Double> bonusScoreFromFriends(Map<String, Integer> commonGames, UserFriendScraper ufs){
        
        Map<String, Double> bonusScore = new HashMap<>();
        int friendsize = ufs.friendlist.size();
        double konstanta = 0.3;
        
        for(Map.Entry<String, Integer> commGames : commonGames.entrySet()){
            bonusScore.put(commGames.getKey(), konstanta * (commGames.getValue() * 1D)/(friendsize * 1D));            
        }
        
//        System.out.println("bonus: " + bonusScore.toString());
        
        return bonusScore;
    }
    
    public Map<String, Double> recommendbyAppearance(Games steamgames, Map<String, Map<String, Double>> scoremap, UserGameScraper ugs){
        // punya Map<String, Map<String, Double>> dari computeJaccardScore2
        //           owned_ID   steam_ID sim
        
        List<String> steamgamelist = new ArrayList<>(steamgames.gameList.keySet());
        Map<String, Double> finalRecommendation = new HashMap<>();
        int counter;
        
        for(int i = 0; i < steamgamelist.size(); i++){
            counter = 0;
            for(Map.Entry<String, Map<String, Double>> tempscore : scoremap.entrySet()){
                if(tempscore.getValue().keySet().contains(steamgamelist.get(i))){
                    counter++;
                }
            }
            
            finalRecommendation.put(steamgamelist.get(i), counter*1D / scoremap.size()*1D);
        }
        
                //ilangin game yang udah dipunya
        
        for(Map.Entry<String, Double> rec : finalRecommendation.entrySet()){
            for(int i = 0; i < ugs.games.size(); i++){
                if(rec.getKey().contains(ugs.games.get(i).appID)){
                    rec.setValue(-999.0);
                }
            }
        }
        
        return finalRecommendation;
        
    }
            
    public <K, V extends Comparable<? super V>> Map<K, V> sortMapByValues(Map<K, V> map) {
    return map.entrySet()
              .stream()
              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
              .collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue(), 
                (e1, e2) -> e1, 
                LinkedHashMap::new
              ));
    }
    
    public int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    
    public List<String> removeRandomGame(int jumlah, UserGameScraper ugs){
        List<String> removedGame = new ArrayList<>();
        
        int index;
        // remove 5 game pemain
        for(int i = 0; i < jumlah; i++){
            index = randInt(0, ugs.games.size()-1);
            System.out.println("Removing game : " + ugs.games.get(index).appID);
            removedGame.add(ugs.games.get(index).appID);
            ugs.games.remove(index);
        }
        
        System.out.println("Removed games : " + removedGame.toString());
        return removedGame;
    }
    
    public void saveRemovedRandomGame (String saveLocation, List<String> removedGame){
        try {
            PrintWriter out = new PrintWriter(saveLocation);
            for(String s: removedGame){
                out.println(s);
            }
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RecommendationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeGamebyAppID(String appID, UserGameScraper ugs){
        
        for(int i = 0; i < ugs.games.size(); i++){
            if(ugs.games.get(i).appID.equals(appID)){
                System.out.println("Removing game : " + ugs.games.get(i).appID);
                ugs.games.remove(i);
            }
        }
        
        System.out.println("Games right now : " + ugs.games.size());
    }

    public Map<String, Double> sortandCutMap(Map<String, Double> finalRecommendation, int limit){
        
        Map<String, Double> sortedFinalRecommendation = new HashMap<>();
        Map<String, Double> topNFinalRecommendation = new HashMap<>();
        Map<String, Double> sortedNFinalRecommendation = new HashMap<>();
        
        sortedFinalRecommendation = sortMapByValues(finalRecommendation);
        topNFinalRecommendation = getTopNValues(sortedFinalRecommendation, limit);
        sortedNFinalRecommendation = sortMapByValues(topNFinalRecommendation);
        
        return sortedNFinalRecommendation;
    }
}

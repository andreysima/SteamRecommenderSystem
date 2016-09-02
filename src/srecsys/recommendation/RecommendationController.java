package srecsys.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import srecsys.model.Game;
import srecsys.scraper.UserFriendScraper;
import srecsys.scraper.UserGameScraper;
import srecsys.stemmer.PorterStemmer;

public class RecommendationController {
    
    private List<String> stopWords = new ArrayList<>();
    private List<String> allTerms = new ArrayList<>();
    public Map<String, List<String>> loadedTerms = new HashMap<>();
    private List<String> ownedGames = new ArrayList<>();
    private Map<String, Map<String, Double>> weightedTerm = new HashMap<>();
    
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
//        System.out.println("LoadedTermsDiComputeIDF: " + loadedTerms.keySet().toString());
        Map<String, Double> termIDF = new HashMap<>();
        
        for(int i = 0; i < allTerms.size(); i++){
            int counter = 0;
            for(Map.Entry<String, List<String>> tempTerms : loadedTerms.entrySet()){
                if(tempTerms.getValue().contains(allTerms.get(i))){
                    counter++;
                }
            }
            termIDF.put(allTerms.get(i), 1 + Math.log10(loadedTerms.size()/counter));
//            System.out.println("i naik: " + i);
        }
        
//        System.out.println("IDFLIST " + termIDF.toString());
        return termIDF;
    }
    
    public void computeTFIDF(){
        Map<String, Double> IDFList = new HashMap<>();
        IDFList = computeIDF();
        double IDF = 0.0;
        double TF = 0.0;
        int counter = 0;
        
        weightedTerm = loadInvertedFile("data/terms.txt");
        
        for(Map.Entry<String, Map<String, Double>> termEntry : weightedTerm.entrySet()){
            for(Map.Entry<String, Double> termWeight : weightedTerm.get(termEntry.getKey()).entrySet()){
                IDF = IDFList.get(termEntry.getKey());
                TF = termWeight.getValue();
                termWeight.setValue(TF * IDF);
                counter++;
//                System.out.println("baaaaa " + counter);
            }
        }
        
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
        
        for(Map.Entry <String, Map<String, Double>> entryGame: weightedTerm.entrySet()){
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
        
        for(Map.Entry <String, Map<String, Double>> entryGame: weightedTerm.entrySet()){
            Map<String, Double> tempValue = entryGame.getValue();
            if(tempValue.keySet().contains(appID)){
                tempLength += tempValue.get(appID)*tempValue.get(appID);
            }else{
                tempLength += 0.0;
            }
        }
        
        return Math.sqrt(tempLength);
    }
        
    public double computeIrisan(String appID1, String appID2){
     
        
        List<String> tempTermsAppID1 = loadedTerms.get(appID1);
        List<String> tempTermsAppID2 = loadedTerms.get(appID2);
                
        double counterIrisan = 0.0;
        
        for (String s : tempTermsAppID1) {
            if(tempTermsAppID2.contains(s)){
                counterIrisan = counterIrisan + 1.0;
            }
        }
        
        return counterIrisan;
    }
    
    public double computejumlahTerm (String appID){
        List<String> tempTermsAppID = new ArrayList<>();
        tempTermsAppID = loadedTerms.get(appID);
        
        return tempTermsAppID.size();
    }
    
    public double computeJaccardSimilarity (String appID1, String appID2){
        System.out.println("APPID1: " + appID1 + ", APPID2: " + appID2);
        double irisan = computeIrisan(appID1, appID2);
        System.out.println("jumlah irisan: " + irisan);
        
        double jumlahAppID1 = computejumlahTerm(appID1);
        
        double jumlahAppID2 = computejumlahTerm(appID2);
        
        return irisan / (jumlahAppID1 + jumlahAppID2 - irisan);
    }

    public Map<String, Map<String, Double>> computeCosineScore(Games steamgames, UserGameScraper ugs){
        
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
    
    public Map<String, Map<String, Double>> computeJaccardScore(Games steamgames, UserGameScraper ugs){
        
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
    
    public Map<String, Double> computeFinalScore(Map<String, Map<String,Double>> gameScores){
        
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
    
    public Map<String, List<String>> getFriendGames(UserFriendScraper ufs, UserGameScraper ugs, String steam64id) throws IOException, Exception{
        
        Map<String, List<String>> friendGames = new HashMap<>();
        List<String> friendList = new ArrayList<>();
        List<String> gameList = new ArrayList<>();
        
        ufs.scrape(steam64id);
        friendList = ufs.friendlist;
        
        for(int i = 0; i < friendList.size(); i++){
            ugs.scrape(friendList.get(i));
            for(int j = 0; j < ugs.games.size(); j++){
                gameList.add(ugs.games.get(j).appID);
            }
            friendGames.put(steam64id, gameList);
        }
        
        return friendGames;
    }
    
    public Map<Game, Integer> getCommonGames(Map<String, List<Game>> friendGames){
        
        Map<Game, Integer> commonGames = new HashMap<>();
        int tempSum = 0;
        
        for(Map.Entry<String, List<Game>> tempGames : friendGames.entrySet()){
            for(int i = 0; i < tempGames.getValue().size(); i++){
                if(!commonGames.containsKey(tempGames.getValue().get(i))){
                    for(int j = 0; j < friendGames.size(); j++){
                        if(tempGames.getValue().contains(tempGames.getValue().get(i))){
                            tempSum += 1;
                        }
                    }
                    commonGames.put(tempGames.getValue().get(i), tempSum);
                }
            }
        }
        
        System.out.println(commonGames.toString());
        
        return commonGames;
    }
    
    public Map<String, Integer> getCommGames(List<String> gamelist, UserFriendScraper ufs, UserGameScraper ugs, String steam64id) throws Exception{
        
        Map<String, Integer> gameCount = new HashMap<>();
        Map<String, List<String>> friendGames = new HashMap<>();
        int counter = 0;
        
        friendGames = getFriendGames(ufs, ugs, steam64id);
                
        for(int i = 0; i < gamelist.size(); i++){
            for(Map.Entry<String, List<String>> tempGames : friendGames.entrySet()){
                if(tempGames.getValue().contains(gamelist.get(i))){
                    counter++;
                }
            }
            gameCount.put(gamelist.get(i), counter);
        }
        
        return gameCount;
    }
    
    public <K, V extends Comparable< ? super V>> Map<K, V>
    sortMapByValues(final Map <K, V> mapToSort)
    {
        List<Map.Entry<K, V>> entries =
            new ArrayList<Map.Entry<K, V>>(mapToSort.size());  

        entries.addAll(mapToSort.entrySet());

        Collections.sort(entries,
                         new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(
                   final Map.Entry<K, V> entry1,
                   final Map.Entry<K, V> entry2)
            {
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });      

        Map<K, V> sortedMap = new LinkedHashMap<K, V>();      

        for (Map.Entry<K, V> entry : entries)
        {
            sortedMap.put(entry.getKey(), entry.getValue());

        }      

        return sortedMap;

    }
    
    public List<String> removeStopWords(String text) throws FileNotFoundException, IOException{
        List<String> oldtexts = new LinkedList<>(Arrays.asList(cleanString(text).split("\\s+")));
        List<String> texts = stemAll(oldtexts);
        
        
        for(int i = texts.size()-1; i >= 0; i--){
            for(int j = 0; j < stopWords.size(); j++){
                if(stopWords.get(j).contains(texts.get(i))){
                    texts.set(i, "");
                }
            }
        }
        
        return texts;
    }
    
    public String cleanString(String text){
        String newtext1 = text.replaceAll("<\\/?[^>]+>", "").toLowerCase();
        String newtext2 = newtext1.replaceAll("[^\\w\\s]+|\\w*\\d\\w*", "").toLowerCase();
        
        return newtext2;
    }
     
    public String stem(String word) {
        PorterStemmer stem = new PorterStemmer();
        stem.add(word.toCharArray(), word.length());
        stem.stem();

        return stem.toString();
    }
    
    public List<String> stemAll(List<String> oldList){
        List<String> newList = new ArrayList<>();
        
        for(int i = 0; i < oldList.size(); i++){
            newList.add(stem(oldList.get(i)));
        }
        
        return newList;
        
    }
}

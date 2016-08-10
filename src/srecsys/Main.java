package srecsys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import srecsys.model.Game;
import srecsys.recommendation.Games;
import srecsys.scraper.UserGameScraper;

public class Main {
    public ArrayList<ArrayList<String>> termDocumentMatrix = new ArrayList<>();
    private static UserGameScraper ugs = new UserGameScraper();
    private static Games steamgames = new Games();
    private static Map<String, Map<String, Double>> invertedTerms;
    public static SortedMap<Double, Set<String[]>> rankedDocuments;
    
    
    public static void main(String[] args) throws Exception{
        
        //mengambil game yang dimiliki user
        ugs.scrape("76561198115471724");
        System.out.println(ugs.games.get(0).appID + ugs.games.get(0).getName());
        System.out.println(ugs.games.get(0).game_terms.toString());
        
        // ini punya user
//        ugs.games;
        
        //mengambil game yang ada di dalam Steam
        steamgames.JSONReader();
        
        // ini punya steam semua
//        steamgames.gameList;

//        for(String i : steamgames.gameList.keySet()){
//            for(int j = 0; j < ugs.games.size(); j++){
//                if(ugs.games.get(j).getAppID().equals(i)){
//                    steamgames.gameList.remove(i);
//                }
//            }
//        }
        
        saveTermsToFile("data/terms.txt");
        loadInvertedFile("data/terms.txt");
        System.out.println(invertedTerms.toString());
        computeSimilarity();
        printDocResult();
//        double sum = 0.0;
//        for(String usergame : ugs.games.get(0).game_terms.keySet()){
//      1      if(steamgames.gameList.get(0).game_terms.get(usergame) != null){
//                sum += steamgames.gameList.get(0).game_terms.get(usergame) * ugs.games.get(0).game_terms.get(usergame);
//            }
//        }
        
//        for(int i = 0; i < steamgames.gameList.size(); i++){
//            System.out.println(steamgames.gameList.get(i).game_terms.toString());
//        } 

    }
    
    public static void saveTermsToFile(String ifLocation) {
        try {
            Writer output = new BufferedWriter(new FileWriter(new File(ifLocation)));

            Map<String, Game> sortedGames = new TreeMap<>(steamgames.gameList);
            for (String s : sortedGames.keySet()) {
                Map<String, Double> invertedTerms = new TreeMap<>(sortedGames.get(s).game_terms);

                for (String key : invertedTerms.keySet()) {
                    output.write(key.toLowerCase() + '\t' + s + '\t' + invertedTerms.get(key) + "\n");
                }
            }

            output.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public static void loadInvertedFile(String ifLocation) {
        Scanner input;
        String[] temp;
        invertedTerms = new HashMap<>();
        Map<String, Double> invTermTemp;
        try {
            input = new Scanner(new FileReader(ifLocation));
            while (input.hasNextLine()){
                temp = input.nextLine().split("\t");
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
    }
    
    public static void computeSimilarity() {
        double queryWeight;
        double totalWeight = 0;
        String[] temp;
        Map<String, Double> invTemp;
        Set<String[]> tempDoc;

        rankedDocuments = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<String, Game> game : steamgames.gameList.entrySet()) {
            for (Map.Entry<String, Double> weightedTerm : ugs.games.get(0).game_terms.entrySet()) {
                if (invertedTerms.containsKey(weightedTerm.getKey())) {
                    invTemp = invertedTerms.get(weightedTerm.getKey());
                    if (invTemp.containsKey(game.getKey())) {
                        queryWeight = weightedTerm.getValue();
                        
                        totalWeight += queryWeight*invTemp.get(game.getKey());
                    }
                }
            }

            temp = new String[6];
            temp[0] = String.valueOf(game.getValue().getName());
            // temp[2] for recall
            // temp[3] for precision
            temp[4] = String.valueOf(game.getKey());
            temp[5] = String.valueOf(totalWeight);
            
            System.out.println(Arrays.toString(temp));
            if (Double.compare(totalWeight, 0.0) > 0) {
                if (!rankedDocuments.containsKey(totalWeight)) {
                    tempDoc = new HashSet<>();
                    tempDoc.add(temp);
                    rankedDocuments.put(totalWeight, tempDoc);
                } else {
                    tempDoc = rankedDocuments.get(totalWeight);
                    tempDoc.add(temp);
                    rankedDocuments.put(totalWeight, tempDoc);
                }
            }
            totalWeight = 0;
        }
    }
    
    public static void printDocResult() {
        if (rankedDocuments!=null) {
            for (Map.Entry<Double, Set<String[]>> rankedDocument : rankedDocuments.entrySet()) {
                for (String[] docs : rankedDocument.getValue()) {
                    System.out.print(rankedDocument.getKey() + " - ");
                    System.out.print(docs[0]);
                    System.out.println(" (" + docs[4] + ")");
                }
            }
        }
    }
}

package srecsys.recommendation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import srecsys.model.Game;
import srecsys.scraper.UserGameScraper;

public class RecommendationController {
    
    public void saveTermsToFile(String ifLocation, Games steamgames){
        
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
    
    public void loadInvertedFile(String ifLocation, Map<String, Map<String, Double>> invertedTerms){
        
        Scanner input;
        String[] temp;
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
    
    public void computeSimilarity(SortedMap<Double, Set<String[]>> rankedDocuments,
            Games steamgames, Map<String, Map<String, Double>> invertedTerms,
            UserGameScraper ugs){
        
        double queryWeight;
        double totalWeight = 0;
        String[] temp;
        Map<String, Double> invTemp;
        Set<String[]> tempDoc;
          
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

    public void removeOwnedGames(Games steamgames, UserGameScraper ugs){
        Iterator<String> iter = steamgames.gameList.keySet().iterator();   
        
        while(iter.hasNext()){
            String str = iter.next();
            
            for(int j = 0; j < ugs.games.size(); j++){
                if(ugs.games.get(j).getAppID().equals(str)){
                    iter.remove();
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
}

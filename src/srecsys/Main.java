package srecsys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import srecsys.recommendation.Games;
import srecsys.recommendation.RecommendationController;
import srecsys.scraper.UserFriendScraper;
import srecsys.scraper.UserGameScraper;
import srecsys.scraper.UserScraper;

public class Main {
    public ArrayList<ArrayList<String>> termDocumentMatrix = new ArrayList<>();
    private static UserFriendScraper ufs;
    private static UserGameScraper ugs;
    private static UserScraper us;
    private static Games steamgames;
    private static Map<String, Map<String, Double>> invertedTerms;
    private static SortedMap<Double, Set<String[]>> rankedDocuments;
    private static Set<String> ownedGenre;
    private static Map<String, Double> rankedGames;
    private static Map<String, Double> sortedGames;
    private static Map<String, Map<String,Double>> gameResults;
    
    
    public static void main(String[] args) throws Exception{
        
        RecommendationController RC = new RecommendationController();
        ufs = new UserFriendScraper();
        ugs = new UserGameScraper();
        us = new UserScraper();
        
        steamgames = new Games();
        invertedTerms = new HashMap<>();
        rankedDocuments = new TreeMap<>(Collections.reverseOrder());
        ownedGenre = new HashSet<>();
        gameResults = new HashMap<>();
        
        String steam64id = "76561198115471724";
        
        //mengambil game, friendlist, dan info yang dimiliki user
        ufs.scrape(steam64id);
        ugs.scrape(steam64id);
        us.scrape(steam64id);
                
        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
//        RC.removeOwnedGames(steamgames, ugs);
        ownedGenre = RC.getOwnedGenres(ugs);
//        RC.removeNonGenreGames(ownedGenre, steamgames);
        
        System.out.println("Owned game genre of "+us.personaname);
        System.out.println(ownedGenre.toString());
        System.out.println("");
        
        RC.saveTermsToFile("data/terms.txt", steamgames);
        invertedTerms = RC.loadInvertedFile("data/terms.txt");
        RC.loadTerms("data/terms.txt");
//        
        System.out.println("loadedTerms: " + RC.loadedTerms.keySet());
            
//        RC.computeIDF();
//        RC.computeTFIDF();
        
         //test Jaccard Similarity
        System.out.println("Jaccard: " + RC.countJaccardSimilarity("570", "570"));
//        
//        gameResults = RC.computeScore(steamgames, ugs);
//        rankedGames = RC.computeFinalScore(gameResults);
//        RC.removeOwnedGames(rankedGames, ugs);
//        sortedGames = RC.sortMapByValues(rankedGames);
//        
//        System.out.println("hasilnya adalah");
//        System.out.println(sortedGames.toString());

//        RC.getCommonGames(RC.getFriendGames(ufs, ugs, steam64id));
        
//        System.out.println(gameResults.toString());
//        rankedGames = RC.computeScore(steamgames, ugs);
//        sortedGames = RC.sortMapByValues(rankedGames);
//        
//        System.out.println(sortedGames.toString());
        
//        System.out.println(RC.computeScore(steamgames, ugs).toString());
        
//        System.out.println(invertedTerms.toString());
//        RC.computeSimilarity(rankedDocuments, steamgames, invertedTerms, ugs);
//        rankedGames = RC.computeSimilarity(ugs, invertedTerms);
//        sortedGames = RC.sortMapByValues(rankedGames);
        
//        System.out.println(RC.createQueryFromGames(ugs).toString());
//        System.out.println(sortedGames.toString());
        
        
//        RC.printDocResult(rankedDocuments);
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
}

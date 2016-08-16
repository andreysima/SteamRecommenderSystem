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
    
    public static void main(String[] args) throws Exception{
        
        RecommendationController RC = new RecommendationController();
        ufs = new UserFriendScraper();
        ugs = new UserGameScraper();
        us = new UserScraper();
        
        steamgames = new Games();
        invertedTerms = new HashMap<>();
        rankedDocuments = new TreeMap<>(Collections.reverseOrder());
        ownedGenre = new HashSet<>();
        
        String steam64id = "76561198115471724";
        
        //mengambil game, friendlist, dan info yang dimiliki user
        ufs.scrape(steam64id);
        ugs.scrape(steam64id);
        us.scrape(steam64id);
        
        System.out.println(ugs.games.get(0).appID + " " + ugs.games.get(0).getName());
        System.out.println(ugs.games.get(0).game_terms.toString());
                
        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
        RC.removeOwnedGames(steamgames, ugs);
        ownedGenre = RC.getOwnedGenres(ugs);
        
        System.out.println("Owned game genre of "+us.personaname);
        System.out.println(ownedGenre.toString());
        System.out.println("");
        
        RC.saveTermsToFile("data/terms.txt", steamgames);
        RC.loadInvertedFile("data/terms.txt", invertedTerms);
//        System.out.println(invertedTerms.toString());
        RC.computeSimilarity(rankedDocuments, steamgames, invertedTerms, ugs);
        RC.printDocResult(rankedDocuments);
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

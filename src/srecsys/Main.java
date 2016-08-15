package srecsys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import srecsys.recommendation.Games;
import srecsys.recommendation.RecommendationController;
import srecsys.scraper.UserGameScraper;

public class Main {
    public ArrayList<ArrayList<String>> termDocumentMatrix = new ArrayList<>();
    private static UserGameScraper ugs;
    private static Games steamgames;
    private static Map<String, Map<String, Double>> invertedTerms;
    private static SortedMap<Double, Set<String[]>> rankedDocuments;
    
    public static void main(String[] args) throws Exception{
        
        RecommendationController RC = new RecommendationController();
        steamgames = new Games();
        ugs = new UserGameScraper();
        invertedTerms = new HashMap<>();
        rankedDocuments = new TreeMap<>(Collections.reverseOrder());
        
        //mengambil game yang dimiliki user
        ugs.scrape("76561198115471724");
        System.out.println(ugs.games.get(0).appID + " " + ugs.games.get(0).getName());
        System.out.println(ugs.games.get(0).game_terms.toString());
                
        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
        RC.removeOwnedGames(steamgames, ugs);
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

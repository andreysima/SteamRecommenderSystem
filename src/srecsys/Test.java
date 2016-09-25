package srecsys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import srecsys.recommendation.Games;
import srecsys.recommendation.RecommendationController;
import srecsys.scraper.UserFriendScraper;
import srecsys.scraper.UserGameScraper;
import srecsys.scraper.UserScraper;

/**
 *
 * @author Andrey
 */
public class Test {

    private static final String steam64id = "76561198115471724";

    public Test() {
    }
        
    public void doTesting() throws IOException, Exception{
            
        UserFriendScraper ufs = new UserFriendScraper();
        UserGameScraper ugs = new UserGameScraper();
        UserScraper us = new UserScraper();
        RecommendationController RC = new RecommendationController();
        Games steamgames = new Games();
        
        Set<String> ownedGenre = new HashSet<>();
        Map<String, Double> rankedGamesAppearance = new HashMap<>();
        Map<String, Double> rankedGamesScore = new HashMap<>();
        Map<String, Double> rankedGameswithFriend = new HashMap<>();
        Map<String, Double> bonusScoreFromFriend = new HashMap<>();
        Map<String, Integer> commonGamesinFriend = new HashMap<>();
        List<String> gameList = new ArrayList<>();
        
        Map<String, Map<String,Double>> gameResultsScore = new HashMap<>();
        Map<String, Map<String,Double>> gameResultsAppearance = new HashMap<>();
        
        //mengambil game, friendlist, dan info yang dimiliki user
        
        ufs.scrape(steam64id);
        ugs.scrape(steam64id);
        us.scrape(steam64id);
        
        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
        ownedGenre = RC.getOwnedGenres(ugs);                
        
        System.out.println("Owned game genre of "+us.personaname);
        System.out.println(ownedGenre.toString());
        System.out.println("");
        
        RC.loadTerms("data/terms.txt");

////////////////////////////////////////////////////////////////////////////////        
        gameResultsScore = RC.computeJaccardScore(steamgames, ugs);
        rankedGamesScore = RC.recommendbyScore(gameResultsScore, ugs);
        System.out.println("Metode Skor Tanpa Friend: " + RC.sortandCutMap(rankedGamesScore, 12).toString());
//        System.out.println("Metode Skor Tanpa Friend: " + RC.sortMapByValues(rankedGamesScore).toString());
        System.out.println("----------------------------------------------------");
////////////////////////////////////////////////////////////////////////////////        
        gameList = RC.loadAllGames(steamgames);
        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGamesScore, bonusScoreFromFriend);
        System.out.println("Metode Skor Dengan Friend: " + RC.sortandCutMap(rankedGameswithFriend, 12).toString());
//        System.out.println("Metode Skor Dengan Friend: " + rankedGameswithFriend.toString());
        System.out.println("----------------------------------------------------");
////////////////////////////////////////////////////////////////////////////////        
////////////////////////////////////////////////////////////////////////////////        
        gameResultsAppearance = RC.computeJaccardAppearance(steamgames, ugs);
        rankedGamesAppearance = RC.recommendbyAppearance(steamgames, gameResultsAppearance, ugs);
        System.out.println("Metode Appearance Tanpa Friend: " + RC.sortandCutMap(rankedGamesAppearance, 12).toString());
//        System.out.println("Metode Appearance Tanpa Friend: " + RC.sortMapByValues(rankedGamesAppearance).toString());
        System.out.println("----------------------------------------------------");
////////////////////////////////////////////////////////////////////////////////        
        gameList = RC.loadAllGames(steamgames);
        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGamesAppearance, bonusScoreFromFriend);
        System.out.println("Metode Appearance Dengan Friend: " + RC.sortandCutMap(rankedGameswithFriend, 12).toString());
//        System.out.println("Metode Appearance Dengan Friend: " + rankedGameswithFriend.toString());
        System.out.println("----------------------------------------------------");
////////////////////////////////////////////////////////////////////////////////          
    }
           
    public static void main(String[] args){
        try {
            System.out.println("Hasil dari -- " + steam64id);
            Test ts = new Test();
            ts.doTesting();
            System.out.println(" ---------------------------------");
        } catch (Exception ex) {
            Logger.getLogger(Testing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
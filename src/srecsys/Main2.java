package srecsys;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import srecsys.recommendation.Games;
import srecsys.recommendation.RecommendationController;
import srecsys.scraper.UserFriendScraper;
import srecsys.scraper.UserGameScraper;
import srecsys.scraper.UserScraper;

public class Main2 {
    private static UserFriendScraper ufs;
    private static UserGameScraper ugs;
    private static UserScraper us;
    private static Games steamgames;
    private static Set<String> ownedGenre;
    private static Map<String, Double> rankedGames;
    
    private static Map<String, Double> rankedGameswithFriend;
    private static Map<String, Double> bonusScoreFromFriend;
    private static Map<String, Integer> commonGamesinFriend;
    private static List<String> gameList;
    
    private static Map<String, Double> sortedGames;
    private static Map<String, Double> top50Games;
    private static Map<String, Map<String,Double>> gameResults;
    
    
    public static void main(String[] args) throws Exception{
        
        RecommendationController RC = new RecommendationController();
        ufs = new UserFriendScraper();
        ugs = new UserGameScraper();
        us = new UserScraper();
        
        steamgames = new Games();
        ownedGenre = new HashSet<>();
        gameResults = new HashMap<>();
        
        String steam64id = "76561198115471724";
        
        //mengambil game, friendlist, dan info yang dimiliki user
        ufs.scrape(steam64id);
        ugs.scrape(steam64id);
        us.scrape(steam64id);
        
        RC.removeRandomGame(5, ugs);

        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
//        RC.removeOwnedGames(steamgames, ugs); OOV gara gara ini
        ownedGenre = RC.getOwnedGenres(ugs);
//        RC.removeNonGenreGames(ownedGenre, steamgames);
        
        System.out.println("Owned game genre of "+us.personaname);
        System.out.println(ownedGenre.toString());
        System.out.println("");
        
//        RC.saveTermsToFile("data/terms.txt", steamgames);
//        invertedTerms = RC.loadInvertedFile("data/terms.txt");
        RC.loadTerms("data/terms.txt");

////////////////////////////////////////////////////////////////////////////////
// COSINE SIMILARITY////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//        RC.computeIDF();
//        RC.computeTFIDF();
//        gameResults = RC.computeCosineScore(steamgames, ugs);
//        rankedGames = RC.computeFinalScore(gameResults);
//        RC.removeOwnedGames(rankedGames, ugs);
//        sortedGames = RC.sortMapByValues(rankedGames);        
//        System.out.println("hasilnya adalah");
//        System.out.println(sortedGames.toString());

//        gameResults = RC.computeCosineScore2(steamgames, ugs);
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// JACCARD SIMILARITY///////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////             
//  penghitungan dengan skor
//
        gameResults = RC.computeJaccardScore2(steamgames, ugs);
        rankedGames = RC.recommendbyScore(gameResults);
        System.out.println("tanpa friend: " + RC.sortandCutMap(rankedGames).toString());
        
        gameList = RC.loadAllGames(steamgames);
        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGames, bonusScoreFromFriend);
        System.out.println("dengan friend: " + rankedGameswithFriend.toString());        
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
// penghitungan dengan kemunculan
//
//        gameResults = RC.computeJaccardScore2(steamgames, ugs);
//        rankedGames = RC.recommendbyAppearance(steamgames, gameResults);
//        System.out.println("tanpa friend: " + rankedGames.toString());
//        
//        gameList = RC.loadAllGames(steamgames);
//        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
//        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
//        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGames, bonusScoreFromFriend);
//        System.out.println("dengan friend: " + rankedGameswithFriend.toString());
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////          
    }  
}

package srecsys;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
public class Testing {

    private static String steam64id = "76561198104407959";

    public Testing() {
    }
    
    public int countPrecision (List<String> removedGames, Set<String> recommendation){
        int retval = 0;
        for(String s: removedGames){
            if(recommendation.contains(s)){
                retval++;
            }
        }
        
        return retval;
    }
    
    public void doTestingAllGenre () throws IOException, Exception{
    
        List<String> removedGames = new ArrayList<>();
        int nHilang = 0;
        
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
        
        Map<String, Map<String,Double>> gameResults = new HashMap<>();
        
        //mengambil game, friendlist, dan info yang dimiliki user
        
        ufs.scrape(steam64id);
        ugs.scrape(steam64id);
        us.scrape(steam64id);
        
        nHilang = ugs.games.size()/3;
        
        // menghilangkan n game untuk diuji
        removedGames = RC.removeRandomGame(nHilang, ugs);
        RC.saveRemovedRandomGame("test/removedGames-" + steam64id +".txt", removedGames);

        //ambil game yang dihapus dari file
        removedGames = readRemovedGameFile(steam64id);
        System.out.println("Removed games : " + removedGames.toString());
        System.out.println("removedGames.size(): " + removedGames.size());

        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
        ownedGenre = RC.getOwnedGenres(ugs);
                
//        RC.removeNonGenreGames(ownedGenre, steamgames);
        
        System.out.println("Owned game genre of "+us.personaname);
        System.out.println(ownedGenre.toString());
        System.out.println("");
        
//        RC.saveTermsToFile("data/terms.txt", steamgames);
//        invertedTerms = RC.loadInvertedFile("data/terms.txt");
        RC.loadTerms("data/terms.txt");
        
        gameResults = RC.computeJaccardScore2(steamgames, ugs);
        rankedGamesScore = RC.recommendbyScore(gameResults, ugs);
        
        Map<String, Double> rankedGamesScoreCut = RC.sortandCutMap(rankedGamesScore, 12);
        Map<String, Double> rankedGamesScoreCutN = RC.sortandCutMap(rankedGamesScore, nHilang);
        
//        System.out.println("tanpa friend rankedGameScore: " + rankedGameScoreCut.toString());
        
        gameList = RC.loadAllGames(steamgames);
        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGamesScore, bonusScoreFromFriend);
//        System.out.println("dengan friend: " + rankedGameswithFriend.toString());    
        Map<String, Double> rankedGameswithFriendCut = RC.sortandCutMap(rankedGameswithFriend, 12);
        Map<String, Double> rankedGameswithFriendCutN = RC.sortandCutMap(rankedGamesScore, nHilang);
//        System.out.println("dengan friend rankedGameScore: " + rankedGameswithFriendCut.toString());
        
        int irisanGameScore = countPrecision (removedGames, rankedGamesScoreCut.keySet());
        int irisanGameScoreFriend = countPrecision (removedGames, rankedGameswithFriendCut.keySet());
        
        int irisanGameScoreN = countPrecision(removedGames, rankedGamesScoreCutN.keySet());
        int irisanGameScoreFriendN = countPrecision (removedGames, rankedGameswithFriendCutN.keySet());
        
        System.out.println("--------------------------------------------------------------------");
        System.out.println("Precision rankedGamesScore: " + (irisanGameScoreN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesScore: " + (irisanGameScore*1D)/12.0 + " dibagi 12");
        System.out.println("Precision rankedGamesScoreFriend: " + (irisanGameScoreFriendN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesScoreFriend: " + (irisanGameScoreFriend*1D)/12.0 + " dibagi 12");
        
        // penghitungan dengan kemunculan
//
//        gameResults = RC.computeJaccardScore2(steamgames, ugs);
        rankedGamesAppearance = RC.recommendbyAppearance(steamgames, gameResults, ugs);
        
        Map<String, Double> rankedGamesAppearanceCut = RC.sortandCutMap(rankedGamesAppearance, 12);
        Map<String, Double> rankedGamesAppearanceCutN = RC.sortandCutMap(rankedGamesAppearance, nHilang);        

//        System.out.println("tanpa friend rankedGameAppearance: " + rankedGamesAppearanceCut.toString());
        int irisanGameAppearance = countPrecision (removedGames, rankedGamesAppearanceCut.keySet());
        int irisanGameAppearanceN = countPrecision(removedGames, rankedGamesAppearanceCutN.keySet());
        
        System.out.println("Precision rankedGamesAppearance: " + (irisanGameAppearanceN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesAppearance: " + (irisanGameAppearance*1D)/12.0 + " dibagi 12");
        
//        gameList = RC.loadAllGames(steamgames);
//        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
//        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGamesAppearance, bonusScoreFromFriend);
        
        Map<String, Double> rankedGameswithFriendAppCut = RC.sortandCutMap(rankedGameswithFriend, 12);
        Map<String, Double> rankedGameswithFriendAppCutN = RC.sortandCutMap(rankedGameswithFriend, nHilang);
        
//        System.out.println("dengan friend rankedGameAppearance: " + rankedGameswithFriendAppCut.toString());
        int irisanGameAppearanceFriend = countPrecision (removedGames, rankedGameswithFriendAppCut.keySet());
        int irisanGameAppearanceFriendN = countPrecision (removedGames, rankedGameswithFriendAppCutN.keySet());
        
        System.out.println("Precision rankedGamesAppearanceFriend: " + (irisanGameAppearanceFriendN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesAppearanceFriend: " + (irisanGameAppearanceFriend*1D)/12.0 + " dibagi 12");
        System.out.println("--------------------------------------------------------------------");
        
    }
    
    public void doTestingGenreRemoved () throws IOException, Exception{
    
        List<String> removedGames = new ArrayList<>();
        int nHilang = 0;
        
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
        
        Map<String, Map<String,Double>> gameResults = new HashMap<>();
        
        //mengambil game, friendlist, dan info yang dimiliki user
        
        ufs.scrape(steam64id);
        ugs.scrape(steam64id);
        us.scrape(steam64id);
        
        nHilang = ugs.games.size()/3;
        
        // menghilangkan n game untuk diuji
//        removedGames = RC.removeRandomGame(nHilang, ugs);
//        RC.saveRemovedRandomGame("test/removedGames-" + steam64id +".txt", removedGames);

        //ambil game yang dihapus dari file
        removedGames = readRemovedGameFile(steam64id);
        System.out.println("Removed games : " + removedGames.toString());
        System.out.println("removedGames.size(): " + removedGames.size());

        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
        ownedGenre = RC.getOwnedGenres(ugs);
                
        RC.removeNonGenreGames(ownedGenre, steamgames);
        
        System.out.println("Owned game genre of "+us.personaname);
        System.out.println(ownedGenre.toString());
        System.out.println("");
        
//        RC.saveTermsToFile("data/terms.txt", steamgames);
//        invertedTerms = RC.loadInvertedFile("data/terms.txt");
        RC.loadTerms("data/terms.txt");
        
        gameResults = RC.computeJaccardScore2(steamgames, ugs);
        rankedGamesScore = RC.recommendbyScore(gameResults, ugs);
        
        Map<String, Double> rankedGamesScoreCut = RC.sortandCutMap(rankedGamesScore, 12);
        Map<String, Double> rankedGamesScoreCutN = RC.sortandCutMap(rankedGamesScore, nHilang);
        
//        System.out.println("tanpa friend rankedGameScore: " + rankedGameScoreCut.toString());
        
        gameList = RC.loadAllGames(steamgames);
        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGamesScore, bonusScoreFromFriend);
//        System.out.println("dengan friend: " + rankedGameswithFriend.toString());    
        Map<String, Double> rankedGameswithFriendCut = RC.sortandCutMap(rankedGameswithFriend, 12);
        Map<String, Double> rankedGameswithFriendCutN = RC.sortandCutMap(rankedGamesScore, nHilang);
//        System.out.println("dengan friend rankedGameScore: " + rankedGameswithFriendCut.toString());
        
        int irisanGameScore = countPrecision (removedGames, rankedGamesScoreCut.keySet());
        int irisanGameScoreFriend = countPrecision (removedGames, rankedGameswithFriendCut.keySet());
        
        int irisanGameScoreN = countPrecision(removedGames, rankedGamesScoreCutN.keySet());
        int irisanGameScoreFriendN = countPrecision (removedGames, rankedGameswithFriendCutN.keySet());
        
        System.out.println("--------------------------------------------------------------------");
        System.out.println("Precision rankedGamesScore: " + (irisanGameScoreN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesScore: " + (irisanGameScore*1D)/12.0 + " dibagi 12");
        System.out.println("Precision rankedGamesScoreFriend: " + (irisanGameScoreFriendN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesScoreFriend: " + (irisanGameScoreFriend*1D)/12.0 + " dibagi 12");
        
        // penghitungan dengan kemunculan
//
//        gameResults = RC.computeJaccardScore2(steamgames, ugs);
        rankedGamesAppearance = RC.recommendbyAppearance(steamgames, gameResults, ugs);
        
        Map<String, Double> rankedGamesAppearanceCut = RC.sortandCutMap(rankedGamesAppearance, 12);
        Map<String, Double> rankedGamesAppearanceCutN = RC.sortandCutMap(rankedGamesAppearance, nHilang);        

//        System.out.println("tanpa friend rankedGameAppearance: " + rankedGamesAppearanceCut.toString());
        int irisanGameAppearance = countPrecision (removedGames, rankedGamesAppearanceCut.keySet());
        int irisanGameAppearanceN = countPrecision(removedGames, rankedGamesAppearanceCutN.keySet());
        
        System.out.println("Precision rankedGamesAppearance: " + (irisanGameAppearanceN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesAppearance: " + (irisanGameAppearance*1D)/12.0 + " dibagi 12");
        
//        gameList = RC.loadAllGames(steamgames);
//        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
//        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGamesAppearance, bonusScoreFromFriend);
        
        Map<String, Double> rankedGameswithFriendAppCut = RC.sortandCutMap(rankedGameswithFriend, 12);
        Map<String, Double> rankedGameswithFriendAppCutN = RC.sortandCutMap(rankedGameswithFriend, nHilang);
        
//        System.out.println("dengan friend rankedGameAppearance: " + rankedGameswithFriendAppCut.toString());
        int irisanGameAppearanceFriend = countPrecision (removedGames, rankedGameswithFriendAppCut.keySet());
        int irisanGameAppearanceFriendN = countPrecision (removedGames, rankedGameswithFriendAppCutN.keySet());
        
        System.out.println("Precision rankedGamesAppearanceFriend: " + (irisanGameAppearanceFriendN*1D)/(removedGames.size()*1D) + " dibagi yang hilang");
        System.out.println("Precision rankedGamesAppearanceFriend: " + (irisanGameAppearanceFriend*1D)/12.0 + " dibagi 12");
        System.out.println("--------------------------------------------------------------------");
        
    }
    
    private List<String> readRemovedGameFile(String steam64id) {
        List<String> removedGames = new ArrayList<>();
        try {
            Scanner in = new Scanner(new FileReader("test/removedGames-" + steam64id +".txt"));
            while(in.hasNextLine()){
                removedGames.add(in.nextLine());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Testing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return removedGames;
    }
    
    
    public static void main(String[] args){
        try {
            System.out.println("Hasil dari -- " + steam64id);
            Testing ts = new Testing();
            ts.doTestingAllGenre();
            System.out.println(" --------- GENRE REMOVED ---------");
            System.out.println("");
            ts.doTestingGenreRemoved();
            System.out.println(" ---------------------------------");
        } catch (Exception ex) {
            Logger.getLogger(Testing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
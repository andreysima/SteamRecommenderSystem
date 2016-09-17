package srecsys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import srecsys.model.Game;
import srecsys.recommendation.Games;
import srecsys.recommendation.RecommendationController;
import srecsys.scraper.JSONController;
import srecsys.scraper.UserFriendScraper;
import srecsys.scraper.UserGameScraper;
import srecsys.scraper.UserScraper;

public class Main {
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
    private static List<String> recommendationResult;
    private static List<Game> recResultwithURL;
    
    private static Map<String, Map<String,Double>> gameResults;
    
    public static List<Game> scrapeURLforView(List<String> recResult) throws IOException{
        
        List<Game> recGameList = new ArrayList<>();
        Game g;
        
        for(int i = 0; i < recResult.size(); i++){
            
            String gameuri = String.format(
                "http://store.steampowered.com/api/appdetails?appids=%s",
                    recResult.get(i));
            
            JSONObject gameobj = JSONController.readJSONFromURL(gameuri);
            JSONObject gameresponse = gameobj.getJSONObject(recResult.get(i));
            
            if(gameresponse.getBoolean("success")){
                JSONObject gamedata = gameresponse.getJSONObject("data");
                if(gamedata.getString("type").equals("game")){
                    g = new Game();
                    g.setAppID(Integer.toString(gamedata.getInt("steam_appid")));
                    g.setName(gamedata.getString("name"));
                    g.setWebsiteURL("http://store.steampowered.com/app/"+g.appID);
                    
                    if(gamedata.has("header_image"))
                        g.setImageURL(gamedata.getString("header_image"));
                    else//jika tidak ada key header_image
                        g.setImageURL("");

                    recGameList.add(g);
                }
            }
        }
        
        return recGameList;
    }
    
    public static void main(String[] args) throws Exception{
        
        RecommendationController RC = new RecommendationController();
        ufs = new UserFriendScraper();
        ugs = new UserGameScraper();
        us = new UserScraper();
        
        steamgames = new Games();
        ownedGenre = new HashSet<>();
        gameResults = new HashMap<>();
        recResultwithURL = new ArrayList<>();
        
        String steam64id = "76561198118645234";
        
        //mengambil game, friendlist, dan info yang dimiliki user
        ufs.scrape(steam64id);
        ugs.scrape(steam64id);
        us.scrape(steam64id);
        
        //mengambil game yang ada di dalam Steam
        steamgames.loadTerms();
//        RC.removeOwnedGames(steamgames, ugs); OOV gara gara ini
        ownedGenre = RC.getOwnedGenres(ugs);
        RC.removeNonGenreGames(ownedGenre, steamgames);
        
        System.out.println("Owned game genre of "+us.personaname);
        System.out.println(ownedGenre.toString());
        System.out.println("");
        
//        RC.saveTermsToFile("data/terms.txt", steamgames);
//        invertedTerms = RC.loadInvertedFile("data/terms.txt");
        RC.loadTerms("data/terms.txt");

////////////////////////////////////////////////////////////////////////////////
// JACCARD SIMILARITY///////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////             
//  penghitungan dengan skor
//
//        gameResults = RC.computeJaccardScore2(steamgames, ugs);
//        rankedGames = RC.recommendbyScore(gameResults, ugs);
//        System.out.println("tanpa friend: " + RC.sortandCutMap(rankedGames, 12).toString());
//        
//        gameList = RC.loadAllGames(steamgames);
//        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
//        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
//        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGames, bonusScoreFromFriend);
//        System.out.println("dengan friend: " + rankedGameswithFriend.toString());        
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
// penghitungan dengan kemunculan
//
        gameResults = RC.computeJaccardScore2(steamgames, ugs);
        rankedGames = RC.recommendbyAppearance(steamgames, gameResults, ugs);
//        System.out.println("tanpa friend: " + RC.sortandCutMap(rankedGames,12).toString());
        
        gameList = RC.loadAllGames(steamgames);
        commonGamesinFriend = RC.getCommonGames(gameList, ufs, ugs, steam64id);
        bonusScoreFromFriend = RC.bonusScoreFromFriends(commonGamesinFriend, ufs);
        rankedGameswithFriend = RC.recommendbyScorewithFriend(rankedGames, bonusScoreFromFriend);
        System.out.println("hasil rekomendasi: " + rankedGameswithFriend.toString());
        
        recommendationResult = new ArrayList<>(rankedGameswithFriend.keySet());
        
        recResultwithURL = scrapeURLforView(recommendationResult);
        
        
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////          
    }  
}

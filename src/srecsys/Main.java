package srecsys;

import srecsys.recommendation.Games;
import srecsys.scraper.UserGameScraper;

public class Main {
    
    public static void main(String[] args) throws Exception{
        UserGameScraper ugs = new UserGameScraper();
        Games steamgames = new Games();
        
//        ugs.scrape("76561198115471724");
        
        // ini punya user
//        ugs.games;
        
        steamgames.JSONReader();
        
        // ini punya steam semua
//        steamgames.gameList;
     
        for(int i = 0; i < steamgames.gameList.size(); i++){
            System.out.println(steamgames.gameList.get(i).game_terms.toString());
        } 
        
    }
}

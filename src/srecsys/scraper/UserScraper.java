package srecsys.scraper;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import srecsys.Constants;
import srecsys.model.User;


/**
 * kelas untuk mengambil data game dari user
 */
public class UserScraper {
    
    public String personaname;
    
    public UserScraper(){
    }
    
    public User scrape(String steam64id) throws IOException, Exception{
        
//        UserGameScraper ugs = new UserGameScraper();
//        UserFriendScraper ufs = new UserFriendScraper();
        
        String uri = String.format(
            "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/"+
            "?&key=%s&steamids=%s&format=json",
                Constants.API_KEY,
                steam64id);
        
        JSONObject obj = JSONController.readJSONFromURL(uri);
        JSONObject response = obj.getJSONObject("response");
        
        JSONArray arr_players = response.getJSONArray("players");
        for(int i = 0; i < arr_players.length(); i++){
            personaname = arr_players.getJSONObject(i).getString("personaname");
        }
        
        User user = new User(steam64id, personaname);

//        ugs.scrape(steam64id);
//        ufs.scrape(steam64id);
//        
//        user.games = ugs.getGames();
//        user.friends = ufs.getFriendlist();
        
        return user;
    }
    
}

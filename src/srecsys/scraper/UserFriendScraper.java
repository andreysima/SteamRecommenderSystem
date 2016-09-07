package srecsys.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import srecsys.Constants;
import srecsys.model.Friend;

/**
 * kelas untuk mengambil data teman yang dimiliki seorang user
 */
public class UserFriendScraper {
   
    public List<String> friendlist;
    private Friend f;
    
    public UserFriendScraper(){
    }
    
    public void scrape(String steam64id) throws IOException{
//        System.out.println("Scraping friendlist for user "+steam64id);
        friendlist = new ArrayList<>();
        
        String uri = String.format(
            "http://api.steampowered.com/ISteamUser/GetFriendList/v0001/"+
            "?key=%s&steamid=%s&relationship=friend",
                Constants.API_KEY,
                steam64id);
        
        JSONObject obj = JSONController.readJSONFromURL(uri);
        JSONObject friendslist = obj.getJSONObject("friendslist");
        
        JSONArray arr_friends = friendslist.getJSONArray("friends");
        for(int i = 0; i < arr_friends.length(); i++){
            f = new Friend();
            f.setSteam64id(arr_friends.getJSONObject(i).get("steamid").toString());
            friendlist.add(f.steam64id);
        }
    }
    
    public List<String> getFriendlist(){
        return Collections.unmodifiableList(friendlist);
    }
}

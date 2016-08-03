package srecsys.scraper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import srecsys.Constants;
import srecsys.model.Game;

/**
 * kelas untuk mengambil data user terkait game
 */
public class UserGameScraper {
    
    private Set<Game> games;
    private Game g;
    
    public UserGameScraper(){
    }
    
    public void scrape(String steam64id) throws Exception{
        System.out.println("Scraping games for user "+steam64id);
        games = new HashSet<Game>();
        
        String uri = String.format(
            "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/"+
            "?key=%s&steamid=%s&format=json&include_appinfo=1"+
            "&include_played_free_games=1",
                Constants.API_KEY,
                steam64id);
        
        JSONObject obj = JSONController.readJSONFromURL(uri);
        JSONObject response = obj.getJSONObject("response");
        
        JSONArray arr_games = response.getJSONArray("games");
        for(int i = 0; i < arr_games.length(); i++){
            g = new Game();
            g.setName(arr_games.getJSONObject(i).get("name").toString());
            g.setPlaytime_forever(arr_games.getJSONObject(i).getLong("playtime_forever"));
            if(arr_games.getJSONObject(i).has("playtime_2weeks")){
                g.setPlaytime_2weeks(arr_games.getJSONObject(i).getLong("playtime_2weeks"));
            }
            g.appID = arr_games.getJSONObject(i).get("appid").toString();
            
            System.out.println("AppID: " + g.appID);
            
            String gameuri = String.format(
                "http://store.steampowered.com/api/appdetails?appids=%s",
                    g.appID);
            
            JSONObject gameobj = JSONController.readJSONFromURL(gameuri);
            JSONObject gameresponse = gameobj.getJSONObject(g.appID);
            
            if(gameresponse.getBoolean("success")){
            
                JSONObject gamedata = gameresponse.getJSONObject("data");
                if(gamedata.has("developers")){
                    JSONArray arr_developers = gamedata.getJSONArray("developers");

                    for(int a = 0; a < arr_developers.length(); a++){
                        g.developers.add(arr_developers.getString(a));
                    }
                }
                else{//jika ada key yang tidak ada
                   g.developers.add("");
                }
                if(gamedata.has("publishers")){
                    JSONArray arr_publishers = gamedata.getJSONArray("publishers");

                    for(int b = 0; b < arr_publishers.length(); b++){
                        g.publishers.add(arr_publishers.getString(b));
                    }
                }
                else{//jika ada key yang tidak ada
                   g.publishers.add("");
                }
                if(gamedata.has("genres")){
                    JSONArray arr_genres = gamedata.getJSONArray("genres");
                    
                    for(int c = 0; c < arr_genres.length(); c++){
                        g.genres.add(arr_genres.getJSONObject(c).get("description").toString());
                    }
                }
                else{
                    g.genres.add("");
                }

                games.add(g);
            }
        }
    }
    
    public Set<Game> getGames(){
        return Collections.unmodifiableSet(games);
    }
    
}

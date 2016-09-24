 package srecsys.scraper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject; 

import srecsys.model.Game;

/**
 * kelas untuk melakukan pengambilan data game yang berada di Steam
 * 
 */
public class GameScraper {
    
    private List<Integer> applist;
    private List<Game> gamelist;
    private Game g;
    
    public GameScraper() {
        applist = new ArrayList<>();
        gamelist = new ArrayList<>();
    }
    
    // mengambil semua aplikasi yang dimiliki Steam
    public void scrapeApps() throws IOException{
        String uri = "http://api.steampowered.com/ISteamApps/GetAppList/v0001/";
        
        JSONObject obj = JSONController.readJSONFromURL(uri);
        JSONObject app_list = obj.getJSONObject("applist");
        JSONObject apps = app_list.getJSONObject("apps");
        JSONArray app_array = apps.getJSONArray("app");
        
        for(int i = 0; i < app_array.length(); i++){
            System.out.println("Scraping application with appID : " + app_array.getJSONObject(i).getInt("appid"));
            applist.add(app_array.getJSONObject(i).getInt("appid"));
        }
    }
    
    // memfilter untuk mengambil game saja, bukan aplikasi
    public int filterGames(int i) throws InterruptedException, IOException{
        
        System.out.println("Filtering apps with appID : " + applist.get(i));
        String gameuri = String.format(
                "http://store.steampowered.com/api/appdetails?appids=%s",
                    applist.get(i));

        JSONObject gameobj = JSONController.readJSONFromURL(gameuri);
        JSONObject gameresponse = gameobj.getJSONObject(applist.get(i).toString());

        if(gameresponse.getBoolean("success")){
            JSONObject gamedata = gameresponse.getJSONObject("data");
            if(gamedata.getString("type").equals("game")){
                g = new Game();
                System.out.println(gamedata.getString("name"));
                g.setAppID(Integer.toString(gamedata.getInt("steam_appid")));
                g.setName(gamedata.getString("name"));
                g.isFree = gamedata.getBoolean("is_free");

                if(gamedata.has("detailed_description"))
                    g.setDetailed_description(gamedata.getString("detailed_description"));
                else//jika tidak ada key detailed description
                    g.setDetailed_description("");

                if(gamedata.has("about_the_game"))
                    g.setAbout_the_game(gamedata.getString("about_the_game"));
                else//jika tidak ada key about the game
                    g.setAbout_the_game("");
                
                if(gamedata.has("pc_requirements")){
                    String pc_req = "";
                    try{
                        JSONObject game_requirements = gamedata.getJSONObject("pc_requirements");
                        pc_req = game_requirements.getString("minimum");
                    }
                    catch(Exception e){}
                    try{
                        JSONArray arr_requirements = gamedata.getJSONArray("pc_requirements");
                        pc_req = arr_requirements.getJSONObject(0).getString("minimum");
                        
                    }
                    catch(Exception e){}
                    g.setPc_requirements(pc_req);
                    System.out.println("PC requirements: " + pc_req);
                }
                else//jika tidak ada key pc_req
                    g.setPc_requirements("");                

                if(gamedata.has("developers")){
                    JSONArray arr_developers = gamedata.getJSONArray("developers");

                    for(int a = 0; a < arr_developers.length(); a++){
                        g.developers.add(arr_developers.getString(a));
                    }
                }
                else{//jika tidak ada key developers
                   g.developers.add("");
                }
                if(gamedata.has("publishers")){
                    JSONArray arr_publishers = gamedata.getJSONArray("publishers");

                    for(int b = 0; b < arr_publishers.length(); b++){
                        g.publishers.add(arr_publishers.getString(b));
                    }
                }
                else{//jika tidak ada key publishers
                   g.publishers.add("");
                }
                if(gamedata.has("genres")){
                    JSONArray arr_genres = gamedata.getJSONArray("genres");

                    for(int c = 0; c < arr_genres.length(); c++){
                        g.genres.add(arr_genres.getJSONObject(c).get("description").toString());
                    }
                }
                else{//jika tidak ada key genres
                    g.genres.add("");
                }
                gamelist.add(g);
                return gamelist.size()-1;
            }
        }
        return 0;
    }
    
    // membuat file json untuk semua game
    public void createJSONFile(){
        JSONArray gameArray = new JSONArray();
        JSONObject game = new JSONObject();
        JSONArray developersArray = new JSONArray();
        JSONArray publishersArray = new JSONArray();
        JSONArray genresArray = new JSONArray();
        JSONObject gameObj = new JSONObject();
        
        for(int i = 0; i < applist.size(); i++){
            try {
                int id = filterGames(i);
                if(id > 0){
                    game = new JSONObject();
                    game.put("appID", gamelist.get(id).getAppID());
                    game.put("Name", gamelist.get(id).getName());
                    for(int j = 0; j < gamelist.get(id).developers.size(); j++){
                        developersArray = new JSONArray();
                        developersArray.put(gamelist.get(id).developers.get(j));
                    }
                    game.put("Developers", developersArray);
                    for(int j = 0; j < gamelist.get(id).publishers.size(); j++){
                        publishersArray = new JSONArray();
                        publishersArray.put(gamelist.get(id).publishers.get(j));
                    }
                    game.put("Publishers", publishersArray);
                    for(int j = 0; j < gamelist.get(id).genres.size(); j++){
                        genresArray = new JSONArray();
                        genresArray.put(gamelist.get(id).genres.get(j));
                    }
                    game.put("Genres", genresArray);
                    game.put("Detailed Description", gamelist.get(id).getDetailed_description());
                    game.put("About the Game", gamelist.get(id).getAbout_the_game());
                    game.put("PC Requirements", gamelist.get(id).getPc_requirements());
                    gameArray.put(game);
                }
                gameObj.put("Games", gameArray);
                if(i%150==0 && i!=0)
                    Thread.sleep(240000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameScraper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GameScraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try (FileWriter file = new FileWriter("D:/NetBeansProjects/SRecSysSpring/data/steamgamelistraw.json")) {
            file.write(gameObj.toString());
            System.out.println("Successfully Copied JSON Object to File...");
	} catch (IOException ex) {
            Logger.getLogger(GameScraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    public static void main(String[] args) throws IOException, InterruptedException{
        GameScraper gs = new GameScraper();

        gs.scrapeApps();
        gs.createJSONFile();
    }
}

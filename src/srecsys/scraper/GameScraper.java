package srecsys.scraper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

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
        
        System.out.println("Filtering games with appID : " + applist.get(i));
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

    // membuat file csv untuk semua game
    public void createCSVFile(){
        FileWriter fileWriter = null;
        String fileName = System.getProperty("user.home")+"/steamgamelistraw.csv";
        
        try {
            fileWriter = new FileWriter(fileName);
            System.out.println("Creating CSV file....");
            
            //untuk header
            fileWriter.append("game_id,game_name,game_developers,game_publishers,game_genres,game_descriptions");
            fileWriter.append("\n");
            for(int i = 0; i < applist.size(); i++){
                int id = filterGames(i);
                if(id > 0){
                    //untuk game_id
                    fileWriter.append(gamelist.get(id).getAppID());
                    fileWriter.append(",");
                    //untuk game_name
                    String game_name = gamelist.get(id).getName().replaceAll("[\",]", "");
                    fileWriter.append("\"");
                    fileWriter.append(game_name);
                    fileWriter.append("\"");
                    fileWriter.append(",");
                    //untuk game_developers
                    String game_developer;
                    fileWriter.append("\"");
                    for(int j = 0; j < gamelist.get(id).developers.size()-1; j++){
                        game_developer = gamelist.get(id).developers.get(j);
                        fileWriter.append(game_developer);
                        fileWriter.append(",");
                    }
                    game_developer = gamelist.get(id).developers.get(gamelist.get(id).developers.size()-1);
                    fileWriter.append(game_developer);
                    fileWriter.append("\"");
                    fileWriter.append(",");
                    //untuk game_publishers
                    String game_publisher;
                    fileWriter.append("\"");
                    for(int j = 0; j < gamelist.get(id).publishers.size()-1; j++){
                        game_publisher = gamelist.get(id).publishers.get(j);
                        fileWriter.append(game_publisher);
                        fileWriter.append(",");
                    }
                    game_publisher = gamelist.get(id).publishers.get(gamelist.get(id).publishers.size()-1);
                    fileWriter.append(game_publisher);
                    fileWriter.append("\"");
                    fileWriter.append(",");            
                    //untuk game_genre
                    String game_genre;
                    fileWriter.append("\"");
                    for(int j = 0; j < gamelist.get(id).genres.size()-1; j++){
                        game_genre = gamelist.get(id).genres.get(j);
                        fileWriter.append(game_genre);
                        fileWriter.append(",");
                    }
                    game_genre = gamelist.get(id).genres.get(gamelist.get(id).genres.size()-1);
                    fileWriter.append(game_genre);
                    fileWriter.append("\"");
                    fileWriter.append(",");
                    //untuk game_descriptions
                    fileWriter.append("\"");
                    String detailed_desc = gamelist.get(id).getDetailed_description().replaceAll("[\",]", "");
                    fileWriter.append(Jsoup.parse(detailed_desc).text());
                    fileWriter.append(" ");
                    String about_game = gamelist.get(id).getAbout_the_game().replaceAll("[\",]", "");
                    fileWriter.append(Jsoup.parse(about_game).text());
                    fileWriter.append("\"");

                    fileWriter.append("\n");
                }
                if(i%199==0 && i!=0)
                    Thread.sleep(240000);
            }
            
            System.out.println("CSV file created");
        }catch (Exception e) {
            System.out.println("Error in writing CSV");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException{
        GameScraper gs = new GameScraper();

        gs.scrapeApps();
        gs.createCSVFile();
    }
}

package srecsys.recommendation;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import srecsys.model.Game;

public class Games {
    public List<String> attributeList = new ArrayList<>();
    public Map<String, Game> gameList = new HashMap<>();
    private Game g;
    
    public List<String> publisherList;
    public List<String> developerList;
    public List<String> game_name_List = new ArrayList<>();
    public List<String> game_detailed_description_List = new ArrayList<>();
    public List<String> game_about_the_game_List = new ArrayList<>();
    
    public List<String> stopWords = new ArrayList<>();
    
    public void loadTerms() throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        
        Object obj = parser.parse(new FileReader("data/steamgamelistraw - all.json"));
        JSONObject jsonobj = (JSONObject) obj;
        JSONArray games_array = (JSONArray) jsonobj.get("Games");
        Iterator i = games_array.iterator();
        
        while(i.hasNext()){
            g = new Game();    
            JSONObject game = (JSONObject) i.next();
            // untuk appID
            g.setAppID((String) game.get("appID"));
            // untuk game_name
            String game_name = (String) game.get("Name");
            g.setName(game_name);        
//            g.addTerm(game_name);
            // untuk detailed_description
            String game_detailed_description = (String) game.get("Detailed Description");
            g.setDetailed_description(game_detailed_description);            
            g.addTerm(game_detailed_description);
            // untuk about_the_game
            String game_about_the_game = (String) game.get("About the Game");
            g.setAbout_the_game(game_about_the_game);            
            g.addTerm(game_about_the_game);
            // untuk game_developers
            JSONArray dev_array = (JSONArray) game.get("Developers");
            Iterator i_dev = dev_array.iterator();
            String game_developers;
            while(i_dev.hasNext()){
                game_developers = (String) i_dev.next();
                g.developers.add(game_developers);
                g.addTerm(game_developers);
            }
            // untuk game_publishers
            JSONArray pub_array = (JSONArray) game.get("Publishers");
            Iterator i_pub = pub_array.iterator();
            String game_publishers;
            while(i_pub.hasNext()){
                game_publishers = (String) i_pub.next();
                g.publishers.add(game_publishers);
                g.addTerm(game_publishers);
            }
            // untuk gane_genre
            JSONArray genre_array = (JSONArray) game.get("Genres");
            Iterator i_genre = genre_array.iterator();
            String game_genres;
            while(i_genre.hasNext()){
                game_genres = (String) i_genre.next();
                g.genres.add(game_genres);
                g.addTerm(game_genres);
            }
            
            gameList.put(g.appID, g);
        }
    }
        
}

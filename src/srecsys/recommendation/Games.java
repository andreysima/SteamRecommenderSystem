package srecsys.recommendation;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import srecsys.model.Game;

public class Games {
    public List<String> attributeList;
    public List<Game> gameList;
    public List<String> publisherList;
    public List<String> developerList;
    
    public static void JSONReader() throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        
        Object obj = parser.parse(new FileReader("data/steamgamelistraw.json"));
        JSONArray games_array = (JSONArray) obj;
        
        for(int i = 0; i < games_array.length(); i++){
            String game_name = games_array.getJSONObject(i).getString("Name");
            JSONArray publishers_array = games_array.getJSONArray(i);
        }
        
    }
}

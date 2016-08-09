package srecsys.recommendation;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import srecsys.model.Game;

public class Games {
    public List<String> attributeList;
    public List<Game> gameList;
    
    public static void JSONReader() throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        
        Object obj = parser.parse(new FileReader("data/steamgamelistraw.json"));
        
    }
}

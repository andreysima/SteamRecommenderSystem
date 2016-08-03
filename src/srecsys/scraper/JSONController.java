package srecsys.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONObject;

/**
 * kelas untuk mengontrol data terkait JSON
 */
public class JSONController {
    public static String readAll(Reader rd) throws IOException{
        StringBuilder sb = new StringBuilder();
        int cp;
        while((cp = rd.read()) != -1){
            sb.append((char)cp);
        }
        return sb.toString();
    }
    
    public static JSONObject readJSONFromURL(String url) throws IOException{
        InputStream is = new URL(url).openStream();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsontext = readAll(br);
            JSONObject obj = new JSONObject(jsontext);
            return obj;
        }
        finally{
            is.close();
        }
    }
}

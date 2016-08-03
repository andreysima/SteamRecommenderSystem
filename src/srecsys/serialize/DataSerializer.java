package srecsys.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import srecsys.model.Game;
import srecsys.model.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Data serialization support. Uses Google Gson to store gamer data,
 * then retrieve it for offline processing.
 * 
 * NOTE: user data retrieval is currently unsupported, due to some tweaks
 * to the core domain model. 
 * 
 * PROPERTY OF JAMES SIDDLE
 */
public class DataSerializer {

    private final Gson gson = new Gson();

    public void writeUsers(Set<User> users, OutputStream os){
        
        Gson gsonb = new GsonBuilder().setPrettyPrinting().create();

        String json = gsonb.toJson(users);
        byte[] jbytes = json.getBytes();

        try {
            os.write(jbytes);
        } catch (IOException ex) {
            throw new RuntimeException(
                "IO exception detected while writing user json data", ex);
        }
    }

    public void writeGames(Set<Game> games, OutputStream os){
        
        Gson gsonb = new GsonBuilder().setPrettyPrinting().create();

        String json = gsonb.toJson(games);
        byte[] jbytes = json.getBytes();

        try {
            os.write(jbytes);
        } catch (IOException ex) {
            throw new RuntimeException(
                "IO exception detected while writing game json data", ex);
        }
    }
    
    public void writeFriends(Set<User> users, OutputStream os){
        
        Gson gsonb = new GsonBuilder().setPrettyPrinting().create();

        String json = gsonb.toJson(users);
        byte[] jbytes = json.getBytes();

        try {
            os.write(jbytes);
        } catch (IOException ex) {
            throw new RuntimeException(
                "IO exception detected while writing user json data", ex);
        }
    }

    public void writeGame(Game game, OutputStream os){
        
        Gson gsonb = new GsonBuilder().setPrettyPrinting().create();

        String json = gsonb.toJson(game);
        byte[] jbytes = json.getBytes();

        try {
            os.write(jbytes);
        } catch (IOException ex) {
            throw new RuntimeException(
                "IO exception detected while writing game json data", ex);
        }
    }

    public Set<User> readUsers(InputStream is) throws IOException {
        
        JsonReader jr = new JsonReader(new InputStreamReader(is));
        
        return readUserArray(jr);
    }

    public Set<Game> readGameSet(InputStream is) throws IOException {

        JsonReader jr = new JsonReader( new InputStreamReader(is) );    
        JsonParser parser = new JsonParser();    
        JsonArray array = parser.parse(jr).getAsJsonArray();

        Set<Game> games = new HashSet<Game>();

        for(JsonElement jsel : array){
            Game g = gson.fromJson(jsel, Game.class);
            games.add(g);
        }

        return games;
    }

    private Set<User> readUserArray(JsonReader jr) throws IOException {

        Set<User> users = new HashSet<User>();

        jr.beginArray();
        while (jr.hasNext()) {
            users.add( readUser(jr) );
        }
        jr.endArray();

        return users;
    }

    private User readUser(JsonReader jr) throws IOException {

        String steamID = null;
        String userName = null;
        Set<Game> games = null;

        jr.beginObject();

        while (jr.hasNext()) {

            String name = jr.nextName();

            if (name.equals("steam64id")) {
                steamID = jr.nextString();      
                System.out.println( String.format("Steam ID: %s", steamID) );
            } if (name.equals("name")) {
                userName = jr.nextString();      
                System.out.println( String.format("Name: %s", userName) );
            } else if (name.equals("games")) {
                games = readGameSet(jr);
                System.out.println( String.format("Games: %s", games) );
            }
        }
        jr.endObject();

        if (steamID == null || userName == null || games == null) {
            throw new RuntimeException( String.format( "Invalid data detected for user: %s", steamID ));
        }

        // Construct the actual in-memory User representation from the various pieces    
        User user = new User(steamID, userName);
        for (Game g : games) {
            user.addGame(g);
        }
        return user;
    }

    private Set<Game> readGameSet(JsonReader jr) throws IOException {
        Type collectionType = new TypeToken<Set<Game>>(){}.getType();
        
        return gson.fromJson(jr, collectionType);

    }
}

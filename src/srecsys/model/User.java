package srecsys.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * kelas untuk mendefinisikan seorang user
 */
public class User {
    // parameter
    public String steam64id;
    public String name;
    public List<Friend> friends;
    public List<Game> games;
    
    public User(String s64id, String name){
        this.steam64id = s64id;
        this.name = name;
        this.friends = new ArrayList<>();
        this.games = new ArrayList<>();
    }
    
    public void addGame(Game g){
        games.add(g);
    }
    
    public List<Game> getGames(){
        return Collections.unmodifiableList(games);
    }
    
    public void addFriend(Friend f){
        friends.add(f);
    }
    
    public List<Friend> getFriends(){
        return Collections.unmodifiableList(friends);
    }
    
    @Override
    public String toString() {
        int totalGames = games.size();
		
        String tos = String.format(
                "%s/%s\tGames:%d",
                steam64id, name, totalGames);
        return tos;
    }
}

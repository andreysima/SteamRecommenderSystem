package srecsys.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * kelas untuk mendefinisikan seorang user
 */
public class User {
    // parameter
    public String steam64id;
    public String name;
    public Set<Friend> friends;
    public Set<Game> games;
    
    public User(String s64id, String name){
        this.steam64id = s64id;
        this.name = name;
        this.friends = new HashSet<Friend>();
        this.games = new HashSet<Game>();
    }
    
    public void addGame(Game g){
        games.add(g);
    }
    
    public Set<Game> getGames(){
        return Collections.unmodifiableSet(games);
    }
    
    public void addFriend(Friend f){
        friends.add(f);
    }
    
    public Set<Friend> getFriends(){
        return Collections.unmodifiableSet(friends);
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

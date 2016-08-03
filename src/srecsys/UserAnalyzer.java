package srecsys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import srecsys.model.Game;
import srecsys.model.User;
import srecsys.serialize.DataSerializer;

/**
 * kelas untuk menganalisis data user yang telah didapat
 */
public class UserAnalyzer {
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        
        String inUserFilename = "data/inUser.txt";
        String inGameFilename = "data/inGame.txt";
        
        FileInputStream fisU = new FileInputStream(new File(inUserFilename));
        FileInputStream fisG = new FileInputStream(new File(inGameFilename));
        DataSerializer ds = new DataSerializer();
        
        Set<User> users = ds.readUsers(fisU);
        Set<Game> games = ds.readGameSet(fisG);
        
        System.out.println("User\tGame");
        
        for(User u : users){
            for (Game ug : u.getGames()){
                System.out.println(String.format("~%s~\t~%s", u.name, ug.appID));
            }
        }
    }
        
    public static Map<Game, Integer> deriveTopGames(Set<User> users){
        Map<Game, Integer> gameData = new HashMap<Game, Integer>();

        for(User u : users){
            System.out.println(u);
            for(Game g : u.getGames()){
                Integer count = gameData.get(g);
                count = count == null ? 1 : count + 1;

                gameData.put(g, count);
            }
        }

        return gameData;
    }
}

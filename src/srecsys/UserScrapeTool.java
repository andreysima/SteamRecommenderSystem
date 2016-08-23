package srecsys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import static srecsys.Constants.FILE_SEP;
import srecsys.model.Game;
import srecsys.model.User;
import srecsys.scraper.UserFriendScraper;
import srecsys.scraper.UserScraper;
import srecsys.serialize.DataSerializer;

public class UserScrapeTool {
    
    public static void main(String[] args) throws IOException, InterruptedException, Exception{
        String inFile = "data/input.txt";
        String dataset = "output";
        
        Set<User> users = new HashSet<User>();
        Set<Game> games = new HashSet<Game>();
        
        System.out.println("Scraping Data...");
        
        scrapeUsers(inFile, users);
//        scrapeGames(games);
        
        System.out.println("Store Data...");
        persistData(dataset, users, games);
        
        System.out.println("Summary...");
        summarize(users,games);
        
    }
    
    private static void scrapeUsers(String inFile, Set<User> users) throws IOException, Exception{
        UserScraper us = new UserScraper();
        
        List<String> idLines = FileUtils.readLines(new File(inFile));
        for(String steamID : idLines){
            User user = us.scrape(steamID);
            users.add(user);
        }
    }
    
    private static void scrapeFriends(String inFile, List<String> friendlist) throws IOException, Exception{
        UserFriendScraper ufs = new UserFriendScraper();
        
        List<String> idLines = FileUtils.readLines(new File(inFile));
        for(String steamID : idLines){
            ufs.scrape(steamID);
            friendlist = ufs.getFriendlist();
        }
    }
    
    private static void persistData(String dataset, Set<User> users, 
            Set<Game> games) throws FileNotFoundException, IOException{
        
        DataSerializer ds = new DataSerializer();
        
        String outUserFileName = "data" + FILE_SEP + dataset + "_users.json";
        String outGameFileName = "data" + FILE_SEP + dataset + "_games.json";
        
        File outFile;
        FileOutputStream fos;
        
        outFile = new File(outUserFileName);
        fos = new FileOutputStream(outFile);
        
        System.out.println("Writing user data to: "+outFile.getAbsolutePath());
        ds.writeUsers(users, fos);
        fos.close();
        
        outFile = new File(outGameFileName);
        fos = new FileOutputStream(outFile);
        
        System.out.println("Writing game data to: "+outFile.getAbsolutePath());
        ds.writeGames(games, fos);
        fos.close(); 
    }
    
    private static void summarize(Set<User> users, Set<Game> games){
        
//        Map<Game, Integer> top = UserAnalyzer.deriveTopGames(users);
//        ValueComparator vc = new ValueComparator(top);
//        TreeMap<Game, Integer> sorted_map = new TreeMap<Game, Integer>(vc);
//        
//        System.out.println(String.format("Top Games (%d total)", games.size()));
//        for(Game g : sorted_map.keySet()){
//            System.out.println(String.format("%s users: game %s", top.get(g), g.appID));
//        }
//        
//        System.out.println("ALL USERS");
//        
//        for(User u : users){
//            System.out.println(u);
//        }
    }
    
    static class ValueComparator implements Comparator<Game>{
        
        Map<Game, Integer> base;
        public ValueComparator(Map<Game, Integer> base){
            this.base = base;
        }
        
        @Override
        public int compare(Game a, Game b) {
            if(base.get(a) >= base.get(b)){
                return -1;
            }
            else{
                return 1;
            }
        }
    }
}

package srecsys.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import srecsys.stemmer.PorterStemmer;

/**
 * kelas untuk mendefinisikan game
 */
public class Game {
    // parameter
    public String appID;
    private String name;
    private String detailed_description;
    private String about_the_game;
    private Long playtime_forever;
    private Long playtime_2weeks;
    private String websiteURL;
    private String imageURL;
    private String pc_requirements; 
    public boolean isFree;
    public List<String> developers;
    public List<String> publishers;
    public List<String> genres;
    public Map<String, Double> game_terms;
    
    private List<String> stopWords;
    
    public Game() throws IOException{
        this.appID = null;
        this.name = null;
        this.playtime_forever = null;
        this.playtime_2weeks = null;
        this.websiteURL = null;
        this.imageURL = null;
        this.developers = new ArrayList<>();
        this.publishers = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.game_terms = new HashMap<>();
        this.stopWords = new ArrayList<>();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader("D:/NetBeansProjects/SRecSysSpring/data/stopwords.txt"));
            String currentLine;
            
            while((currentLine = br.readLine()) != null){
               stopWords.add(currentLine);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    public Long getPlaytime_forever() {
        return playtime_forever;
    }

    public void setPlaytime_forever(Long playtime_forever) {
        this.playtime_forever = playtime_forever;
    }
    
    public Long getPlaytime_2weeks() {
        return playtime_2weeks;
    }

    public void setPlaytime_2weeks(Long playtime_2weeks) {
        this.playtime_2weeks = playtime_2weeks;
    }
    
    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getPc_requirements() {
        return pc_requirements;
    }

    public void setPc_requirements(String pc_requirements) {
        this.pc_requirements = pc_requirements;
    }
    
    public String getDetailed_description() {
        return detailed_description;
    }

    public void setDetailed_description(String detailed_description) {
        this.detailed_description = detailed_description;
    }

    public String getAbout_the_game() {
        return about_the_game;
    }

    public void setAbout_the_game(String about_the_game) {
        this.about_the_game = about_the_game;
    }
    
    public void addTerm(String term) throws IOException{
        List<String> unstemmed_term_array = removeStopWords(term);
        List<String> term_array = stemAll(unstemmed_term_array); 
        

        for(int i = 0; i < term_array.size(); i++){
            if (term_array.get(i).length() > 0) {
                if(game_terms.containsKey(term_array.get(i))){
                    game_terms.put(term_array.get(i), game_terms.get(term_array.get(i))+1.0);
                }
                else{
                    game_terms.put(term_array.get(i), 1.0);                 
                }
            }
        }
    }
    
    public List<String> removeStopWords(String text) throws FileNotFoundException, IOException{
        List<String> texts = new LinkedList<>(Arrays.asList(cleanString(text).split("\\s+")));
        
        for(int i = texts.size()-1; i >= 0; i--){
            for(int j = 0; j < stopWords.size(); j++){
                if(stopWords.get(j).contains(texts.get(i))){
                    texts.set(i, "");
                }
            }
        }
        
        return texts;
    }
    
    public String cleanString(String text){
        String newtext1 = text.replaceAll("\\\\", "");
        String newtext2 = newtext1.replaceAll("\\b(https?|ftp|file):\\/\\/[www\\.][-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "").toLowerCase();
        String newtext3 = newtext2.replaceAll("<\\/?[^>]+>", "").toLowerCase();
        String newtext4 = newtext3.replaceAll("[^\\w\\s]+|\\w*\\d\\w*", "").toLowerCase();
        
        return newtext4;
    }
     
    public String stem(String word) {
        PorterStemmer stem = new PorterStemmer();
        stem.add(word.toCharArray(), word.length());
        stem.stem();

        return stem.toString();
    }
    
    public List<String> stemAll(List<String> oldList){
        List<String> newList = new ArrayList<>();
        
        for(int i = 0; i < oldList.size(); i++){
            newList.add(stem(oldList.get(i)));
        }
        
        return newList;
    }   
}

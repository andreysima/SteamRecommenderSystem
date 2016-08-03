package srecsys.model;

import java.util.ArrayList;
import java.util.List;

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
    public boolean isFree;
    public List<String> developers;
    public List<String> publishers;
    public List<String> genres;
    
    public Game(){
        this.appID = null;
        this.name = null;
        this.playtime_forever = null;
        this.playtime_2weeks = null;
        this.developers = new ArrayList<>();
        this.publishers = new ArrayList<>();
        this.genres = new ArrayList<>();
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
}

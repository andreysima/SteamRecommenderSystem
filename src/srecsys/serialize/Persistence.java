package srecsys.serialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import static srecsys.Constants.FILE_SEP;

public class Persistence {
    
    public static OutputStream prepGameFile(String dataset) throws FileNotFoundException{
        String outGameFilename = "data" + FILE_SEP + dataset + "_games.json";
        
        File file;
        FileOutputStream outputStream;
        
        file = new File(outGameFilename);
        outputStream = new FileOutputStream(file);
        
        System.out.println("Writing serialized game data to: " + file.getAbsolutePath());
        
        return outputStream;
    }
}

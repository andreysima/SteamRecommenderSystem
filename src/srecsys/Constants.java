package srecsys;

/**
 * kelas untuk menyimpan konstanta yang akan dibutuhkan kemudian untuk
 * pengambilan data
 */

public class Constants {
    // parameter
    public static String FILE_SEP = System.getProperty("file.separator");
    public static String API_KEY = "711A3A5C526B3E04F62A9D9D502A1452";
 
    static {
        System.out.println("Using steam API key: " + API_KEY);
    }
}
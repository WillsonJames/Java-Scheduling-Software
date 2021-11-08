import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Server name: wgudb.ucertify.com
 Port: 3306
 Database name: WJ08B6v
 Username: U08B6v
 Password: 53689237941 **/

public class DBConnection {

    //JDBC URL
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String ip = "//wgudb.ucertify.com:3306/";
    private static final String dbName = "WJ08B6v";
    private static final String jdbcurl = protocol + vendorName + ip + dbName;

    //driver
    private static final String mysqljdbcdriver = "com.mysql.cj.jdbc.Driver";
    private static Connection conn = null;

    //username and password
    private static final String userName = "U08B6v";
    private static String password = "53689237941";

    /**
     * Initializes connection to the database and returns connection object
     * @return conn
     */
    public static Connection startConnection() {
        try {
            Class.forName(mysqljdbcdriver);
            conn = DriverManager.getConnection(jdbcurl, userName, password);
            System.out.println("Connection Successful");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Disconnects from database
     */
    public static void closeConnection(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Connection Closed");
    }

    /**
     * Returns current active connection
     * @return conn
     */
    public static Connection getConnection(){
        return conn;
    }
}

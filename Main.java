import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author James Fuller
 * C195 project Final
 * 07/26/2021
 */

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{

        //checks for french canadian language
        ResourceBundle lan = ResourceBundle.getBundle("Nat", Locale.forLanguageTag("EN"));
        if(Locale.getDefault().getLanguage().contains("fr")) {
            lan = ResourceBundle.getBundle("Nat", Locale.forLanguageTag("FR"));
        }

        //creates loader and scene, then shows window
        Parent root = FXMLLoader.load(getClass().getResource("MainPage.fxml"), lan);
        String content = lan.getString("SchedulingProgram");
        primaryStage.setTitle(content); //french
        primaryStage.setScene(new Scene(root, 1430, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {

        DBConnection.startConnection();

        //add contacts
        //DBAccess.genericAlteration("INSERT INTO contacts (Contact_ID, Contact_Name, Email) VALUES (1, 'Jane', 'Jane@yahoo.com')");
        //DBAccess.genericAlteration("INSERT INTO contacts (Contact_ID, Contact_Name, Email) VALUES (2, 'Gene', 'Gene@yahoo.com')");

        //User lookup and add
        //DBAccess.genericAlteration("INSERT INTO users (User_Name, Password, Create_Date, Created_By, Last_Updated_By) VALUES ('test', 'test', '2021-07-1-00:00:00', 'Admin', 'Admin')");
        //DBAccess.genericSearch("SELECT User_Name, Password FROM users", 2);

        launch(args);
        DBConnection.closeConnection();
    }
}

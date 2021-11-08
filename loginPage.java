import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Controls the login dialog with username and password inputs
 */
public class loginPage {
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private TextField local;

    public void setLocal(String loc){
        local.setText(loc);
    }

    /**
     * Takes the password entered by the user and sends it to the controller
     * @return passwordAtt
     */
    @FXML
    public String attemptLoginPass(){
        String passwordAtt = password.getText().trim();
        return passwordAtt;
    }

    /**
     * Takes the username entered by the user and sends it to the controller
     * @return userAtt
     */
    @FXML
    public String attemptLoginUser(){
        String userAtt = username.getText().trim();
        return userAtt;
    }
}

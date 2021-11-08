import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controls the customer page to add and modify customers
 */
public class CustomerDialogController {
    //Customer variables text fields
    @FXML
    private TextField addID;
    @FXML
    private TextField addName;
    @FXML
    private TextField addAddress;
    @FXML
    private TextField addCity;
    @FXML
    private TextField addPostal;
    @FXML
    private TextField addPhone;

    //country buttons and province selector
    @FXML
    private RadioButton unitedStates;
    @FXML
    private RadioButton unitedKingdom;
    @FXML
    private RadioButton canada;
    @FXML
    private ComboBox<String> province;

    //set province lists
    private ObservableList<String> ukProvinces = DBAccess.divisionSearch("230");
    private ObservableList<String> canadaProvinces = DBAccess.divisionSearch("38");
    private ObservableList<String> usProvinces = DBAccess.divisionSearch("231");

    //gets username from login ingo
    private String userName = "";

    /**
     * sSts the username to maintain login through multiple windows
     * @param userName username of logged in user
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }


    //general methods

    /**
     * Takes the values from boxes entered by the user and generates new customer
     */
    public boolean processResults() {
        String name = addName.getText().trim();
        String address = addAddress.getText().trim();
        address += ", " + addCity.getText().trim();
        String phone = addPhone.getText().trim();
        String postal = addPostal.getText().trim();
        if (phone.equals("") || name.equals("") || addAddress.getText().trim().equals("") || addCity.getText().trim().equals("") || postal.equals("")){
            Controller.genericAlert("allCategories");
            return false;
        }
        int divisionID = 0;
        try {
            divisionID = Controller.parseCommas(province.getSelectionModel().getSelectedItem());
        } catch (Exception e){
            Controller.genericAlert("allCategories");
            return false;
        }

        new Customer(name, address, postal, phone, divisionID, userName);
        return true;
    }

    /**
     * Takes the customer to be modified and prefils boxes with customer data
     * @param Customer customer to be modified
     */
    public void editCustomer(Customer Customer) {
        addID.setText(String.valueOf(Customer.getId()));
        addName.setText(Customer.getName());
        //addresses
        String[] splitAddress = Customer.getAddress().split(",", 2);
        addAddress.setText(splitAddress[0]);
        addCity.setText(splitAddress[1].trim());

        addPhone.setText(Customer.getPhone());
        addPostal.setText(Customer.getPostal());

        //new province
        String newProvince = "";
        //canada
        for (int i = 0; i < canadaProvinces.size(); i++) {
            if (Controller.parseCommas(canadaProvinces.get(i)) == Customer.getDivisionID()) {
                newProvince = canadaProvinces.get(i);
                canada.setSelected(true);
            }
        }
        //us
        for (int i = 0; i < usProvinces.size(); i++) {
            if (Controller.parseCommas(usProvinces.get(i)) == Customer.getDivisionID()) {
                newProvince = usProvinces.get(i);
                unitedStates.setSelected(true);
            }
        }
        //uk
        for (int i = 0; i < ukProvinces.size(); i++) {
            if (Controller.parseCommas(ukProvinces.get(i)) == Customer.getDivisionID()) {
                newProvince = ukProvinces.get(i);
                unitedKingdom.setSelected(true);
            }
        }
        province.getSelectionModel().select(newProvince);
    }

    /**
     * Takes the updated values and modifies the selected customer
     * @param Customer customer being modified
     */
    public boolean updateCustomer(Customer Customer) {
        String name = addName.getText().trim();
        String address = addAddress.getText().trim();
        address += ", " + addCity.getText().trim();
        String phone = addPhone.getText().trim();
        String postal = addPostal.getText().trim();
        if (phone.equals("") || name.equals("") || addAddress.getText().trim().equals("") || addCity.getText().trim().equals("") || postal.equals("")){
            Controller.genericAlert("allCategories");
            return false;
        }
        int division = 0;
        try {
            division = Controller.parseCommas(province.getSelectionModel().getSelectedItem());
        } catch (Exception e){
            Controller.genericAlert("allCategories");
            return false;
        }

        Customer.updateCustomer(Customer, name, address, postal, phone, division, userName);
        return true;
    }

    /**
     * sets the state/province options for UK
     */
    public void countryClickedUK() {
        province.setItems(ukProvinces);
    }

    /**
     * sets the state/province options for US
     */
    public void countryClickedUS() {
        province.setItems(usProvinces);
    }

    /**
     * sets the state/province options for Canada
     */
    public void countryClickedCanada() {
        province.setItems(canadaProvinces);
    }
}



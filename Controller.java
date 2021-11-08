import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.*;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls the mainpage menus and tables
 */
public class Controller {

    //main lists
    @FXML
    private BorderPane mainPage;
    @FXML
    private TableView<Appointment> AppointmentsList;
    @FXML
    private TableView<Customer> CustomersList;

    //Appointment sorters
    @FXML
    private ToggleGroup weekMonth;
    @FXML
    private DatePicker weekSelection;
    @FXML
    private ComboBox<String> weekMonthCombo;
    @FXML
    private ComboBox<String> contactCombo;

    //Appointment report
    @FXML
    private TextField contactReport;
    @FXML
    private TextField contactReportMinutes;

    //Customer reports
    @FXML
    private ComboBox<String> monthCombo;
    @FXML
    private TextField monthReport;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private TextField typeReport;
    @FXML
    private TextField typemonthReport;

    //login displays
    @FXML
    private TextField loggedIn;
    @FXML
    private TextField loggedInID;

    //login variables
    public int userID = 0;
    public String userName = "";
    private boolean login = false;

    //Lists
    private ObservableList<String> contactList = DBAccess.contactSearch();
    private ObservableList<String> months = FXCollections.observableArrayList();
    private ObservableList<Appointment> tempAssociatedAppts = FXCollections.observableArrayList();

    //language resource loaded
    private ResourceBundle lan = ResourceBundle.getBundle("Nat", Locale.forLanguageTag("EN"));

    //main controller functions

    /**
     * Sets the change listener, gets correct user language and initializes selectionmodels for tables and month options
     */
    public void initialize() {
        //initializes the change listener for customer report
        CustomersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Customer>() {
            @Override
            public void changed(ObservableValue<? extends Customer> observable, Customer customer, Customer t1) {
                if(t1 != null){
                    Customer currItem = CustomersList.getSelectionModel().getSelectedItem();
                    generateReport(currItem);
                }
                else {
                    //associatedAppointmentsList.setItems(null);
                }
            }
        });

        //ensures each list can only have one selected item
        AppointmentsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        CustomersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        //month contact setup
        months.addAll("1, JAN", "2, FEB", "3, MAR", "4, APR", "5, MAY", "6, JUN", "7, JUL", "8, AUG", "9, SEP", "10, OCT", "11, NOV", "12, DEC");
        monthCombo.setItems(months);
        weekMonthCombo.setItems(months);

         /** edit out for mandatory login
        login = true;
        userID = 1;
        userName="User";
        loggedIn.setText(userName);
        loggedInID.setText(String.valueOf(userID));
        dbRefresh();
         **/

        //setup lan
        if(Locale.getDefault().getLanguage().contains("fr")) {
            lan = ResourceBundle.getBundle("Nat", Locale.forLanguageTag("FR"));
        }
    }

    /**
     * Sends user login attempt info to login tracker file
     * @param user username
     * @param successful true or false
     */
    public void loginTracker(String user, boolean successful){
        //converts system time to UTC and puts it in timestamp format
        Timestamp timeNow = Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
        String report = "Username: " + user + " | Date and time: " + timeNow + " | Successful: " + String.valueOf(successful) + "\n";
        try {
            byte[] reportByte = report.getBytes();
            Files.write(Path.of("login_activity.txt"), reportByte, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //login and user info functions

    /**
     * Generates login window, fills tables with data and unlocks access once completed
     */
    @FXML
    public void loginWindow() {
        boolean loginSet = false;
        String userAttempt = "";

        while (loginSet == false){
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(mainPage.getScene().getWindow());
            String content = lan.getString("Pleaselogin");
            dialog.setTitle(content);
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("loginPage.fxml"));
            fxmlLoader.setResources(lan);
            //tries loading the dialog pane with the fxml file and gives error if it fails
            try {
                dialog.getDialogPane().setContent(fxmlLoader.load());
            } catch(IOException e) {
                e.printStackTrace();
                return;
            }
            //initializes the ok and cancel buttons for dialog
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
            dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-font-family: Times New Roman bold;");
            loginPage loginController = fxmlLoader.getController();
            loginController.setLocal(ZonedDateTime.now().getZone().getId());

            //tells dialog to open and wait for user input from buttons
            Optional<ButtonType> result = dialog.showAndWait();

            //processes the results of user input if ok is pressed. If cancel is pressed the dialog exits without saving
            while(result.isPresent() && result.get() == ButtonType.OK) {
                loginSet = DBAccess.passwordSearch(loginController.attemptLoginUser(), loginController.attemptLoginPass());
                userAttempt = loginController.attemptLoginUser();
                loginTracker(userAttempt, loginSet);
                if (loginSet==false){
                        genericAlert("Incorrect");
                        result = dialog.showAndWait();
                } else {
                    login = true;
                    userName = userAttempt;
                    loggedIn.setText(userAttempt);
                    int tempID = DBAccess.userIDSearch(userAttempt);
                    if (tempID != 0){
                        userID = tempID;
                        loggedInID.setText(String.valueOf(tempID));
                    } else {
                        System.out.println("User ID Error");
                    }
                    dbRefresh();
                    contactCombo.setItems(contactList);
                    checkLoginTime();
                    return;
                }
            }
            if(result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
        }
    }

    /**
     * Checks the login time against upcoming appointments and displays message if any are within 15 minutes of login
     */
    public void checkLoginTime(){
        LocalDateTime loginTime = LocalDateTime.now();
        LocalDateTime fifteenBefore = loginTime.minusMinutes(15);
        LocalDateTime fifteenAfter = loginTime.plusMinutes(15);
        for(int i=0; i<AppointmentsList.getItems().size(); i++){
            Appointment tempAppt = AppointmentsList.getItems().get(i);
            if (tempAppt.getStart().isAfter(fifteenBefore) && tempAppt.getStart().isBefore(fifteenAfter)){
                String upCome= lan.getString("upCome");
                String apptAlert = upCome+ " ID: " + tempAppt.getId() + " At: " + tempAppt.getStart();
                customAlert(apptAlert);
                return;
            }
        }
        genericAlert("noImme");
    }

    //Report functions

    /**
     * Generates reports on the number of customer items for each category once customer list is clicked
     * @param customer customer to have report generated
     */
    private void generateReport(Customer customer) {
        monthCombo.getSelectionModel().clearSelection();
        typeCombo.getSelectionModel().clearSelection();
        monthCombo.setDisable(false);
        typeCombo.setDisable(false);
        monthReport.setText("");
        typeReport.setText("");
        typemonthReport.setText("");
        tempAssociatedAppts = DBAccess.getAssociatedAppointments(customer.getId());
        ObservableList<String> typeList = FXCollections.observableArrayList();
        for (int i=0; i<tempAssociatedAppts.size() ;i++){
            if (! typeList.contains(tempAssociatedAppts.get(i).getType())){
                typeList.add(tempAssociatedAppts.get(i).getType());
            }
        }
        typeCombo.setItems(typeList);
        monthCombo.getSelectionModel().select(null);
        typeCombo.getSelectionModel().select(null);
    }

    /**
     * Updates customer report once month is selected
     */
    @FXML
    public void monthComboClicked(){
        if (monthCombo.getSelectionModel().getSelectedItem() != null) { //ensures null value not passed to parsecomma
            int monthParsed = parseCommas(monthCombo.getSelectionModel().getSelectedItem());
            int counter = 0;
            for (int i = 0; i < tempAssociatedAppts.size(); i++) {
                if (tempAssociatedAppts.get(i).getStart().getMonthValue() == monthParsed) {
                    counter += 1;
                }
            }
            monthReport.setText(String.valueOf(counter));
        }
        if (monthCombo.getSelectionModel().getSelectedItem() != null && typeCombo.getSelectionModel().getSelectedItem() != null){
            typemonthComboCLicked();
        }
    }

    /**
     * Updates customer report once type is selected
     */
    @FXML
    public void typeComboClicked() {
        if (typeCombo.getSelectionModel().getSelectedItem() != null) {
            String typeCompare = typeCombo.getSelectionModel().getSelectedItem().trim();
            int counter = 0;
            for (int i = 0; i < tempAssociatedAppts.size(); i++) {
                if (tempAssociatedAppts.get(i).getType().trim().equals(typeCompare)) {
                    counter += 1;
                }
            }
            typeReport.setText(String.valueOf(counter));
        }
        if (monthCombo.getSelectionModel().getSelectedItem() != null && typeCombo.getSelectionModel().getSelectedItem() != null){
            typemonthComboCLicked();
        }
    }

    public void typemonthComboCLicked(){
        int monthParsed = parseCommas(monthCombo.getSelectionModel().getSelectedItem());
        String typeCompare = typeCombo.getSelectionModel().getSelectedItem().trim();
        int counter = 0;
        for (int i = 0; i < tempAssociatedAppts.size(); i++) {
            if (tempAssociatedAppts.get(i).getType().trim().equals(typeCompare) && tempAssociatedAppts.get(i).getStart().getMonthValue() == monthParsed) {
                counter += 1;
            }
        }
        typemonthReport.setText(String.valueOf(counter));
    }

    //week month selection

    /**
     * Sets the report for selected contact hours whenever new appointments are displayed with lambda expression
     * @lambda uses lamba expression to go through list of appointments and quickly add total time for each customer
     */
    private void setContactReport(ObservableList<Appointment> contactApptList){
        AtomicInteger minutesSave = new AtomicInteger();
        contactApptList.forEach(Appointment -> minutesSave.addAndGet(Math.toIntExact(Duration.between(Appointment.getStart(), Appointment.getEnd()).toMinutes())));
        int minuteshours = minutesSave.get();

        if (minuteshours>60) {
            contactReportMinutes.setText(String.valueOf(minuteshours%60));
            contactReport.setText(String.valueOf(minuteshours/60));
        } else{
            contactReportMinutes.setText(String.valueOf(minuteshours));
            contactReport.setText("0");
        }
    }

    /**
     * Updates appointment table once Contact is selected
     */
    @FXML
    private void selectContactCombo(){
        if (contactCombo.getSelectionModel().getSelectedItem() != null && login==true) {
            int contact = parseCommas(contactCombo.getSelectionModel().getSelectedItem());
            ObservableList<Appointment> contactApptList = DBAccess.getContactAppointments(contact);
            AppointmentsList.setItems(contactApptList);
            weekMonthCombo.setDisable(true);
            weekSelection.setDisable(true);
            weekMonthCombo.getSelectionModel().clearSelection();
            weekMonth.selectToggle(null);

            //set contact report
            setContactReport(contactApptList);
        }
    }

    /**
     * Clears contact selection and updates values
     */
    @FXML
    private void clearContactCombo(){
        if(login==true) {
            dbRefresh();
        }
    }

    /**
     * Enables month selection
     */
    @FXML
    public void monthClicked() {
        weekMonthCombo.setDisable(false);
        weekSelection.setDisable(true);
    }

    /**
     * Enables week selection
     */
    @FXML
    public void weekClicked(){
        weekMonthCombo.setDisable(true);
        weekSelection.setDisable(false);
    }

    /**
     * Updates appointment table to match month selected
     */
    @FXML
    public void monthSelected() {
        if (weekMonthCombo.getSelectionModel().getSelectedItem() != null && login==true) {
            ObservableList<Appointment> appointmentsToSet = DBAccess.getAllAppointments();
            int selectedMonth = parseCommas(weekMonthCombo.getSelectionModel().getSelectedItem());
            int forLoop = appointmentsToSet.size();

            //filter by month and contact
            if (contactCombo.getSelectionModel().getSelectedItem() != null) {
                int contactFilter = parseCommas(contactCombo.getSelectionModel().getSelectedItem());
                for (int i = forLoop-1; i >= 0; i--) {
                    if (appointmentsToSet.get(i).getStart().getMonthValue() != selectedMonth || appointmentsToSet.get(i).getContact() != contactFilter) {
                        appointmentsToSet.remove(i);
                    }
                }
            }
                //only filter by month
             else{
                    for (int i = forLoop-1; i >= 0; i--) {
                        if (appointmentsToSet.get(i).getStart().getMonthValue() != selectedMonth) {
                            appointmentsToSet.remove(i);
                        }
                    }
                }
            AppointmentsList.setItems(appointmentsToSet);
            setContactReport(appointmentsToSet);
        }
    }

    /**
     * Updates appointment table to match week selected
     */
    @FXML
    public void weekSelected() {
        if (weekSelection.getValue() != null && login==true) {
            ObservableList<Appointment> appointmentsToSet = DBAccess.getAllAppointments();
            LocalDate selectedDate = weekSelection.getValue();
            LocalDate endOfWeek = selectedDate.plusDays(6);
            int forLoop = appointmentsToSet.size();

            //filter by month and contact
            if (contactCombo.getSelectionModel().getSelectedItem() != null) {
                int contactFilter = parseCommas(contactCombo.getSelectionModel().getSelectedItem());
                for (int i = forLoop-1; i >= 0; i--) {
                    if (appointmentsToSet.get(i).getStart().toLocalDate().isAfter(endOfWeek) || appointmentsToSet.get(i).getStart().toLocalDate().isBefore(selectedDate) || appointmentsToSet.get(i).getContact() != contactFilter) {
                        appointmentsToSet.remove(i);
                    }
                }
                //only filter by month
            } else {
                for (int i = forLoop-1; i >= 0; i--) {
                    if (appointmentsToSet.get(i).getStart().toLocalDate().isAfter(endOfWeek) || appointmentsToSet.get(i).getStart().toLocalDate().isBefore(selectedDate)) {
                        appointmentsToSet.remove(i);
                    }
                }
            }
            AppointmentsList.setItems(appointmentsToSet);
            setContactReport(appointmentsToSet);
        }
    }

    //Add, modify, delete

    /**
     * Opens add appointment window
     */
    @FXML
    public void addAppointment() {
        //initializes dialog and gets fxml file
        if (login == true) {
            Dialog<ButtonType> dialog = new Dialog<>();
            String content = lan.getString("AddAppointment");
            dialog.setTitle(content);
            dialog.initOwner(mainPage.getScene().getWindow());
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("addAppointmentDialog.fxml"));
            fxmlLoader.setLocation(getClass().getResource("addAppointmentDialog.fxml"));
            fxmlLoader.setResources(lan);

            //tries loading the dialog pane with the fxml file and gives error if it fails
            try {
                dialog.getDialogPane().setContent(fxmlLoader.load());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            //initializes the ok and cancel buttons for dialog
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
            dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-font-family: Times New Roman bold;");

            //finds data and loads contacts
            AppointmentDialogController AppointmentController = fxmlLoader.getController();
            AppointmentController.boxSet();
            AppointmentController.setUserID(userID);
            AppointmentController.setUserName(userName);

            //tells dialog to open and wait for user input from buttons
            Optional<ButtonType> result = dialog.showAndWait();

            //processes the results of user input if ok is pressed. If cancel is pressed the dialog exits without saving
            boolean completedForm = false;
            while (result.isPresent() && result.get() == ButtonType.OK && completedForm == false) {
                AppointmentDialogController controller = fxmlLoader.getController();
                completedForm = controller.processResults();
                if (completedForm == false) {
                    result = dialog.showAndWait();
                }
                if (result.isPresent() && result.get() != ButtonType.OK){
                    return;
                }
            }
            dbRefresh();
        } else{
            genericAlert("Youmustlogin");
        }
    }

    /**
     * Opens add customer window
     */
    @FXML
    public void addCustomer() {
        if (login == true) {
            //initializes dialog and gets fxml file
            Dialog<ButtonType> dialog = new Dialog<>();
            String content = lan.getString("AddCustomer");
            dialog.setTitle(content);
            dialog.initOwner(mainPage.getScene().getWindow());
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("AddCustomerDialog.fxml"));
            fxmlLoader.setResources(lan);

            //tries loading the dialog pane with the fxml file and gives error if it fails
            try {
                dialog.getDialogPane().setContent(fxmlLoader.load());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            //initializes the ok and cancel buttons for dialog
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
            dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-font-family: Times New Roman bold;");

            CustomerDialogController controller = fxmlLoader.getController();
            controller.setUserName(userName);
            //tells dialog to open and wait for user input from buttons
            Optional<ButtonType> result = dialog.showAndWait();

            //processes the results of user input if ok is pressed. If cancel is pressed the dialog exits without saving
            boolean completedForm = false;
            while (result.isPresent() && result.get() == ButtonType.OK && completedForm == false) {
                completedForm = controller.processResults();
                if (completedForm == false) {
                    result = dialog.showAndWait();
                }
                if (result.isPresent() && result.get() != ButtonType.OK){
                    return;
                }
            }
            dbRefresh();
        } else{
            genericAlert("Youmustlogin");
        }
    }

    /**
     * Opens modify appointment window from selected
     */
    @FXML
    public void modifyAppointment() {
        //takes selected Customer and generates error if none selected
        Appointment selectedAppointment = AppointmentsList.getSelectionModel().getSelectedItem();
        if(selectedAppointment == null) {
            genericAlert("NoAppointmentSelected");
            return;
        }

        //initializes dialog and loads fxml file
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPage.getScene().getWindow());
        String page = lan.getString("ModifyAppointment");
        dialog.setTitle(page);


        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("addAppointmentDialog.fxml"));
        fxmlLoader.setResources(lan);

        //tries loading the dialog pane with the fxml file and gives error if it fails
        try {
           dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        //initializes the ok and cancel buttons for dialog
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-font-family: Times New Roman bold;");

        //finds the new controller and loads the data
        AppointmentDialogController AppointmentController = fxmlLoader.getController();
        AppointmentController.boxSet();
        AppointmentController.editAppointment(selectedAppointment);
        AppointmentController.setUserID(userID);
        AppointmentController.setUserName(userName);

        //tells dialog to open and wait for user input from buttons
        Optional<ButtonType> result = dialog.showAndWait();

        //processes the results of user input if ok is pressed. If cancel is pressed the dialog exits without saving
        boolean completedForm = false;
        while (result.isPresent() && result.get() == ButtonType.OK && completedForm == false) {
            AppointmentDialogController controller = fxmlLoader.getController();
            completedForm = controller.updateAppointment(selectedAppointment);
            if (completedForm == false) {
                result = dialog.showAndWait();
            }
            if (result.isPresent() && result.get() != ButtonType.OK){
                return;
            }
        }
        //de-selects item from Customer list after changes to ensure it will be updated before being used
        dbRefresh();
    }

    /**
     * Opens modify customer window from selected
     */
    @FXML
    public void modifyCustomer() {
        //takes selected Customer and generates error if none selected
        Customer selectedCustomer = CustomersList.getSelectionModel().getSelectedItem();
            if(selectedCustomer == null) {
               genericAlert("NoCustomerSelected");
                return;
            }

        //initializes dialog and loads fxml file
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPage.getScene().getWindow());
        String page = lan.getString("ModifyCustomer");
        dialog.setTitle(page);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("AddCustomerDialog.fxml"));
        fxmlLoader.setResources(lan);

        //tries loading the dialog pane with the fxml file and gives error if it fails
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        //initializes the ok and cancel buttons for dialog
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-font-family: Times New Roman bold;");

        //finds the new controller and loads teh data from the selected Customer
        CustomerDialogController CustomerController = fxmlLoader.getController();
        CustomerController.editCustomer(selectedCustomer);
        CustomerController.setUserName(userName);

        //tells dialog to open and wait for user input from buttons
        Optional<ButtonType> result = dialog.showAndWait();

        //processes the results of user input if ok is pressed. If cancel is pressed the dialog exits without saving
        boolean completedForm = false;
        while (result.isPresent() && result.get() == ButtonType.OK && completedForm == false) {
            CustomerDialogController controller = fxmlLoader.getController();
            completedForm = controller.updateCustomer(selectedCustomer);
            if (completedForm == false) {
                result = dialog.showAndWait();
            }
            if (result.isPresent() && result.get() != ButtonType.OK){
                return;
            }
        }
        dbRefresh();
    }

    /**
     * Deletes selected appointment after prompt
     */
    @FXML
    public void deleteAppointment() {
        //takes selected Appointment and generates error if none selected
        Appointment selectedAppointment = AppointmentsList.getSelectionModel().getSelectedItem();
        if(selectedAppointment == null) {
            genericAlert("NoAppointmentSelected");
            return;
        }

        //initializes the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPage.getScene().getWindow());
        dialog.setTitle("Alert");
        dialog.getDialogPane().setStyle("-fx-font-family: Times New Roman bold;");
        String content = lan.getString("Confirmdel");
        dialog.setContentText(content);

        //initializes the ok and cancel buttons for dialog and restyles them
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
        //dialog.getDialogPane().lookupButton(ButtonType.OK).setAccessibleText("Delete");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-font-family: Times New Roman bold;");

        //tells dialog to open and wait for user input from buttons
        Optional<ButtonType> result = dialog.showAndWait();

        //processes the delete if ok is pressed. If cancel is pressed the dialog exits without saving
        if(result.isPresent() && result.get() == ButtonType.OK) {
            Appointment tempAppt = AppointmentsList.getSelectionModel().getSelectedItem();
            DBAccess.removeAppointment((Appointment) AppointmentsList.getSelectionModel().getSelectedItem());
            String deleted = lan.getString("deleted");
            String apptAlert = deleted + " Appointment ID: " + tempAppt.getId() + " Type: " + tempAppt.getType();
            customAlert(apptAlert);
        }
        //de-selects item from Customer list after changes to ensure it will be updated before being used
        dbRefresh();
    }

    /**
     * Deletes selected customer after prompt
     */
    @FXML
    public void deleteCustomer() {
        //takes selected Customer and generates error if none selected
        Customer selectedCustomer = CustomersList.getSelectionModel().getSelectedItem();
        if(selectedCustomer == null) {
            genericAlert("NoCustomerSelected");
            return;
        }

        //checks for associated Appointments in Customer to be deleted
        if(DBAccess.getAssociatedAppointments(CustomersList.getSelectionModel().getSelectedItem().getId()).size() > 0) {
            genericAlert("deassociate");
            return;
        }

        //initializes the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPage.getScene().getWindow());
        dialog.setTitle("Alert");
        dialog.getDialogPane().setStyle("-fx-font-family: Times New Roman bold;");
        String content = lan.getString("Confirmdel");
        dialog.setContentText(content);

        //initializes the ok and cancel buttons for dialog and restyles them
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setAccessibleText("Delete");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-font-family: Times New Roman bold;");

        //tells dialog to open and wait for user input from buttons
        Optional<ButtonType> result = dialog.showAndWait();

        //processes the delete if ok is pressed. If cancel is pressed the dialog exits without saving
        if(result.isPresent() && result.get() == ButtonType.OK) {
            DBAccess.removeCustomer((Customer) CustomersList.getSelectionModel().getSelectedItem());
        }
        dbRefresh();
    }

    //Generic methods

    /**
     * Takes iD, name pairs from combo boxes and returns selected iD
     * @param text text to be parsed
     */
    public static int parseCommas(String text){
        String[] temp = text.split(",", 2);
        return Integer.parseInt(temp[0].trim());
    }

    /**
     * Generates alert window from code provided
     * @param body body of text to be converted then displayed
     */
    public static void genericAlert(String body){
        ResourceBundle lan = ResourceBundle.getBundle("Nat", Locale.forLanguageTag("EN"));
        if(Locale.getDefault().getLanguage().contains("fr")) {
            lan = ResourceBundle.getBundle("Nat", Locale.forLanguageTag("FR"));
        }
        String actual = lan.getString(body);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
        alert.getDialogPane().setStyle("-fx-font-family: Times New Roman bold;");
        alert.getDialogPane().setContentText(actual);
        alert.showAndWait();
    }

    /**
     * Generates alert window from exact text provided
     * @param actual actual text to be displayed
     */
    public void customAlert(String actual){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-family: Times New Roman bold;");
        alert.getDialogPane().setStyle("-fx-font-family: Times New Roman bold;");
        alert.getDialogPane().setContentText(actual);
        alert.showAndWait();
    }

    /**
     * Refreshes all settings in window and reloads values from database
     */
    public void dbRefresh() {
        //clear selections
        CustomersList.getSelectionModel().clearSelection();
        AppointmentsList.getSelectionModel().clearSelection();

        //clear contact sorting
        contactCombo.getSelectionModel().clearSelection();
        weekMonthCombo.getSelectionModel().clearSelection();
        weekMonth.selectToggle(null);

        //disable combos
        weekMonthCombo.setDisable(true);
        weekSelection.setDisable(true);
        monthCombo.setDisable(true);
        typeCombo.setDisable(true);

        //clear reports
        contactReportMinutes.clear();
        contactReport.clear();
        monthReport.clear();
        typeReport.clear();
        typemonthReport.clear();

        //refresh customers
        CustomersList.setItems(DBAccess.getAllCustomers());

        //Refresh appointments and reset contact report
        ObservableList<Appointment> appointmentsToSet = DBAccess.getAllAppointments();
        AppointmentsList.setItems(appointmentsToSet);
        setContactReport(appointmentsToSet);
    }

}


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import static java.lang.Integer.parseInt;
import java.time.*;

/**
 * Controls the appointment dialog to modify and add appointments
 */
public class AppointmentDialogController {
    //Appointment variable text fields
    @FXML
    private TextField addID;
    @FXML
    private TextField addTitle;
    @FXML
    private TextField addDescription;
    @FXML
    private TextField addLocation;
    @FXML
    private TextField addType;

    //time fields
    @FXML
    private TextField addStartHour;
    @FXML
    private TextField addStartMin;
    @FXML
    private DatePicker addStartDateSelection;
    @FXML
    private TextField duration;

    //Buttons
    @FXML
    private ToggleGroup startAMPM;
    @FXML
    private RadioButton addStartPM;
    @FXML
    private RadioButton addStartAM;

    //Combo boxes
    @FXML
    private ComboBox<String> contactCombo;
    @FXML
    private ComboBox<String> customerCombo;
    @FXML
    private ComboBox<String> userCombo;

    private ObservableList<String> contactList = DBAccess.contactSearch();

    private ObservableList<String> customerList = FXCollections.observableArrayList();

    private ObservableList<String> userList = FXCollections.observableArrayList();

    //methods for login info
    private int userID = 0;

    private String userName = "";

    /**
     * sets userid to maintain login throughout windows
     * @param userID logged in user id
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * sets username to maintain login through windows
     * @param userName logged in username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    //general methods

    /**
     * Sets the correct customers and contacts for their respective boxes and formats them with lambda
     * @lambda goes through each item in list and formats customer id in string with customer name
     */
    public void boxSet(){
        //contacts
        contactCombo.setItems(contactList);
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        customers.addAll(DBAccess.getAllCustomers());
        customers.forEach(customer -> customerList.add(String.valueOf(customer.getId()) + ", " + customer.getName()));
        customerCombo.setItems(customerList);
        //users
        userList.setAll(DBAccess.userSearch());
        userCombo.setItems(userList);
    }

    /**
     * Takes components of a date and combines them into a date which is returned
     * @param inHour hour to be parsed
     * @param inMin min to be parsed
     * @param date date to be parsed
     * @param ampm am or pm selection
     * @return newDateTime
     */
    public LocalDateTime parseDate(String inHour, String inMin, LocalDate date, String ampm) {
        int hour = parseInt(inHour);
        int min = parseInt(inMin);
        if(ampm.contains("PM")){
            if (hour < 12) {
                hour += 12;
            }
        } else if (hour == 12){
            hour = 0;
        }
        LocalTime time = LocalTime.of(hour, min);
        LocalDateTime newDateTime = LocalDateTime.of(date, time);
        return newDateTime;
    }

    /**
     * Takes items from boxes and generates a new appointment
     */
    public boolean processResults() {
        //make sure none are empty
        String title = addTitle.getText().trim();
        String description = addDescription.getText().trim();
        String location = addLocation.getText().trim();
        String type = addType.getText().trim();
        if (type.equals("") || description.equals("") || location.equals("") || title.equals("")){
            Controller.genericAlert("allCategories");
            return false;
        }

        //check contact and customerid boxes
        int contact = 0;
        int customerID = 0;
        int userIDSet = 0;
        try {
            contact = Controller.parseCommas(contactCombo.getSelectionModel().getSelectedItem());
            customerID = Controller.parseCommas(customerCombo.getSelectionModel().getSelectedItem());
            userIDSet = Controller.parseCommas(userCombo.getSelectionModel().getSelectedItem());
        } catch(Exception e) {
            Controller.genericAlert("allCategories");
            return false;
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
          try{
              start = parseDate(addStartHour.getText().trim(), addStartMin.getText().trim(), addStartDateSelection.getValue(), startAMPM.getSelectedToggle().toString());
              end = start.plusMinutes(parseInt(duration.getText().trim()));
          } catch(Exception e){
              Controller.genericAlert("ValidTime");
              return false;
          }

        //check that inside business hours 8am-10pm est
            //converts start and end to UTC
        LocalDateTime startTime = DBAccess.retrieveTimeZones(start);
        LocalDateTime endTime = DBAccess.retrieveTimeZones(end);
            //business hours in UTC with separate dates in case there is issue with overnight conversion
        LocalDateTime openTime = LocalDateTime.of(startTime.toLocalDate(), LocalTime.of(12, 0));
        LocalDateTime openStartEarlyTime = openTime.minusHours(10);
        LocalDateTime closeTime = openTime.plusHours(14);
        System.out.println(start + ", " + end + " | " + startTime + ", " + endTime);
            //checks if starttime is within business hours
        //checks if appt starts at end of previous business day, but goes over hours
        if(startTime.isBefore(openStartEarlyTime) && endTime.isAfter(openStartEarlyTime)){
            Controller.genericAlert("temporal");
            return false;
        }
        //checks if appt starts before business opens
        if(startTime.isAfter(openStartEarlyTime) && startTime.isBefore(openTime)){
            Controller.genericAlert("temporal");
            return false;
        }
        //checks if appt ends after business hours the day appt starts
        if (endTime.isAfter(closeTime)){
            Controller.genericAlert("temporal");
            return false;
        }

          //go through appt list and check overlaps
        ObservableList<Appointment> utdAppts = DBAccess.getAllAppointments();
        for(int i=0; i<utdAppts.size(); i++) {
            if (utdAppts.get(i).getStart().isAfter(start) && utdAppts.get(i).getStart().isBefore(end) && String.valueOf(utdAppts.get(i).getId()) != addID.getText()) {
                Controller.genericAlert("dateOverlap");
                return false;
            }
            if (utdAppts.get(i).getEnd().isAfter(start) && utdAppts.get(i).getEnd().isBefore(end) && String.valueOf(utdAppts.get(i).getId()) != addID.getText()) {
                Controller.genericAlert("dateOverlap");
                return false;
            }
        }
        new Appointment(title, description, location, contact, type, start, end, customerID, userName, userIDSet);
        return true;
        }

    /**
     * Takes the items from the appointment to be modified, and puts them in the correct boxes in the forms
     * @param Appointment appointment to be modified
     */
    public void editAppointment(Appointment Appointment) {
        addID.setText(String.valueOf(Appointment.getId()));
        addTitle.setText(Appointment.getTitle());
        addDescription.setText(Appointment.getDescription());
        addLocation.setText(Appointment.getLocation());
        addType.setText(Appointment.getType());

        //go through contact temp and get full string
        String contactTemp = "";
        for (int i=0; i<contactList.size(); i++) {
            if (Controller.parseCommas(contactList.get(i)) == Appointment.getContact()) {
                contactTemp = contactList.get(i);
            }
        }
        contactCombo.getSelectionModel().select(contactTemp);

        //go through user temp and get full string
        String userTemp = "";
        for (int i=0; i<userList.size(); i++) {
            if (Controller.parseCommas(userList.get(i)) == Appointment.getUserID()) {
                userTemp = userList.get(i);
            }
        }
        userCombo.getSelectionModel().select(userTemp);

        //go through customer temp and get full string
        String customerTemp = "";
        for (int i=0; i<customerList.size(); i++) {
            if (Controller.parseCommas(customerList.get(i)) == Appointment.getCustomerID()) {
                customerTemp = customerList.get(i);
            }
        }
        customerCombo.getSelectionModel().select(String.valueOf(customerTemp));
        addStartDateSelection.setValue(Appointment.getStart().toLocalDate());
        if(Appointment.getStart().getHour() == 12) {
            startAMPM.selectToggle(addStartPM);
            addStartHour.setText(String.valueOf(Appointment.getStart().getHour()));
        }
        else if(Appointment.getStart().getHour() > 12) {
            addStartHour.setText(String.valueOf(Appointment.getStart().getHour() - 12));
            startAMPM.selectToggle(addStartPM);
        }
        else {
            startAMPM.selectToggle(addStartAM);
            addStartHour.setText(String.valueOf(Appointment.getStart().getHour()));
        }
        addStartMin.setText(String.valueOf(Appointment.getStart().getMinute()));
        int minutes = Appointment.getEnd().getHour() * 60 + Appointment.getEnd().getMinute() - Appointment.getStart().getHour() * 60 - Appointment.getStart().getMinute();
        duration.setText(String.valueOf(minutes));
    }

    /**
     * takes the modified appointment values and sends an update request
     * @param Appointment appointment being modified
     */
    public boolean updateAppointment(Appointment Appointment){
        //make sure none are empty
        String title = addTitle.getText().trim();
        String description = addDescription.getText().trim();
        String location = addLocation.getText().trim();
        String type = addType.getText().trim();
        if (type.equals("") || description.equals("") || location.equals("") || title.equals("")){
            Controller.genericAlert("allCategories");
            return false;
        }

        //test contact and customer boxes
        int contact = 0;
        int customer = 0;
        int userIDSet = 0;
        try {
            contact = Controller.parseCommas(contactCombo.getSelectionModel().getSelectedItem());
            customer = Controller.parseCommas(customerCombo.getSelectionModel().getSelectedItem());
            userIDSet = Controller.parseCommas(userCombo.getSelectionModel().getSelectedItem());
        } catch(Exception e) {
            Controller.genericAlert("allCategories");
            return false;
        }

        //start end getters
        LocalDateTime start = null;
        LocalDateTime end = null;
        try{
            start = parseDate(addStartHour.getText().trim(), addStartMin.getText().trim(), addStartDateSelection.getValue(), startAMPM.getSelectedToggle().toString());
            end = start.plusMinutes(parseInt(duration.getText().trim()));
        } catch(Exception e){
            Controller.genericAlert("ValidTime");
            return false;
        }

        //check that inside business hours 8am-10pm est
        //converts start and end to UTC
        LocalDateTime startTime = DBAccess.retrieveTimeZones(start);
        LocalDateTime endTime = DBAccess.retrieveTimeZones(end);
        //business hours in UTC with separate dates in case there is issue with overnight conversion
        LocalDateTime openTime = LocalDateTime.of(startTime.toLocalDate(), LocalTime.of(12, 0));
        LocalDateTime openStartEarlyTime = openTime.minusHours(10);
        LocalDateTime closeTime = openTime.plusHours(14);
        //check time conversion
        //System.out.println(start + ", " + end + " | " + startTime + ", " + endTime);

        //checks if starttime is within business hours
        //checks if appt starts at end of previous business day, but goes over hours
        if(startTime.isBefore(openStartEarlyTime) && endTime.isAfter(openStartEarlyTime)){
            Controller.genericAlert("temporal");
            return false;
        }
        //checks if appt starts before business opens
        if(startTime.isAfter(openStartEarlyTime) && startTime.isBefore(openTime)){
            Controller.genericAlert("temporal");
            return false;
        }
        //checks if appt ends after business hours the day appt starts
        if (endTime.isAfter(closeTime)){
            Controller.genericAlert("temporal");
            return false;
        }

        //go through appt list and check overlaps
        ObservableList<Appointment> utdAppts = DBAccess.getAllAppointments();
        for(int i=0; i<utdAppts.size(); i++) {
            if (utdAppts.get(i).getStart().isAfter(start) && utdAppts.get(i).getStart().isBefore(end)  && String.valueOf(utdAppts.get(i).getId()) != addID.getText().trim()) {
                Controller.genericAlert("dateOverlap");
                return false;
            }
            if (utdAppts.get(i).getEnd().isAfter(start) && utdAppts.get(i).getEnd().isBefore(end)  && String.valueOf(utdAppts.get(i).getId()) != addID.getText().trim()) {
                Controller.genericAlert("dateOverlap");
                return false;
            }
        }

        Appointment.updateAppointment(Appointment, title, description, location, contact, type, start, end, customer, userName, userIDSet);
        return true;
    }

}
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DBAccess {

    //login

    /**
     * Checks provided password and username against database values
     * @param username input username
     * @param password input password
     */
    public static boolean passwordSearch(String username, String password) {

        //password sql string to try
        String passWordsql = "SELECT User_Name, Password from users;";

        try {
            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(passWordsql);
            ResultSet tempData = sqlStatement.executeQuery(passWordsql);

            //check each password username combo to see if user input matches any
            while(tempData.next()) {
                if (tempData.getString(2).equals(password) && tempData.getString(1).equalsIgnoreCase(username)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            return false;
        }

        return false;
    }

    //generics

    /**
     * Used to lookup custom values from database
     * @param sqlText swl text to add
     * @param counting number of values expected
     */
    public static void genericSearch(String sqlText, int counting){

        try {
            //get result of statement as tempdata
            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sqlText);
            ResultSet tempData = sqlStatement.executeQuery(sqlText);

            //go through each item in search for number of columns specified and print each item
            while (tempData.next()) {
                int memo = counting;
                String lined = "";
                //goes through line and prints number of columns indicated
                while (memo >0){
                    lined += tempData.getString(memo) + " ";
                    memo -=1;
                }
                System.out.println(lined);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used for general database updates, must enter full sql string
     * @param sqlText sql text string
     */
    public static void genericAlteration(String sqlText){
        try {
            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sqlText);
            sqlStatement.executeUpdate();
            System.out.println("Added Successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //time zones

    /**
     * Takes date and time from database and converts it to user's time zone
     * @param changeDate date to change
     * @return altDate
     */
    public static LocalDateTime filterTimeZones(Timestamp changeDate){
        //change set date to UTC
        ZonedDateTime zoned = changeDate.toInstant().atZone((ZoneId.of("UTC")));
        //change back to localdatetime adjusted to user timezone
        LocalDateTime altDate = zoned.withZoneSameInstant(ZonedDateTime.now().getZone()).toLocalDateTime();
    return altDate;
    }

    /**
     * Takes time in user time zone and converts it to UTC for the database
     * @param changeDate date to change
     * @return changeDate
     */
    public static LocalDateTime retrieveTimeZones(LocalDateTime changeDate){
        ZonedDateTime zoned = changeDate.atZone(ZonedDateTime.now().getZone());
        changeDate = zoned.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        return changeDate;
    }

    //division, user and contacts

    /**
     * Returns list of divisions for country provided
     * @param division division value for country for lookup
     * @return divisionList
     */
    public static ObservableList<String> divisionSearch(String division){
        String sqlText =  "SELECT Division, Division_ID FROM first_level_divisions WHERE Country_ID='"+ division +"';";
        ObservableList<String> divisionList = FXCollections.observableArrayList();

        try {
            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sqlText);
            ResultSet tempData = sqlStatement.executeQuery(sqlText);
            while (tempData.next()) {
                String lined = tempData.getString(2) + ", " + tempData.getString(1);
                divisionList.add(lined);
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return divisionList;
    }

    /**
     * Returns list of all company contacts
     * @return contactList
     */
    public static ObservableList<String> contactSearch(){
        String sqlText =  "SELECT Contact_Name, Contact_ID FROM contacts;";
        ObservableList<String> contactList = FXCollections.observableArrayList();

        try {
            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sqlText);
            ResultSet tempData = sqlStatement.executeQuery(sqlText);
            while (tempData.next()) {
                String lined = tempData.getString(2) + ", " + tempData.getString(1);
                contactList.add(lined);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contactList;
    }

    /**
     * Returns list of all company users
     * @return userList
     */
    public static ObservableList<String> userSearch(){
        String sqlText =  "SELECT User_Name, User_ID FROM users;";
        ObservableList<String> userList = FXCollections.observableArrayList();

        try {
            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sqlText);
            ResultSet tempData = sqlStatement.executeQuery(sqlText);
            while (tempData.next()) {
                String lined = tempData.getString(2) + ", " + tempData.getString(1);
                userList.add(lined);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Finds user iD for provided username
     * @param username provided username
     * @return Id
     */
    public static int userIDSearch(String username){
        String sql = "SELECT * FROM users WHERE User_Name='" + username + "'";
        int Id = 0;
        try {
            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sql);
            ResultSet tempData = sqlStatement.executeQuery(sql);
            tempData.next();
            Id = tempData.getInt("User_ID");
            //Id = sqlStatement.executeQuery(sql).getFetchSize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Id;
    }

    //add and  modify

    /**
     * Adds customer to database from values provided
     * @param customer customer to add
     * @param userName user adding customer
     */
    public static void addCustomer(Customer customer, String userName) {
        String Name = customer.getName();
        String Address = customer.getAddress();
        String Postal = customer.getPostal();
        String Phone = customer.getPhone();
        String divisionID = String.valueOf(customer.getDivisionID());

        //sql insert text
        String fullString ="'" + Name + "', '" + Address + "', '" + Postal + "', '" + Phone + "', " + divisionID + ", '" + userName + "', '" + userName + "');";
        String sql = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Division_ID, Created_By, Last_Updated_By) VALUES (" + fullString;

        //add customer into database
        genericAlteration(sql);
    }

    /**
     * Finds and modifies customer in database based on values provided
     * @param customer customer to modify
     * @param userName user modifying customer
     */
    public static void modifyCustomer(Customer customer, String userName) {
        String Name = customer.getName();
        String Address = customer.getAddress();
        String Postal = customer.getPostal();
        String Phone = customer.getPhone();
        String division = String.valueOf(customer.getDivisionID());
        int id = customer.getId();

        //sql update text
        String sql = "UPDATE customers SET Customer_Name = '" + Name + "', Address = '" + Address + "', Postal_Code = '" + Postal + "', Phone = '" + Phone + "', Division_ID = " + division + ", Last_Updated_By = '" + userName + "' WHERE Customer_ID = " + id + ";";

        //add customer into database
        genericAlteration(sql);
    }

    /**
     * Adds appointment to database from values provided
     * @param appt appointment to add
     * @param userName user adding appointment, name
     */
    public static void addAppointment(Appointment appt, String userName) {
        String title = appt.getTitle();
        String description = appt.getDescription();
        String location = appt.getLocation();
        String type = appt.getType();
        String start = retrieveTimeZones(appt.getStart()).toString();
        String end = retrieveTimeZones(appt.getEnd()).toString();
        String customerId = String.valueOf(appt.getCustomerID());
        String contactId = String.valueOf(appt.getContact());
        String userID = String.valueOf(appt.getUserID());

        //sql insert text
        String fullString ="'" + title + "', '" + description + "', '" + location + "', '" + type + "', '" + start + "', '" + end + "', " + customerId + ", " + contactId +  ", '" + userName + "', '" + userName + "', " + userID + ");";
        String sql = "INSERT INTO appointments (Title, Description, Location, Type, Start, End, Customer_ID, Contact_ID, Created_By, Last_Updated_By, User_ID) VALUES (" + fullString;

        //add appt into database
        genericAlteration(sql);
    }

    /**
     * Finds and modifies appointment in database based on values provided
     * @param appt appointment being modified
     * @param userName user modifying appointment
     */
    public static void modifyAppointment(Appointment appt, String userName) {

        String title = appt.getTitle();
        String description = appt.getDescription();
        String location = appt.getLocation();
        String type = appt.getType();
        String start = retrieveTimeZones(appt.getStart()).toString();
        String end = retrieveTimeZones(appt.getEnd()).toString();
        String customerId = String.valueOf(appt.getCustomerID());
        String contactId = String.valueOf(appt.getContact());
        String userIDSet = String.valueOf(appt.getUserID());
        int id = appt.getId();
        //sql update text
        String sql = "UPDATE appointments SET Title = '" + title + "', Description = '" + description + "', Location = '" + location + "', Type = '" + type + "', Start = '" + start + "', End = '" + end  + "', Contact_ID = " + contactId + ", Customer_ID = " + customerId + ", Last_Updated_By = '" + userName +  "', User_ID = " + userIDSet + " WHERE Appointment_ID = " + id + ";";

        //add customer into database
        genericAlteration(sql);
    }

    //delete

    /**
     * Finds and deletes provided customer from database
     * @param customer customer to find
     */
    public static void removeCustomer(Customer customer) {
        String id = String.valueOf(customer.getId());
        String sql = "DELETE FROM customers WHERE Customer_ID = " + id + ";";
        genericAlteration(sql);
    }

    /**
     * Finds and deletes provided appointment from database
     * @param appt appointment to find
     */
    public static void removeAppointment(Appointment appt) {
        String id = String.valueOf(appt.getId());
        String sql = "DELETE FROM appointments WHERE Appointment_ID = " + id + ";";
        genericAlteration(sql);
    }

    //get customers

    /**
     * Returns list of all customers in database as customer items
     * @return allCustomers
     */
    public static ObservableList<Customer> getAllCustomers() {

        ObservableList<Customer> allCustomers = FXCollections.observableArrayList();

        int count = 0;
        try {
            String sql = "SELECT * FROM customers";

            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sql);

            ResultSet sqlResult = sqlStatement.executeQuery();
            while (sqlResult.next()) {
                //print number of additions
                count += 1;

                int customerID = sqlResult.getInt("Customer_ID");
                String customerName = sqlResult.getString("Customer_Name");
                String address = sqlResult.getString("Address");
                String postal = sqlResult.getString("Postal_Code");
                String phone = sqlResult.getString("Phone");
                int divisionID = sqlResult.getInt("Division_ID");

                Customer tempCustomer = new Customer(customerID, customerName, address, postal, phone, divisionID);

                //add customer
                allCustomers.add(tempCustomer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(String.valueOf(count) + " Customers Loaded");
        return allCustomers;
    }

    //get appts

    /**
     * Returns list of all appointments in database as appointment items
     * @return allAppts
     */
    public static ObservableList<Appointment> getAllAppointments() {
        ObservableList<Appointment> allAppts = FXCollections.observableArrayList();
        int count = 0 ;
        try {
            String sql = "SELECT * from appointments";

            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sql);

            ResultSet sqlResult = sqlStatement.executeQuery();

            while (sqlResult.next()) {
                int appointmentID = sqlResult.getInt("Appointment_ID");
                String title = sqlResult.getString("Title");
                String description = sqlResult.getString("Description");
                int contact = sqlResult.getInt("Contact_ID");
                String location = sqlResult.getString("Location");
                String type = sqlResult.getString("Type");
                LocalDateTime start = filterTimeZones(sqlResult.getTimestamp("Start"));
                LocalDateTime end = filterTimeZones(sqlResult.getTimestamp("End"));
                int cid = sqlResult.getInt("Customer_ID");
                int userIDSet = sqlResult.getInt("User_ID");

                Appointment tempAppt = new Appointment(appointmentID, title, description, location, contact, type, start, end, cid, userIDSet);

                allAppts.add(tempAppt);
                count +=1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(String.valueOf(count) + " Appointments Loaded");
        return allAppts;
    }

    /**
     * Returns list of all appointments associated with selected customer iD
     * @param id id selected
     * @return allCustomerAppts
     */
    public static ObservableList<Appointment> getAssociatedAppointments(int id) {

        ObservableList<Appointment> allCustomerAppts = FXCollections.observableArrayList();

        try {
            String sql = "SELECT * from appointments WHERE Customer_ID='" + id + "'";

            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sql);

            ResultSet sqlResult = sqlStatement.executeQuery();

            while (sqlResult.next()) {
                int appointmentID = sqlResult.getInt("Appointment_ID");
                String title = sqlResult.getString("Title");
                String description = sqlResult.getString("Description");
                int contact = sqlResult.getInt("Contact_ID");
                String location = sqlResult.getString("Location");
                String type = sqlResult.getString("Type");
                LocalDateTime start = filterTimeZones(sqlResult.getTimestamp("Start"));
                LocalDateTime end = filterTimeZones(sqlResult.getTimestamp("End"));
                int userIDSet = sqlResult.getInt("User_ID");

                Appointment tempAppt = new Appointment(appointmentID, title, description, location, contact, type, start, end, id, userIDSet);

                //add apt
                allCustomerAppts.add(tempAppt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allCustomerAppts;
    }

    /**
     * Returns list of all appointments associated with selected contact iD
     * @param id selected
     * @return allContactAppts
     */
    public static ObservableList<Appointment> getContactAppointments(int id) {

        ObservableList<Appointment> allContactAppts = FXCollections.observableArrayList();

        try {
            String sql = "SELECT * from appointments WHERE Contact_ID='" + id + "'";

            PreparedStatement sqlStatement = DBConnection.getConnection().prepareStatement(sql);

            ResultSet sqlResult = sqlStatement.executeQuery();

            while (sqlResult.next()) {
                int appointmentID = sqlResult.getInt("Appointment_ID");
                String title = sqlResult.getString("Title");
                String description = sqlResult.getString("Description");
                int customer = sqlResult.getInt("Customer_ID");
                String location = sqlResult.getString("Location");
                String type = sqlResult.getString("Type");
                LocalDateTime start = filterTimeZones(sqlResult.getTimestamp("Start"));
                LocalDateTime end = filterTimeZones(sqlResult.getTimestamp("End"));
                int userIDSet = sqlResult.getInt("User_ID");

                Appointment tempAppt = new Appointment(appointmentID, title, description, location, id, type, start, end, customer, userIDSet);

                //add apt
                allContactAppts.add(tempAppt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allContactAppts;
    }

}
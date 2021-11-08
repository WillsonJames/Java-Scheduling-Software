
import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private String title;
    private String description;
    private String location;
    private int contact;
    private String type;
    private LocalDateTime start;
    private LocalDateTime end;
    private int customerID;
    private int userID;

    //constructor

    /**
     * Constructor for appointment that doesn't automatically add to database
     * @param id id
     * @param title title
     * @param description description
     * @param location location
     * @param contact contavt
     * @param type type
     * @param start start
     * @param end end
     * @param customerID customerID
     * @param userID userID
     */
    public Appointment(int id, String title, String description, String location, int contact, String type, LocalDateTime start, LocalDateTime end, int customerID, int userID) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.type = type;
        this.start = start;
        this.end = end;
        this.customerID = customerID;
        this.userID = userID;
    }

    /**
     * Constructor for appointment that automatically adds to database
     * @param title title
     * @param description description
     * @param location location
     * @param contact contact
     * @param type type
     * @param start start
     * @param end end
     * @param customerID customerID
     * @param userName userName
     */
    public Appointment(String title, String description, String location, int contact, String type, LocalDateTime start, LocalDateTime end, int customerID, String userName, int userIDset) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.type = type;
        this.start = start;
        this.end = end;
        this.customerID = customerID;
        this.userID = userIDset;
        DBAccess.addAppointment(this, userName);
    }

    //getters and setters

    /**
     * Getter for iD
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for iD
     * @param id id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for Title
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for Title
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for Description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for Description
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for Location
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter for Location
     * @param location location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter for Contact
     * @return contact
     */
    public int getContact() {
        return contact;
    }

    /**
     * Setter for Contact
     * @param contact contact
     */
    public void setContact(int contact) {
        this.contact = contact;
    }

    /**
     * Getter for Type
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for Type
     * @param type type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for Start
     * @return start
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Setter for Start
     * @param start start
     */
    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    /**
     * Getter for End
     * @return end
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Setter for End
     * @param end end
     */
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    /**
     * Getter for Customer iD
     * @return customerID
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * Setter for Customer iD
     * @param customerID customerID
     */
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    /**
     * Getter for User iD
     * @return userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Setter for User iD
     * @param userID userID
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * Updates all appointment variables and sends to database for modification
     * @param appointment appointment
     * @param title title
     * @param description description
     * @param location location
     * @param contact contact
     * @param type type
     * @param start start
     * @param end end
     * @param customerID customerID
     * @param userName userName
     * @param userID userID
     */
    public void updateAppointment(Appointment appointment, String title, String description, String location, int contact, String type, LocalDateTime start, LocalDateTime end, int customerID, String userName, int userID) {
        appointment.setTitle(title);
        appointment.setDescription(description);
        appointment.setLocation(location);
        appointment.setContact(contact);
        appointment.setType(type);
        appointment.setStart(start);
        appointment.setEnd(end);
        appointment.setCustomerID(customerID);
        appointment.setUserID(userID);

        DBAccess.modifyAppointment(appointment, userName);
    }
}
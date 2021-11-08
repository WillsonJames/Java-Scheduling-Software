

public class Customer {
    private int id;
    private String name;
    private String address;
    private String postal;
    private String phone;
    private int divisionID;

    /**
     * Constructor for customer that does not automatically add to database
     * @param id id
     * @param name name
     * @param address address
     * @param postal postal
     * @param phone phone
     * @param divisionID divisionID
     */
    public Customer(int id, String name, String address, String postal, String phone, int divisionID) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.postal = postal;
        this.phone = phone;
        this.divisionID = divisionID;
    }

    /**
     * Constructor for customer that automatically updates in database
     * @param name name
     * @param address address
     * @param postal postal
     * @param phone phone
     * @param divisionID divisionID
     * @param userName userName
     */
    public Customer(String name, String address, String postal, String phone, int divisionID, String userName) {
        this.name = name;
        this.address = address;
        this.postal = postal;
        this.phone = phone;
        this.divisionID = divisionID;
        DBAccess.addCustomer(this, userName);
    }

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
     * getter for Name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for Name
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for Address
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Setter for Address
     * @param address address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Getter for Postal
     * @return postal
     */
    public String getPostal() {
        return postal;
    }

    /**
     * Setter for Postal
     * @param postal postal
     */
    public void setPostal(String postal) {
        this.postal = postal;
    }

    /**
     * Getter for Phone
     * @return phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Setter for Phone
     * @param phone phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Getter for Division iD
     */
    public int getDivisionID() {
        return divisionID;
    }

    /**
     * Setter for Division iD
     * @param divisionID divisionID
     */
    public void setDivisionID(int divisionID) {
        this.divisionID = divisionID;
    }

    /**
     * Updates all customer variables and sends to database for modification
     * @param Customer customer
     * @param name name
     * @param address address
     * @param postal postal
     * @param phone phone
     * @param divisionID divisionID
     * @param userName userName
     */
    public void updateCustomer(Customer Customer, String name, String address, String postal, String phone, int divisionID, String userName) {
        Customer.setName(name);
        Customer.setAddress(address);
        Customer.setPostal(postal);
        Customer.setPhone(phone);
        Customer.setDivisionID(divisionID);

        DBAccess.modifyCustomer(Customer, userName);
    }
}


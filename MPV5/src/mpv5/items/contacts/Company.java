/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.items.contacts;

/**
 *
 * @author Administrator
 */
public class Company {

    private String name = "";
    private String street = "";
    private String zip = "";
    private String city = "";
    private Contact maincontact = null;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return the zip
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the maincontact
     */
    public Contact getMaincontact() {
        return maincontact;
    }

    /**
     * @param maincontact the maincontact to set
     */
    public void setMaincontact(Contact maincontact) {
        this.maincontact = maincontact;
    }

    @Override
    public String toString() {
        return name;
    }
}

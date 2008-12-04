/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.items.contacts;

/**
 *
 * @author Administrator
 */
public class Roles {

    private boolean Customer = false;
    private boolean Supplier = false;
    private boolean Manufacturer = false;

    /**
     * @return the Customer
     */
    public boolean isCustomer() {
        return Customer;
    }

    /**
     * @param Customer the Customer to set
     */
    public void setCustomer(boolean Customer) {
        this.Customer = Customer;
    }

    /**
     * @return the Supplier
     */
    public boolean isSupplier() {
        return Supplier;
    }

    /**
     * @param Supplier the Supplier to set
     */
    public void setSupplier(boolean Supplier) {
        this.Supplier = Supplier;
    }

    /**
     * @return the Manufacturer
     */
    public boolean isManufacturer() {
        return Manufacturer;
    }

    /**
     * @param Manufacturer the Manufacturer to set
     */
    public void setManufacturer(boolean Manufacturer) {
        this.Manufacturer = Manufacturer;
    }
}

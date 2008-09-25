/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */

package mp4.utils.export.textdatei;

/**
 *
 * @author anti43
 */
public class VCard{
  private String name = "";
    private String nName = "";
    private String comment = "";
    private String title = "";
    private String organisation = "";
    private String address = "";
    private String phone = "";
    private String fax = "";
    private String mobile = "";
    private String email = "";


  /**
   *  This function returns a String containing the VCard.
   * @return 
   */
  public String getVCard() {

    String vCard = "BEGIN:VCARD\n"
      + "FN:" + getName() + "\n"
      + "N:" + getNName() + "\n"   
      + "NOTE;ENCODING=QUOTED-PRINTABLE:" + getComment() + "\n"   
      + "TITLE:" + getTitle() + "\n"
      + "ORG:" + getOrganisation() + "\n"
      + "ADR;POSTAL;WORK:;" + getAddress() + "\n"
      + "TEL;Work:" + getPhone() + "\n"
      + "TEL;Fax:" + getFax() + "\n"
      + "TEL;Cell:" + getMobile() + "\n"
      + "EMAIL;Internet:" + getEmail() + "\n"
      + "EMAIL;Internet:" + getEmail() + "\n"
      + "VERSION:2.1\n"
      + "END:VCARD\n";

    return vCard;
  }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNName() {
        return nName;
    }

    public void setNName(String nName) {
        this.nName = nName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

  
}



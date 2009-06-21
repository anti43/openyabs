/*
 *  This file is part of MP.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.models;

import java.util.Date;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.utils.date.DateConverter;

/**
 * A DateSelectorModel is a DefaultComboBoxModel for a List
 * of months, quarters and the year.
 * @author hnauheim
 */
public class DateSelectorModel extends DefaultComboBoxModel {

  private ComboBoxModel model;
  private Object[][] boxData;
  private int idx;

  /**
   * Creates a DateSelectorModel
   */
  public DateSelectorModel() {
    boxData = new Object[17][2];
    idx = Integer.parseInt(DateConverter.getMonth());
    setElements(DateConverter.getYear());
  }

  /**
   * Get the correct ComboBoxModel
   * @return a ComboBoxModel
   */
  public ComboBoxModel getModel() {
    return model;
  }

  /**
   * Returns the start day of the selected date object
   * @return the start date as SQLDate formatted String
   */
  public String getStartDay() {
    return DateConverter.getSQLDateString(((StartEndDays) getSelectedItem()).getStartDay());
  }

  /**
   * Returns the end day of the selected date object
   * @return the end date as SQLDate formatted String
   */
  public String getEndDay() {
    return DateConverter.getSQLDateString(((StartEndDays) getSelectedItem()).getEndDay());
  }

  /**
   * @param year the year to set
   */
  public void setYear(String year) {
    getSelectedItem();
    setElements(year);
  }

  @Override
  public Object getSelectedItem() {
    StartEndDays sed = (StartEndDays) ((MPComboBoxModelItem) model.getSelectedItem()).getIdObject();
    idx = MPComboBoxModelItem.getItemID(sed, model);
    return sed;
  }

  /**
   * Fills the model with date objects (months, quarters, year)
   * @param year the year value for the date objects as a String (e.g. "1999")
   */
  private void setElements(String year) {
    Date d = DateConverter.getDate(year + "-01-01");
    StartEndDays sed = new StartEndDays(d, StartEndDays.YEAR_MODE);
    boxData[0][0] = sed;
    boxData[0][1] = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Bundle").getString("entire_Year");
    Date dd = d;
    for (int i = 1; i < 13; i++) {
      sed = new StartEndDays(dd, StartEndDays.MONTH_MODE);
      boxData[i][0] = sed;
      boxData[i][1] = DateConverter.getMonthName(sed.getStartDay());
      dd = DateConverter.addMonth(dd);
    }
    for (int i = 13; i < 17; i++) {
      sed = new StartEndDays(d, StartEndDays.QUARTER_MODE);
      boxData[i][0] = sed;
      boxData[i][1] = (i - 12) + java.util.ResourceBundle.getBundle("mpv5/resources/languages/Bundle").getString("._Quarter");
      d = DateConverter.addQuarter(d);
    }
    model = MPComboBoxModelItem.toModel(boxData);
    MPComboBoxModelItem mcm = (MPComboBoxModelItem) model.getElementAt(idx);
    model.setSelectedItem(mcm);
  }
}

/** A StartEndDay object holds a date object of a given start day and a
 * date object of the end day.
 */
// <editor-fold defaultstate="collapsed" desc="class StartEndDays">
class StartEndDays {

  public static final int MONTH_MODE = 1;
  public static final int QUARTER_MODE = 3;
  public static final int YEAR_MODE = 12;
  private Date startDay;
  private Date endDay;

  /**
   * A StartEndDay object contains a date object of a given start day and a
   * date object of the end day. The end date will be calculated by the use
   * of a given modus.
   * @param startDay the start day
   * @param modus allowed modi:<br />
   * MONTH_MODE (adds 1 month to the start date<br />
   * QUARTER_MODE (adds 3 months to the start date<br />
   * YEAR_MODE (adds 1 year to the start date<br />
   */
  public StartEndDays(Date startDay, int modus) {
    this.startDay = startDay;
    switch (modus) {
      case 1:
        endDay = DateConverter.addMonth(startDay);
        break;
      case 3:
        endDay = DateConverter.addQuarter(startDay);
        break;
      case 12:
        endDay = DateConverter.addYear(startDay);
        break;
      default:
        endDay = startDay;
    }
  }

  /**
   * @return the startDay
   */
  public Date getStartDay() {
    return startDay;
  }

  /**
   * @return the calculated endDay
   */
  public Date getEndDay() {
    return endDay;
  }
}
// </editor-fold>

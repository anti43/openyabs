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

  /**
   * Creates a DateSelectorModel
   */
  public DateSelectorModel() {
    boxData = new Object[17][2];
    initElements();
  }

  /**
   * Get the correct ComboBoxModel
   * @return a ComboBoxModel
   */
  public ComboBoxModel getModel() {
    return model;
  }

  /**
   * Fills the model with date objects (months, quarters, year).
   */
  private void initElements() {
    String year = DateConverter.getYear();
    Date d = DateConverter.getDate(year);
    for (int i = 0; i < boxData.length; i++) {
      boxData[i][0] = "";
    }
    boxData[0][1] = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels").getString("entire_Year");
    for (int i = 1; i < 13; i++) {
      boxData[i][1] = DateConverter.getMonthName(d);
      d = DateConverter.addMonth(d);
    }
    for (int i = 13; i < 17; i++) {
      boxData[i][1] = (i - 12) + java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels").getString("._Quarter");
    }
    addDate(year);
    model = MPComboBoxModelItem.toModel(boxData);
    sortDate();
    int idx = Integer.parseInt(DateConverter.getMonth());
    MPComboBoxModelItem mcm = (MPComboBoxModelItem) model.getElementAt(idx);
    model.setSelectedItem(mcm);
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
   * Returns the selected element
   * @return a StartEndDay object
   */
  @Override
  public Object getSelectedItem() {
    StartEndDays sed = (StartEndDays) ((MPComboBoxModelItem) model.getSelectedItem()).getIdObject();
    return sed;
  }

  /**
   * Change the year of the date objects in the model
   * @param year the year value for the date objects as a String (e.g. "1999")
   */
  public void setYear(String year) {
    addDate(year);
    for (int i = 0; i < model.getSize(); i++) {
      ((MPComboBoxModelItem) model.getElementAt(i)).setId(boxData[i][0]);
    }
  }

  private void addDate(String year) {
    Date d = DateConverter.getDate(year + "-01-01");
    boxData[0][0] = new StartEndDays(d, StartEndDays.YEAR_MODE);
    Date dd = d;
    for (int i = 1; i < 13; i++) {
      boxData[i][0] = new StartEndDays(dd, StartEndDays.MONTH_MODE);
      dd = DateConverter.addMonth(dd);
    }
    for (int i = 13; i < 17; i++) {
      boxData[i][0] = new StartEndDays(d, StartEndDays.QUARTER_MODE);
      d = DateConverter.addQuarter(d);
    }
  }

  private void sortDate() {
    for (int i = 0; i < model.getSize(); i++) {
      ((MPComboBoxModelItem) model.getElementAt(i)).setCompareMode(MPComboBoxModelItem.COMPARE_BY_ID);
    }
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

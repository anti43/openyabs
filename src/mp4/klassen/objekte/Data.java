/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp4.klassen.objekte;

import mp4.klassen.objekte.Rechnung;
import mp3.database.util.Query;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;

/**
 *
 * @author anti43
 */
public class Data extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    private String[][] years;
    private String[][] ausgaben;
    private String[][] einnahmen;
    private String[][] rechnungen;

    public Data(Query query) {
        super(query.clone(TABLE_BILLS));


        Query q = mp3.classes.layer.QueryClass.instanceOf().clone(TABLE_DUES);
        ausgaben = q.select("preis, tax, datum", null);

        q = mp3.classes.layer.QueryClass.instanceOf().clone(TABLE_INCOME);
        einnahmen = q.select("preis, tax, datum", null);


        rechnungen = new Rechnung(q).getPaidNaked();

        years = Formater.merge(ausgaben, einnahmen);
        years = Formater.merge(years, rechnungen);

        einnahmen = Formater.merge(einnahmen, rechnungen);
    }

    @Override
    public void save() {
    }

    public String fetchUmsatzInMonth(String month, String year) {


        Double money = 0d;

        for (int i = 0; i < years.length; i++) {

            if (years[i][2].split("\\.")[1].matches(month) && years[i][2].split("\\.")[2].matches(year)) {

                try {
                    money = money + (Double.valueOf(years[i][0]));

                } catch (NumberFormatException numberFormatException) {
                }

            }
        }

        return Formater.formatMoney(money);
    }

    /**
     * 
     * @param month
     * @param year
     * @return
     */
    public Double fetchGewinnInMonth(String month, String year) {

        month = monthReplace(month);

        Double money = 0d;

        for (int i = 0; i < einnahmen.length; i++) {

            if (einnahmen[i][2].split("\\.")[1].matches(month) && einnahmen[i][2].split("\\.")[2].matches(year)) {

                try {
                    money = money + (Double.valueOf(einnahmen[i][0]));
                } catch (NumberFormatException numberFormatException) {
                    Log.Debug(numberFormatException);
                }

            }
        }

        for (int i = 0; i < ausgaben.length; i++) {

            if (ausgaben[i][2].split("\\.")[1].matches(month) && ausgaben[i][2].split("\\.")[2].matches(year)) {

                try {
                    money = money - (Double.valueOf(ausgaben[i][0]));
                  
                } catch (NumberFormatException numberFormatException) {
                    Log.Debug(numberFormatException);
                }

            }
        }



        return Formater.formatDecimalDouble(money);
    }

    public Double fetchUmsatzInMonth2(String month, String year) {

        if (month.matches("Jan")) {
            month = "01";
        }
        if (month.matches("Feb")) {
            month = "02";
        }
        if (month.matches("Mar")) {
            month = "03";
        }
        if (month.matches("Apr")) {
            month = "04";
        }
        if (month.matches("Mai")) {
            month = "05";
        }
        if (month.matches("Jun")) {
            month = "06";
        }
        if (month.matches("Jul")) {
            month = "07";
        }
        if (month.matches("Aug")) {
            month = "08";
        }
        if (month.matches("Sep")) {
            month = "09";
        }
        if (month.matches("Okt")) {
            month = "10";
        }
        if (month.matches("Nov")) {
            month = "11";
        }
        if (month.matches("Dez")) {
            month = "12";
        }




        Double money = 0d;

        for (int i = 0; i < years.length; i++) {

            if (years[i][2].split("\\.")[1].matches(month) && years[i][2].split("\\.")[2].matches(year)) {

                try {
                    money = money + (Double.valueOf(years[i][0]));
                } catch (NumberFormatException numberFormatException) {
                }

            }
        }

        return Formater.formatDecimalDouble(money);
    }

    public String fetchUmsatzInYear(String year) {


        Double money = 0d;

        for (int i = 0; i < years.length; i++) {

            if (years[i][2].split("\\.")[2].matches(year)) {

                try {
                    money = money + (Double.valueOf(years[i][0]));
                } catch (NumberFormatException numberFormatException) {
                }


            }
        }

        return Formater.formatMoney(money);
    }

    private String monthReplace(String month) {
        if (month.matches("Jan")) {
            month = "01";
        }
        if (month.matches("Feb")) {
            month = "02";
        }
        if (month.matches("Mar")) {
            month = "03";
        }
        if (month.matches("Apr")) {
            month = "04";
        }
        if (month.matches("Mai")) {
            month = "05";
        }
        if (month.matches("Jun")) {
            month = "06";
        }
        if (month.matches("Jul")) {
            month = "07";
        }
        if (month.matches("Aug")) {
            month = "08";
        }
        if (month.matches("Sep")) {
            month = "09";
        }
        if (month.matches("Okt")) {
            month = "10";
        }
        if (month.matches("Nov")) {
            month = "11";
        }
        if (month.matches("Dez")) {
            month = "12";
        }

        return month;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mp4.utils.importe.daten;


import compat.mp3.Bill;
import compat.mp3.Customer;
import mp4.items.Rechnung;
import mp4.items.RechnungPosten;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class MP3DatenImport {
   
    private static Customer cust;
    private static Bill bills;

    
    public static void start(){

        cust = new compat.mp3.Customer(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL));
        bills = new compat.mp3.Bill(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL));

        String[][] data = cust.select("*", null, null, false);

        for (int i = 0; i < data.length; i++) {
            
            String[] singleVals = data[i];
            
            mp4.items.Customer kunde = new mp4.items.Customer();
            kunde.LEGACYexplode(singleVals);
            kunde.save();
            
             String[][] bdata = bills.select("id", "kundenid", kunde.getid(), true);
             
             for (int j = 0; j < bdata.length; j++) {
                String[] bsingleVals = bdata[j];
                compat.mp3.Bill bill = new compat.mp3.Bill(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL), bsingleVals[0]);
           
                Rechnung rechnung = new Rechnung();
                rechnung.setKundenId(kunde.getId());
                rechnung.setRechnungnummer(bill.getRechnungnummer());
                rechnung.setDatum(DateConverter.getDate(bill.getDatum()));
                rechnung.setBezahlt(bill.isBezahlt());
                rechnung.setStorno(bill.isStorno());
                rechnung.setGesamtpreis(Double.valueOf(bill.getGesamtpreis()));
                rechnung.setGesamttax(Double.valueOf(bill.getGesamttax()));
                rechnung.save();
                 
     /*** 
     * @return id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis
     *java.lang.Integer.class, java.lang.Double.class, java.lang.String.class,
      java.lang.Double.class, java.lang.Double.class, java.lang.Double.class};*/      
                Object[][] postendta =  bill.getProductlistAsArray();
                
                 for (int k = 0; k < postendta.length; k++) {
                     Object[] objects = postendta[k];
                     RechnungPosten p = new RechnungPosten();
                     
                     p.setRechnungid(rechnung.getId());
                     p.setAnzahl(Double.valueOf(objects[1].toString()));
                     p.setPosten(objects[2].toString());
                     p.setSteuersatz(Double.valueOf(objects[3].toString()));
                     p.setPreis(Double.valueOf(objects[4].toString()));
                     p.save();
                 }
             }
        }
    }
    
}

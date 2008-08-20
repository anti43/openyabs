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
package mp4.statistik;

/**
 *
 * @author Andreas
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * Die Klasse dient zur Erstellung von Bilddateien im png-Format. 
 * Es ist möglich verschiedene Diagrammtypen wie z.B. Kuchendiagramme oder
 * Balkendiagramme zu erzeugen. Dies geschieht unter der Verwendung einer 
 * Drittanbieterbibliothek mit dem Namen JFreeChart. 
 * JFreeChart steht unter der GNU Lesser General Public Licence (LGPL). 
 * 
 * @author Nicolas Amringer
 * @version 1.5
 */
public class Diagramme {

    private JPanel panel;

    public Diagramme(JPanel panel) {
        this.panel = panel;
    }

    /**
     * Erzeugt eine Bilddatei im png-Format mit dem entsprechendem Kuchendiagramm.
     * 
     * @param stDateiPfad Ablagestelle für die erzeugte Bilddatei.
     * @param vecStTitel Titel für die Kuchenstücke.
     * @param vecOWerte Werte für die Kuchenstücke.
     * @param stEinheit Einheit der Werte.
     * @param stDiaTitel Der Titel für das Diagramm.
     * @param nfBeschrFormat Format für die Beschriftung der Kuchenstücke.
     */
    public void erzeugeKuchenGrafik(String stDateiPfad, Vector<String> vecStTitel, Vector<Object> vecOWerte,
            String stEinheit, String stDiaTitel, NumberFormat nfBeschrFormat) {
        DefaultPieDataset dpdDaten = setzeDaten(vecStTitel, vecOWerte);
        JFreeChart jfcKuchen = ChartFactory.createPieChart3D(stDiaTitel, dpdDaten, true, true, false);

        jfcKuchen.setBackgroundPaint(Color.WHITE);
        jfcKuchen.setAntiAlias(true);
        jfcKuchen.setTextAntiAlias(true);

        LegendTitle ltTitel = (LegendTitle) jfcKuchen.getLegend();
        ltTitel.setPosition(RectangleEdge.BOTTOM);

        PiePlot ppPlotter = (PiePlot) jfcKuchen.getPlot();
        ppPlotter.setSectionOutlinesVisible(false);
        ppPlotter.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        ppPlotter.setForegroundAlpha(0.5F);

        ppPlotter.setCircular(true);
        ppPlotter.setLabelGap(0.07);

        ((PiePlot) jfcKuchen.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1}" + stEinheit, nfBeschrFormat, NumberFormat.getInstance()));

        panel.add(new ChartPanel(jfcKuchen), BorderLayout.CENTER);
        
        
//        try {
////            ChartUtilities.saveChartAsPNG(new File(stDateiPfad), jfcKuchen, 500, 400);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            // e.printStackTrace();
//        }
    }

    /**
     * Erzeugt eine Bilddatei im png-Format mit dem entsprechendem Balkendiagramm.
     * 
     * @param stDateiPfad Ablagestelle für die erzeugte Bilddatei.
     * @param vecStTitel Titel für die Balken.
     * @param vecDWerte Werte für die Balken.
     * @param stEinheit Einheit der Werte.
     * @param stDiaTitel Der Titel für das Diagramm.
     * @param stAbszisse Beschriftung für die x-Achse.
     * @param stOrdinate Beschriftung für die y-Achse.
     * @param nfBeschrFormat Format für die Beschriftung der Balken.
     * @exception IOException
     */
    public void erzeugeBalkenGrafik(String stDateiPfad, Vector<String> vecStTitel, Vector<Double> vecDWerte,
            String stEinheit, String stDiaTitel, String stAbszisse, String stOrdinate, NumberFormat nfBeschrFormat) {
        DefaultCategoryDataset dcdDaten = setzeDaten(vecStTitel, vecDWerte);
        JFreeChart jfcBalken = ChartFactory.createBarChart3D(stDiaTitel, stAbszisse, stOrdinate,
                dcdDaten, PlotOrientation.VERTICAL, true, true, false);

        jfcBalken.setBackgroundPaint(Color.WHITE);
        jfcBalken.setAntiAlias(true);
        jfcBalken.setTextAntiAlias(true);

        LegendTitle ltTitel = (LegendTitle) jfcBalken.getLegend();
        ltTitel.setPosition(RectangleEdge.BOTTOM);

        CategoryPlot cpPlotter = jfcBalken.getCategoryPlot();
        cpPlotter.setForegroundAlpha(0.7F);
        cpPlotter.setBackgroundPaint(Color.lightGray);
        cpPlotter.setDomainGridlinePaint(Color.white);
        cpPlotter.setDomainGridlinesVisible(true);
        cpPlotter.setRangeGridlinePaint(Color.white);

        BarRenderer brRenderer = (BarRenderer) cpPlotter.getRenderer();
        brRenderer.setDrawBarOutline(false);

        CategoryItemRenderer ciRenderer = cpPlotter.getRenderer();
        ciRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}" + stEinheit, nfBeschrFormat));
        ciRenderer.setBaseItemLabelsVisible(true);
        cpPlotter.setRenderer(ciRenderer);

        panel.add(new ChartPanel(jfcBalken), BorderLayout.CENTER);
        
//        try {
//            ChartUtilities.saveChartAsPNG(new File(stDateiPfad), jfcBalken, 500, 400);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            // e.printStackTrace();
//        }
    }

    /**
     * Erzeugt eine Bilddatei im png-Format mit dem entsprechendem Liniendiagramm.
     * 
     * @param stDateiPfad Ablagestelle für die erzeugte Bilddatei.
     * @param vecDatWerte Datumswerte für die x-Achse.
     * @param vecDWerte Werte für die y-Achse.
     * @param stEinheit Die Einheit der Werte.
     * @param stDiaTitel Der Titel für das Diagramm.
     * @param stAbszisse Beschriftung für die x-Achse.
     * @param stOrdinate Beschriftung für die y-Achse.
     * @param stLinienName Beschriftung für die Linie.
     * @param nfBeschrFormat Format für die Beschriftung der XY-Punkte.
     * @exception IOException
     */
    public void erzeugeLinienGrafik(String stDateiPfad, Vector<Date> vecDatWerte, Vector<Double> vecDWerte,
            String stEinheit, String stDiaTitel, String stAbszisse, String stOrdinate, String stLinienName, NumberFormat nfBeschrFormat) {
        XYDataset xyDaten = setzeDaten(vecDatWerte, vecDWerte, stLinienName);

        JFreeChart jfcLinien = ChartFactory.createTimeSeriesChart(stDiaTitel, stAbszisse, stOrdinate,
                xyDaten, true, true, false);

        jfcLinien.setBackgroundPaint(Color.WHITE);
        jfcLinien.setAntiAlias(true);
        jfcLinien.setTextAntiAlias(true);

        LegendTitle ltTitel = (LegendTitle) jfcLinien.getLegend();
        ltTitel.setPosition(RectangleEdge.BOTTOM);

        XYPlot xypPlotter = (XYPlot) jfcLinien.getPlot();
        xypPlotter.setBackgroundPaint(Color.LIGHT_GRAY);
        xypPlotter.setDomainGridlinePaint(Color.WHITE);
        xypPlotter.setRangeGridlinePaint(Color.WHITE);
        xypPlotter.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        xypPlotter.setDomainCrosshairVisible(true);
        xypPlotter.setRangeCrosshairVisible(true);

        XYItemRenderer xyiRenderer = xypPlotter.getRenderer();

        DateFormat dfFormat = new SimpleDateFormat("dd.MM.yyyy");

        XYLineAndShapeRenderer xylRenderer = (XYLineAndShapeRenderer) xyiRenderer;
        xylRenderer.setBaseShapesVisible(true);
        xylRenderer.setBaseShapesFilled(true);
        xylRenderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator("{2}" + stEinheit, dfFormat, nfBeschrFormat));
        xylRenderer.setBaseItemLabelsVisible(true);

        DateAxis daAchse = (DateAxis) xypPlotter.getDomainAxis();
        daAchse.setDateFormatOverride(new SimpleDateFormat("MM / yyyy"));
        daAchse.setMinimumDate(new Date(vecDatWerte.get(0).getTime() - 2629800000L));
        daAchse.setMaximumDate(new Date(vecDatWerte.lastElement().getTime() + 2629800000L));

        panel.add(new ChartPanel(jfcLinien), BorderLayout.CENTER);
        
//        try {
//            ChartUtilities.saveChartAsPNG(new File(stDateiPfad), jfcLinien, 500, 400);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            // e.printStackTrace();
//        }
    }

    /**
     * Gruppiert die Daten für das Kuchendiagramm.
     * 
     * @param vecStTitel Titel für die Kuchenstücke.
     * @param vecIWerte Werte für die Kuchenstücke.
     * @return Gruppierte Daten.
     * @exception ClassCastException Tritt auf, wenn die Werte nicht vom Typ Double oder Integer sind.
     */
    private DefaultPieDataset setzeDaten(Vector<String> vecStTitel, Vector<Object> vecOWerte) {
        DefaultPieDataset dpdDaten = new DefaultPieDataset();

        for (int i = 0; i < vecStTitel.size(); i++) {
            if (vecOWerte.get(i) instanceof Double) {
                dpdDaten.setValue(vecStTitel.get(i), (Double) vecOWerte.get(i));
            } else if (vecOWerte.get(i) instanceof Integer) {
                dpdDaten.setValue(vecStTitel.get(i), (Integer) vecOWerte.get(i));
            } else {
                throw new ClassCastException(this.getClass().getName() + ": Die Werte müssen vom Typ Integer oder Double sein");
            }
        }
        return dpdDaten;
    }

    /**
     * Gruppiert die Daten für das Balkendiagramm.
     * 
     * @param vecStTitel Titel für die Balken.
     * @param vecDWerte Werte für die Balken.
     * @return Gruppierte Daten.
     */
    private DefaultCategoryDataset setzeDaten(Vector<String> vecStTitel, Vector<Double> vecDWerte) {
        DefaultCategoryDataset dcdDaten = new DefaultCategoryDataset();

        for (int i = 0; i < vecStTitel.size(); i++) {
            dcdDaten.addValue((double) vecDWerte.get(i), vecStTitel.get(i), "");
        }
        return dcdDaten;
    }

    /**
     * Gruppiert die Daten für das Liniendiagramm.
     * 
     * @param vecDatWerte Datumswerte für die x-Achse.
     * @param vecDWerte Werte für die y-Achse.
     * @return Gruppierte Daten.
     */
    private static XYDataset setzeDaten(Vector<Date> vecDatWerte, Vector<Double> vecDWerte,
            String stLinienName) {
        TimeSeries tsZuordnung = new TimeSeries(stLinienName, Month.class);

        for (int i = 0; i < vecDatWerte.size(); i++) {
//            DateFormat dfMonat = new SimpleDateFormat("MM");
//            int iMonat = Integer.parseInt(dfMonat.format((Date) vecDatWerte.get(i)));
//
//            DateFormat dfJahr = new SimpleDateFormat("yyyy");
//            int iJahr = Integer.parseInt(dfJahr.format((Date) vecDatWerte.get(i)));

            tsZuordnung.add(new Month((Date) vecDatWerte.get(i)), vecDWerte.get(i));
        }

        TimeSeriesCollection xyDaten = new TimeSeriesCollection();
        xyDaten.addSeries(tsZuordnung);

        return xyDaten;
    }
}

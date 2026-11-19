/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.JournalCalc.Totals;
import mpv5.logging.Log;
import mpv5.ui.beans.TimeframeChooser;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.modern.ModernTheme;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.numberformat.FormatNumber;

/**
 * The analysis of the journal: offers, invoiced, paid and expenses per month,
 * quarter or year as bars with the surplus as line, key figures (conversion,
 * days to payment, open receivables) and the top customers. Net amounts;
 * offers and invoices by document date, payments and expenses by payment
 * date. It has its own period.
 */
public class JournalChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    static final int MONTH = 0;
    static final int QUARTER = 1;
    static final int YEAR = 2;

    private final TimeframeChooser time = new TimeframeChooser();
    private final ButtonGroup chipGroup = new ButtonGroup();
    private final JComboBox<String> per = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{
        Messages.JOURNAL_CHART_MONTH.toString(), Messages.JOURNAL_CHART_QUARTER.toString(), Messages.JOURNAL_CHART_YEAR.toString()}));
    private final JLabel state = new JLabel(" ");
    private final JPanel kpis = new JPanel(new GridLayout(1, 0, ModernTheme.px(10), 0));
    private final Chart chart = new Chart();
    private final TopList top = new TopList();
    private boolean loaded;
    private int generation;

    public JournalChartPanel() {
        super(new BorderLayout(0, ModernTheme.px(10)));
        setOpaque(false);
        for (Component c : time.getComponents()) {
            if (c instanceof JLabel) {
                c.setVisible(false);
            }
        }
        time.setOpaque(false);
        time.setPreferredSize(new Dimension(ModernTheme.px(250), time.getPreferredSize().height));
        time.setTime(range(0, LocalDate.now()));
        ModernTheme.roomyCombo(per);
        per.addActionListener(e -> reload());

        JPanel line = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(2)));
        line.setOpaque(false);
        line.add(ModernTheme.fieldLabel(Messages.JOURNAL_PERIOD.toString()));
        line.add(time);
        String[] chips = {Messages.JOURNAL_YEAR.toString(), Messages.JOURNAL_LAST_YEAR.toString(),
            Messages.JOURNAL_LAST_12_MONTHS.toString(), Messages.JOURNAL_LAST_3_YEARS.toString()};
        for (int i = 0; i < chips.length; i++) {
            final int preset = i;
            ChipToggle chip = new ChipToggle(chips[i]);
            chip.setSelected(i == 0);
            chip.addActionListener(e -> {
                time.setTime(range(preset, LocalDate.now()));
                if (preset == 3) {
                    per.setSelectedIndex(QUARTER);
                }
                reload();
            });
            chipGroup.add(chip);
            line.add(chip);
        }
        line.add(javax.swing.Box.createHorizontalStrut(ModernTheme.px(10)));
        line.add(ModernTheme.fieldLabel(Messages.JOURNAL_CHART_BY.toString()));
        line.add(per);
        javax.swing.JButton refresh = new mpv5.ui.modern.PrimaryButton(Messages.JOURNAL_RELOAD.toString(), null);
        refresh.addActionListener(e -> reload());
        JPanel bar = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        bar.setOpaque(false);
        bar.add(line, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        right.setOpaque(false);
        state.setForeground(ModernTheme.muted());
        right.add(state);
        right.add(refresh);
        bar.add(right, BorderLayout.EAST);

        kpis.setOpaque(false);
        JPanel north = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        north.setOpaque(false);
        north.add(new Card(null).addFull(bar), BorderLayout.NORTH);
        north.add(kpis, BorderLayout.CENTER);

        Card chartCard = new Card(Messages.JOURNAL_CHART_TITLE.toString()).addFull(chart, 1)
                .addFull(mpv5.ui.modern.Applets.hint(Messages.JOURNAL_CHART_HINT.toString()));
        Card topCard = new Card(Messages.JOURNAL_CHART_TOP.toString()).addFull(top, 1);
        topCard.setPreferredSize(new Dimension(ModernTheme.px(300), ModernTheme.px(10)));
        JPanel center = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        center.setOpaque(false);
        center.add(chartCard, BorderLayout.CENTER);
        center.add(topCard, BorderLayout.EAST);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    /**
     * Loads the data the first time the tab is shown
     */
    public void showing() {
        if (!loaded) {
            loaded = true;
            reload();
        }
    }

    /**
     * @param preset 0 this year, 1 last year, 2 last 12 months, 3 last 3 years
     * (and this one)
     * @param today
     * @return the period
     */
    static vTimeframe range(int preset, LocalDate today) {
        LocalDate start;
        LocalDate end;
        switch (preset) {
            case 1:
                start = LocalDate.of(today.getYear() - 1, 1, 1);
                end = LocalDate.of(today.getYear() - 1, 12, 31);
                break;
            case 2:
                start = today.withDayOfMonth(1).minusMonths(11);
                end = today.withDayOfMonth(1).plusMonths(1).minusDays(1);
                break;
            case 3:
                start = LocalDate.of(today.getYear() - 3, 1, 1);
                end = LocalDate.of(today.getYear(), 12, 31);
                break;
            default:
                start = LocalDate.of(today.getYear(), 1, 1);
                end = LocalDate.of(today.getYear(), 12, 31);
        }
        return new vTimeframe(date(start), DateConverter.getEndOfDay(date(end)));
    }

    private static Date date(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate local(Date d) {
        return new java.sql.Date(d.getTime()).toLocalDate();
    }

    /**
     * The figures of a period
     */
    static final class Data {

        final List<String> buckets = new ArrayList<String>();
        /**
         * series - values per bucket: offers, invoiced, paid, expenses
         */
        final BigDecimal[][] values = new BigDecimal[4][];
        BigDecimal offers = BigDecimal.ZERO;
        BigDecimal invoiced = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        BigDecimal openReceivables = BigDecimal.ZERO;
        double payDays = -1;
        final Map<String, BigDecimal> top = new LinkedHashMap<String, BigDecimal>();
    }

    private void reload() {
        final vTimeframe t = time.getTime();
        final int p = per.getSelectedIndex();
        final int gen = ++generation;
        state.setText(Messages.JOURNAL_LOADING.toString());
        new SwingWorker<Data, Void>() {
            @Override
            protected Data doInBackground() throws Exception {
                return compute(JournalPanel.loadAll(t, false), JournalPanel.loadAll(t, true), t, p);
            }

            @Override
            protected void done() {
                if (gen != generation) {
                    return;
                }
                try {
                    show(get());
                    state.setText(" ");
                } catch (Exception e) {
                    Log.Debug(JournalChartPanel.class, e);
                    state.setText(Messages.JOURNAL_EXPORT_FAILED + ": " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * @param d the date
     * @param start start of the period
     * @param p MONTH, QUARTER, YEAR
     * @return the index of the bucket
     */
    static int bucket(LocalDate d, LocalDate start, int p) {
        switch (p) {
            case YEAR:
                return d.getYear() - start.getYear();
            case QUARTER:
                return (d.getYear() * 4 + (d.getMonthValue() - 1) / 3) - (start.getYear() * 4 + (start.getMonthValue() - 1) / 3);
            default:
                return (d.getYear() * 12 + d.getMonthValue() - 1) - (start.getYear() * 12 + start.getMonthValue() - 1);
        }
    }

    /**
     * @param byDocDate the journal of the period by document date
     * @param byPayDate the journal of the period by payment date
     * @param t the period
     * @param p MONTH, QUARTER, YEAR
     * @return the figures
     */
    static Data compute(List<Entry> byDocDate, List<Entry> byPayDate, vTimeframe t, int p) {
        Data d = new Data();
        LocalDate start = local(t.getStart());
        LocalDate end = local(t.getEnd());
        int n = bucket(end, start, p) + 1;
        for (int i = 0; i < n; i++) {
            LocalDate b = p == YEAR ? start.plusYears(i) : p == QUARTER ? start.plusMonths(3L * i) : start.plusMonths(i);
            d.buckets.add(p == YEAR ? String.valueOf(b.getYear())
                    : p == QUARTER ? "Q" + ((b.getMonthValue() - 1) / 3 + 1) + " " + (b.getYear() % 100 < 10 ? "0" : "") + b.getYear() % 100
                    : String.format("%02d/%02d", b.getMonthValue(), b.getYear() % 100));
        }
        for (int s = 0; s < 4; s++) {
            d.values[s] = new BigDecimal[n];
            for (int i = 0; i < n; i++) {
                d.values[s][i] = BigDecimal.ZERO;
            }
        }
        long days = 0;
        int paidInvoices = 0;
        for (Entry e : byDocDate) {
            if (e.date == null || JournalCalc.isCancelled(e)) {
                continue;
            }
            int i = bucket(local(e.date), start, p);
            if (i < 0 || i >= n) {
                continue;
            }
            if (e.type == Constants.TYPE_OFFER) {
                d.values[0][i] = d.values[0][i].add(e.net);
                d.offers = d.offers.add(e.net);
            } else if (e.type == Constants.TYPE_INVOICE || e.type == Constants.TYPE_DEPOSIT || e.type == Constants.TYPE_PART_PAYMENT
                    || e.type == Constants.TYPE_CREDIT) {
                BigDecimal v = e.type == Constants.TYPE_CREDIT ? e.net.negate() : e.net;
                d.values[1][i] = d.values[1][i].add(v);
                d.invoiced = d.invoiced.add(v);
                if (e.type != Constants.TYPE_CREDIT && e.status == Item.STATUS_PAID && e.obj instanceof Item) {
                    Date paidOn = ((Item) e.obj).__getDateend();
                    Date issued = ((Item) e.obj).__getDateadded();
                    if (paidOn != null && issued != null) {
                        long x = ChronoUnit.DAYS.between(local(issued), local(paidOn));
                        if (x >= 0) {
                            days += x;
                            paidInvoices++;
                        }
                    }
                }
            }
        }
        d.payDays = paidInvoices == 0 ? -1 : (double) days / paidInvoices;
        Totals open = JournalCalc.compute(byDocDate, null);
        d.openReceivables = open.openReceivables;
        //paid and expenses: as the EÜR counts them
        JournalCalc.compute(byPayDate, paidInParts(byPayDate));
        Map<String, BigDecimal> customers = new java.util.HashMap<String, BigDecimal>();
        for (Entry e : byPayDate) {
            if (!e.counted || e.date == null) {
                continue;
            }
            int i = bucket(local(e.date), start, p);
            if (i < 0 || i >= n) {
                continue;
            }
            if (e.type == Constants.TYPE_EXPENSE) {
                d.values[3][i] = d.values[3][i].add(e.net);
                d.expenses = d.expenses.add(e.net);
            } else {
                d.values[2][i] = d.values[2][i].add(e.effect);
                d.paid = d.paid.add(e.effect);
                String c = (e.contact + " " + e.prename).trim();
                if (!c.isEmpty()) {
                    BigDecimal v = customers.get(c);
                    customers.put(c, v == null ? e.effect : v.add(e.effect));
                }
            }
        }
        customers.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(8)
                .forEach(x -> d.top.put(x.getKey(), x.getValue()));
        return d;
    }

    /**
     * @return the invoices with payment rows in the list (their payments
     * count, not the invoice)
     */
    private static java.util.Set<Integer> paidInParts(List<Entry> l) {
        java.util.Set<Integer> s = new java.util.HashSet<Integer>();
        for (Entry e : l) {
            if (e.type == Constants.TYPE_PAYMENT) {
                s.add(e.itemId);
            }
        }
        return s;
    }

    private void show(Data d) {
        kpis.removeAll();
        kpis.add(kpi(Messages.JOURNAL_CHART_OFFERS.toString(), money(d.offers), null, 0));
        kpis.add(kpi(Messages.JOURNAL_CHART_INVOICED.toString(), money(d.invoiced),
                d.offers.signum() == 0 ? null : String.format(Messages.JOURNAL_KPI_CONVERSION.toString(),
                        FormatNumber.formatDezimal(d.invoiced.multiply(new BigDecimal(100)).divide(d.offers, 1, java.math.RoundingMode.HALF_UP))), 1));
        kpis.add(kpi(Messages.JOURNAL_CHART_PAID.toString(), money(d.paid),
                d.payDays < 0 ? null : String.format(Messages.JOURNAL_KPI_PAYDAYS.toString(), Math.round(d.payDays)), 2));
        kpis.add(kpi(Messages.JOURNAL_CHART_EXPENSES.toString(), money(d.expenses), null, 3));
        kpis.add(kpi(Messages.JOURNAL_CHART_SURPLUS.toString(), money(d.paid.subtract(d.expenses)), null, -1));
        kpis.add(kpi(Messages.JOURNAL_SUM_OPEN_RECEIVABLES.toString(), money(d.openReceivables), null, -2));
        kpis.revalidate();
        kpis.repaint();
        chart.setData(d);
        top.setData(d.top);
    }

    private static String money(BigDecimal v) {
        return FormatNumber.formatLokalCurrency(v);
    }

    /**
     * A key figure: label with the color of its series, value, a note
     */
    private static JComponent kpi(String label, String value, String note, int series) {
        Card c = new Card(null);
        JLabel l = new JLabel(label);
        l.setForeground(ModernTheme.muted());
        l.setFont(ModernTheme.font(-1, Font.PLAIN));
        if (series >= 0) {
            l.setIcon(new SwatchIcon(series));
        } else if (series == -1) {
            l.setIcon(new LineIcon());
        }
        JLabel v = new JLabel(value);
        v.setFont(ModernTheme.font(5, Font.BOLD));
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(2)));
        p.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        JLabel n = new JLabel(note == null ? " " : note);
        n.setForeground(ModernTheme.muted());
        n.setFont(ModernTheme.font(-1, Font.PLAIN));
        p.add(n, BorderLayout.SOUTH);
        c.addFull(p);
        return c;
    }

    /**
     * The colors of the series (fixed order, validated categorical palette;
     * light and dark steps) and of the surplus line
     */
    static Color series(int i) {
        boolean dark = ModernTheme.isDark();
        switch (i) {
            case 0:
                return dark ? new Color(0x3987e5) : new Color(0x2a78d6);
            case 1:
                return dark ? new Color(0xd95926) : new Color(0xeb6834);
            case 2:
                return dark ? new Color(0x199e70) : new Color(0x1baf7a);
            case 3:
                return dark ? new Color(0xc98500) : new Color(0xeda100);
            default:
                return dark ? new Color(0x9085e9) : new Color(0x6250d6);
        }
    }

    private static final class SwatchIcon implements javax.swing.Icon {

        private final int series;

        SwatchIcon(int series) {
            this.series = series;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(series(series));
            g2.fillRoundRect(x, y + 1, getIconWidth() - 3, getIconHeight() - 2, 3, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return ModernTheme.px(13);
        }

        @Override
        public int getIconHeight() {
            return ModernTheme.px(10);
        }
    }

    private static final class LineIcon implements javax.swing.Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(series(4));
            g2.setStroke(new BasicStroke(2f));
            int m = y + getIconHeight() / 2;
            g2.drawLine(x, m, x + getIconWidth() - 3, m);
            g2.fillOval(x + (getIconWidth() - 3) / 2 - 3, m - 3, 6, 6);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return ModernTheme.px(14);
        }

        @Override
        public int getIconHeight() {
            return ModernTheme.px(10);
        }
    }

    /**
     * Grouped bars (offers, invoiced, paid, expenses) per period with the
     * surplus (paid - expenses) as line; a legend whose entries hide and show
     * a series; the values of a period on hover
     */
    static final class Chart extends JComponent {

        private static final long serialVersionUID = 1L;
        private Data data;
        private final boolean[] visible = {true, true, true, true, true};
        private final String[] names = {Messages.JOURNAL_CHART_OFFERS.toString(), Messages.JOURNAL_CHART_INVOICED.toString(),
            Messages.JOURNAL_CHART_PAID.toString(), Messages.JOURNAL_CHART_EXPENSES.toString(), Messages.JOURNAL_CHART_SURPLUS.toString()};
        private final List<Rectangle> legend = new ArrayList<Rectangle>();
        private final List<Rectangle> columns = new ArrayList<Rectangle>();
        private int hover = -1;

        Chart() {
            setPreferredSize(new Dimension(ModernTheme.px(500), ModernTheme.px(280)));
            setFont(ModernTheme.font(-1, Font.PLAIN));
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int h = -1;
                    for (int i = 0; i < columns.size(); i++) {
                        if (columns.get(i).contains(e.getPoint())) {
                            h = i;
                        }
                    }
                    boolean overLegend = false;
                    for (Rectangle r : legend) {
                        overLegend |= r.contains(e.getPoint());
                    }
                    setCursor(Cursor.getPredefinedCursor(overLegend ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    if (h != hover) {
                        hover = h;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    for (int i = 0; i < legend.size(); i++) {
                        if (legend.get(i).contains(e.getPoint())) {
                            visible[i] = !visible[i];
                            repaint();
                        }
                    }
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
        }

        void setData(Data d) {
            data = d;
            hover = -1;
            repaint();
        }

        private BigDecimal surplus(int i) {
            return data.values[2][i].subtract(data.values[3][i]);
        }

        /**
         * @return a round step for about 5 grid lines
         */
        static double step(double range) {
            if (range <= 0) {
                return 1;
            }
            double raw = range / 5;
            double mag = Math.pow(10, Math.floor(Math.log10(raw)));
            double n = raw / mag;
            return (n <= 1 ? 1 : n <= 2 ? 2 : n <= 2.5 ? 2.5 : n <= 5 ? 5 : 10) * mag;
        }

        private static String axis(double v) {
            double a = Math.abs(v);
            boolean de = mpv5.i18n.LanguageManager.isGerman();
            if (a >= 1000000) {
                return FormatNumber.formatDezimal(v / 1000000d) + (de ? " Mio." : " M");
            } else if (a >= 1000) {
                return FormatNumber.formatDezimal(v / 1000d) + (de ? " Tsd." : " k");
            }
            return FormatNumber.formatDezimal(v);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            Color muted = ModernTheme.muted();
            Color grid = ModernTheme.border();
            legend.clear();
            columns.clear();

            //legend (always: several series), click hides a series
            int lx = 0;
            int ly = 0;
            int lh = fm.getHeight() + ModernTheme.px(6);
            for (int s = 0; s < names.length; s++) {
                int w = ModernTheme.px(18) + fm.stringWidth(names[s]) + ModernTheme.px(16);
                Rectangle r = new Rectangle(lx, ly, w, lh);
                legend.add(r);
                int cy = ly + lh / 2;
                if (s < 4) {
                    g2.setColor(visible[s] ? series(s) : grid);
                    g2.fill(new RoundRectangle2D.Double(lx, cy - ModernTheme.px(5), ModernTheme.px(12), ModernTheme.px(10), 4, 4));
                } else {
                    g2.setColor(visible[s] ? series(4) : grid);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(lx, cy, lx + ModernTheme.px(12), cy);
                    g2.fillOval(lx + ModernTheme.px(6) - 3, cy - 3, 6, 6);
                }
                g2.setColor(visible[s] ? ModernTheme.foreground() : muted);
                g2.drawString(names[s], lx + ModernTheme.px(18), cy + fm.getAscent() / 2 - 1);
                lx += w;
            }

            if (data == null || data.buckets.isEmpty()) {
                g2.dispose();
                return;
            }
            int n = data.buckets.size();
            double max = 0;
            double min = 0;
            for (int i = 0; i < n; i++) {
                for (int s = 0; s < 4; s++) {
                    if (visible[s]) {
                        max = Math.max(max, data.values[s][i].doubleValue());
                        min = Math.min(min, data.values[s][i].doubleValue());
                    }
                }
                if (visible[4]) {
                    max = Math.max(max, surplus(i).doubleValue());
                    min = Math.min(min, surplus(i).doubleValue());
                }
            }
            if (max == 0 && min == 0) {
                g2.setColor(muted);
                String s = Messages.JOURNAL_CHART_EMPTY.toString();
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }
            double step = step(max - min);
            double top = Math.ceil(max / step) * step;
            double bottom = Math.floor(min / step) * step;
            int labelW = 0;
            for (double v = bottom; v <= top + step / 2; v += step) {
                labelW = Math.max(labelW, fm.stringWidth(axis(v)));
            }
            int left = labelW + ModernTheme.px(10);
            int plotTop = lh + ModernTheme.px(12);
            int plotBottom = getHeight() - fm.getHeight() - ModernTheme.px(6);
            int plotW = getWidth() - left - ModernTheme.px(4);
            int plotH = plotBottom - plotTop;
            if (plotW < 50 || plotH < 30) {
                g2.dispose();
                return;
            }
            java.util.function.DoubleToIntFunction y = v -> plotBottom - (int) Math.round((v - bottom) / (top - bottom) * plotH);

            //grid and axis labels (recessive)
            for (double v = bottom; v <= top + step / 2; v += step) {
                int yy = y.applyAsInt(v);
                g2.setColor(Math.abs(v) < step / 1000 ? muted : grid);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(left, yy, left + plotW, yy);
                g2.setColor(muted);
                String s = axis(v);
                g2.drawString(s, left - ModernTheme.px(6) - fm.stringWidth(s), yy + fm.getAscent() / 2 - 1);
            }

            //bars
            double colW = plotW / (double) n;
            int bars = 0;
            for (int s = 0; s < 4; s++) {
                bars += visible[s] ? 1 : 0;
            }
            double groupW = colW * 0.72;
            double barW = bars == 0 ? 0 : Math.max(2, (groupW - (bars - 1) * 2) / bars);
            int zero = y.applyAsInt(0);
            for (int i = 0; i < n; i++) {
                int cx = left + (int) Math.round(colW * i);
                columns.add(new Rectangle(cx, plotTop, (int) Math.ceil(colW), plotH));
                if (i == hover) {
                    g2.setColor(ModernTheme.accentSoft());
                    g2.fillRect(cx, plotTop, (int) Math.ceil(colW), plotH);
                }
                double bx = cx + (colW - (bars * barW + (bars - 1) * 2)) / 2;
                for (int s = 0; s < 4; s++) {
                    if (!visible[s]) {
                        continue;
                    }
                    double v = data.values[s][i].doubleValue();
                    int yy = y.applyAsInt(v);
                    int h = Math.abs(zero - yy);
                    if (h > 0) {
                        g2.setColor(series(s));
                        //rounded at the data end, flat at the baseline
                        int r = (int) Math.min(ModernTheme.px(4), Math.min(barW / 2, h));
                        int top0 = Math.min(yy, zero);
                        g2.fill(new RoundRectangle2D.Double(bx, top0, barW, h, r * 2, r * 2));
                        if (v >= 0) {
                            g2.fillRect((int) bx, zero - r, (int) Math.ceil(barW), r);
                        } else {
                            g2.fillRect((int) bx, zero, (int) Math.ceil(barW), r);
                        }
                    }
                    bx += barW + 2;
                }
                //period labels: thinned out if they do not fit
                String label = data.buckets.get(i);
                int every = Math.max(1, (int) Math.ceil((fm.stringWidth(label) + ModernTheme.px(8)) / colW));
                if (i % every == 0) {
                    g2.setColor(muted);
                    g2.drawString(label, cx + (int) (colW - fm.stringWidth(label)) / 2, plotBottom + fm.getAscent() + ModernTheme.px(4));
                }
            }

            //surplus line
            if (visible[4]) {
                g2.setColor(series(4));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int px = -1;
                int py = -1;
                for (int i = 0; i < n; i++) {
                    int xx = left + (int) Math.round(colW * i + colW / 2);
                    int yy = y.applyAsInt(surplus(i).doubleValue());
                    if (px >= 0) {
                        g2.drawLine(px, py, xx, yy);
                    }
                    px = xx;
                    py = yy;
                }
                Color surface = ModernTheme.cardBackground();
                for (int i = 0; i < n; i++) {
                    int xx = left + (int) Math.round(colW * i + colW / 2);
                    int yy = y.applyAsInt(surplus(i).doubleValue());
                    g2.setColor(surface);
                    g2.fillOval(xx - 5, yy - 5, 10, 10);
                    g2.setColor(series(4));
                    g2.fillOval(xx - 4, yy - 4, 8, 8);
                }
            }

            //tooltip of the period under the mouse
            if (hover >= 0 && hover < n) {
                List<String[]> rows = new ArrayList<String[]>();
                for (int s = 0; s < 5; s++) {
                    if (visible[s]) {
                        rows.add(new String[]{names[s], money(s < 4 ? data.values[s][hover] : surplus(hover)), String.valueOf(s)});
                    }
                }
                String title = data.buckets.get(hover);
                int tw = fm.stringWidth(title);
                for (String[] r : rows) {
                    tw = Math.max(tw, ModernTheme.px(16) + fm.stringWidth(r[0]) + ModernTheme.px(16) + fm.stringWidth(r[1]));
                }
                int pad = ModernTheme.px(8);
                int th = (rows.size() + 1) * fm.getHeight() + pad * 2;
                Rectangle col = columns.get(hover);
                int tx = col.x + col.width + ModernTheme.px(6);
                if (tx + tw + pad * 2 > getWidth()) {
                    tx = col.x - tw - pad * 2 - ModernTheme.px(6);
                }
                int ty = plotTop;
                g2.setColor(ModernTheme.cardBackground());
                g2.fill(new RoundRectangle2D.Double(tx, ty, tw + pad * 2, th, 10, 10));
                g2.setColor(grid);
                g2.draw(new RoundRectangle2D.Double(tx, ty, tw + pad * 2, th, 10, 10));
                g2.setColor(ModernTheme.foreground());
                g2.setFont(getFont().deriveFont(Font.BOLD));
                g2.drawString(title, tx + pad, ty + pad + fm.getAscent());
                g2.setFont(getFont());
                int ry = ty + pad + fm.getHeight();
                for (String[] r : rows) {
                    int s = Integer.parseInt(r[2]);
                    g2.setColor(series(s));
                    g2.fillRoundRect(tx + pad, ry + fm.getHeight() / 2 - 4, 10, 8, 3, 3);
                    g2.setColor(muted);
                    g2.drawString(r[0], tx + pad + ModernTheme.px(16), ry + fm.getAscent());
                    g2.setColor(ModernTheme.foreground());
                    g2.drawString(r[1], tx + pad + tw - fm.stringWidth(r[1]), ry + fm.getAscent());
                    ry += fm.getHeight();
                }
            }
            g2.dispose();
        }
    }

    /**
     * The top customers by paid amount: name, value and a thin bar
     */
    static final class TopList extends JComponent {

        private static final long serialVersionUID = 1L;
        private Map<String, BigDecimal> data = new LinkedHashMap<String, BigDecimal>();

        TopList() {
            setFont(ModernTheme.font(-1, Font.PLAIN));
            setPreferredSize(new Dimension(ModernTheme.px(260), ModernTheme.px(200)));
        }

        void setData(Map<String, BigDecimal> d) {
            data = d;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            if (data.isEmpty()) {
                g2.setColor(ModernTheme.muted());
                g2.drawString(Messages.JOURNAL_CHART_EMPTY.toString(), 0, fm.getAscent());
                g2.dispose();
                return;
            }
            BigDecimal max = data.values().iterator().next();
            int y = 0;
            int row = fm.getHeight() + ModernTheme.px(14);
            for (Map.Entry<String, BigDecimal> e : data.entrySet()) {
                String v = money(e.getValue());
                int vw = fm.stringWidth(v);
                String name = e.getKey();
                while (name.length() > 3 && fm.stringWidth(name) > getWidth() - vw - ModernTheme.px(12)) {
                    name = name.substring(0, name.length() - 2);
                }
                if (!name.equals(e.getKey())) {
                    name = name + "…";
                }
                g2.setColor(ModernTheme.foreground());
                g2.drawString(name, 0, y + fm.getAscent());
                g2.drawString(v, getWidth() - vw, y + fm.getAscent());
                int w = max.signum() <= 0 ? 0 : (int) Math.round(getWidth() * Math.max(0, e.getValue().doubleValue()) / max.doubleValue());
                g2.setColor(ModernTheme.border());
                g2.fillRoundRect(0, y + fm.getHeight() + 2, getWidth(), ModernTheme.px(5), 4, 4);
                g2.setColor(series(2));
                g2.fillRoundRect(0, y + fm.getHeight() + 2, w, ModernTheme.px(5), 4, 4);
                y += row;
                if (y + row > getHeight()) {
                    break;
                }
            }
            g2.dispose();
        }
    }
}

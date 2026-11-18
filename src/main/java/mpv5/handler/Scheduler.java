package mpv5.handler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mpv5.YabsApplication;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.panels.HomeScreen;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;

/**
 * This class handles the scheduled events
 */
public class Scheduler extends Thread {

    private HomeScreen homescreen;

    public Scheduler() {
        homescreen = HomeScreen.getInstance();
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        //the dashboard loads its data in the background itself (HomeScreen.refresh, started by
        //getInstance in the constructor): no extra queries before it is shown (issue #159)
        //wait for the application without burning a CPU core (was an empty busy loop)
        while (!YabsApplication.getApplication().isReady()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        if (User.getCurrentUser().getProperties().getProperty(homescreen, "disable", false)) {
            Log.Debug(this, "Homescreen disabled by configuration");
        } else {
            mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(homescreen, Messages.HOMESCREEN.toString());
        }
    }

    /**
     * @param use e.g. "usebills": show this kind of document on the dashboard
     * @return the setting; without one: shown (credit notes: not, they have no
     * setting)
     */
    static boolean uses(String use) {
        return User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty$" + use, !"usecredit".equals(use));
    }

    /**
     * @param hide e.g. "hideunpaidbills": only from the alarm
     */
    static boolean hides(String hide) {
        return User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty$" + hide, false);
    }

    /**
     * @param itemType the kind as used here ("bills", "delivery", "partial
     * payment" ...)
     * @param level "warn" or "alert"
     * @return the days of the settings page (its keys), else 14 (warn) or 30
     * (alert)
     */
    static int days(String itemType, String level) {
        String key = settingsKey(itemType) + "." + level + ".days";
        String v = User.getCurrentUser().getProperties().getProperty(key);
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return "warn".equals(level) ? 14 : 30;
        }
    }

    /**
     * @return the name the settings page stores the days under
     */
    static String settingsKey(String itemType) {
        switch (itemType) {
            case "delivery":
                return "deliver";
            case "partial payment":
            case "partial payments":
            case "partpayment":
                return "partpayment";
            case "revenues":
            case "revenue":
                return "revenue";
            default:
                return itemType;
        }
    }

    /**
     * Fills the dashboard again (in the background) and shows it: also when
     * it is switched off at start or was closed (menu View > Open dashboard)
     *
     * @param show true to show the tab, false to only bring it up to date
     */
    public static void openHomeScreen(final boolean show) {
        //getInstance loads the data again in the background
        HomeScreen h = HomeScreen.getInstance();
        if (show) {
            mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(h, Messages.HOMESCREEN.toString());
        }
    }

    public void checkForCreateBillEvents(vTimeframe DateFrame) {
        HashMap<Color, List<? extends DatabaseObject>> map;
        map = getScheduledBills(DateFrame);
        Iterator<Color> it = map.keySet().
                iterator();
        while (it.hasNext()) {
            Color c = it.next();
            List<? extends DatabaseObject> data = map.get(c);
            if (!data.isEmpty()) {
                homescreen.show(map, homescreen.getNextEvents());
                break;
            }
        }
    }

    public void checkForOverdueEvents(vTimeframe DateFrame) {
        HashMap<Color, List<? extends DatabaseObject>> map;
        map = getOverdueEvents(DateFrame);
        Iterator<Color> it = map.keySet().
                iterator();
        while (it.hasNext()) {
            Color c = it.next();
            List<? extends DatabaseObject> data = map.get(c);
            if (!data.isEmpty()) {
                homescreen.show(map, homescreen.getOverdue());
                break;
            }
        }
    }

    public static HashMap<Color, List<? extends DatabaseObject>> getScheduledBills(vTimeframe DateFrame) {
        ArrayList<Schedule> data;
        if (DateFrame == null) {
            data = Schedule.getEvents(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));
        } else {
            data = Schedule.getEvents(DateFrame);
        }
        List<Schedule> warnings = new ArrayList<Schedule>();
        List<Schedule> alerts = new ArrayList<Schedule>();
        List<Schedule> waitings = new ArrayList<Schedule>();

        for (int i = 0; i < data.size(); i++) {
            Schedule sched = data.get(i);
            if (!sched.__getIsdone() && sched.__getIntervalmonth() != 0) {
                if (sched.__getNextdate().compareTo(new Date()) < 0) {
                    alerts.add(sched);
                } else if (sched.__getNextdate().compareTo(new Date()) > 0) {
                    waitings.add(sched);
                } else {
                    warnings.add(sched);
                }
            }
        }

        HashMap<Color, List<? extends DatabaseObject>> map = new HashMap<Color, List<? extends DatabaseObject>>();
        map.put(Color.red, alerts);
        map.put(Color.yellow, warnings);
        map.put(Color.green, waitings);
        return map;
    }

    public static HashMap<Color, List<? extends DatabaseObject>> getOverdueEvents(vTimeframe DateFrame) {
        List<DatabaseObject> warnings = new ArrayList<>();
        List<DatabaseObject> alerts = new ArrayList<>();
        List<DatabaseObject> waitings = new ArrayList<>();
        int b = 0;
        Context c = Context.getInvoice();
        String ItemType = "bills";
        String prop = "";
        String use = "";
        try {
            while (b < 8) {
                QueryCriteria2 opens = new QueryCriteria2();
                switch (b) {
                    case 0:
                        c = Context.getInvoice();
                        ItemType = "bills";
                        prop = "hideunpaidbills";
                        opens.or(
                                new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(c, "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usebills";
                        break;
                    case 1:
                        c = Context.getOrder();
                        ItemType = "order";
                        prop = "hideunattentedorders";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useorders";
                        break;
                    case 2:
                        c = Context.getOffer();
                        ItemType = "offer";
                        prop = "hideunacceptedoffers";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useoffers";
                        break;
                    case 3:
                        c = Context.getDelivery();
                        ItemType = "delivery";
                        prop = "hideunattenteddeliverys";
                        opens.or(
                                new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(c, "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usedeliverys";
                        break;
                    case 4:
                        c = Context.getConfirmation();
                        ItemType = "confirmation";
                        prop = "hideunattentedconfirmations";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useconfirmations";
                        break;
                    case 5:
                        c = Context.getCredit();
                        ItemType = "credit";
                        prop = "hideunattendedcredits";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usecredit";
                        break;
                    case 6:
                        c = Context.getPartPayment();
                        ItemType = "partial payment";
                        prop = "hideunattentedpartpayments";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usepartpayment";
                        break;
                    case 7:
                        c = Context.getDeposit();
                        ItemType = "deposit";
                        prop = "hideunattenteddeposits";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usedeposit";
                        break;
                }
                if (!hides(prop)
                        && uses(use)) {
                    if (b < 5) {
                        opens.and(new QueryParameter(c, "inttype", b, QueryParameter.EQUALS));
                    }
                    opens.setOrder("dateadded", true);
                    try {
                        ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens);

                        if (data.hasData()) {
                            Object[][] d = data.getData();
                            for (int i = 0; i < d.length; i++) {
                                int id = Integer.valueOf(d[i][0].toString());
                                try {
                                    Item it = (Item) Item.getObject(
                                            c,
                                            id);
                                    if (DateFrame != null) {
                                        if (DateFrame.contains(it.__getDateadded())) {
                                            waitings.add(it);
                                        }
                                    } else {
                                        waitings.add(it);
                                    }
                                } catch (NodataFoundException ex) {
                                    Log.Debug(Scheduler.class,
                                            ex.getMessage());
                                }
                            }
                        }
                    } catch (NodataFoundException ex) {
                        Log.Debug(Scheduler.class, ex.getLocalizedMessage());
                    }
                } else {
                    Log.Debug(Scheduler.class,
                            "Don't show " + ItemType + " befor alert!");
                }
                b++;
            }
        } catch (Exception numberFormatException) {
            Log.Debug(numberFormatException);
        }
        b = 0;
        try {
            while (b < 8) {
                QueryCriteria2 opens = new QueryCriteria2();
                switch (b) {
                    case 0:
                        c = Context.getInvoice();
                        ItemType = "bills";
                        prop = "hideunpaidbills";
                        opens.or(
                                new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(c, "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usebills";
                        break;
                    case 1:
                        c = Context.getOrder();
                        ItemType = "order";
                        prop = "hideunattentedorders";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useorders";
                        break;
                    case 2:
                        c = Context.getOffer();
                        ItemType = "offer";
                        prop = "hideunacceptedoffers";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useoffers";
                        break;
                    case 3:
                        c = Context.getDelivery();
                        ItemType = "delivery";
                        prop = "hideunattenteddeliverys";
                        opens.or(
                                new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(c, "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usedeliverys";
                        break;
                    case 4:
                        c = Context.getConfirmation();
                        ItemType = "confirmation";
                        prop = "hideunattentedconfirmations";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useconfirmations";
                        break;
                    case 5:
                        c = Context.getCredit();
                        ItemType = "credit";
                        prop = "hideunattendedcredits";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usecredit";
                        break;
                    case 6:
                        c = Context.getPartPayment();
                        ItemType = "partial payments";
                        prop = "hideunattentedpartpayments";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usepartpayment";
                        break;
                    case 7:
                        c = Context.getDeposit();
                        ItemType = "deposit";
                        prop = "hideunattenteddeposits";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usedeposit";
                        break;
                }
                if (!hides(prop)
                        && uses(use)) {
                    Integer warn = days(ItemType, "warn");
                    if (b < 5) {
                    opens.and(new QueryParameter(c, "inttype", b, QueryParameter.EQUALS));
                    }
                    opens.setOrder("dateadded", true);
                    try {
                        ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens,
                                new vTimeframe(new Date(0), DateConverter.addDays(new Date(), warn * -1)), "dateadded");

                        if (data.hasData()) {
                            Object[][] d = data.getData();
                            for (int i = 0; i < d.length; i++) {
                                int id = Integer.valueOf(d[i][0].toString());
                                try {
                                    Item it = (Item) Item.getObject(
                                            c,
                                            id);
                                    if (DateFrame != null) {
                                        if (DateFrame.contains(it.__getDateadded())) {
                                            warnings.add(it);
                                        }
                                    } else {
                                        warnings.add(it);
                                    }
                                } catch (NodataFoundException ex) {
                                    Log.Debug(Scheduler.class,
                                            ex.getMessage());
                                }
                            }
                        }
                    } catch (NodataFoundException ex) {
                        Log.Debug(Scheduler.class, ex.getLocalizedMessage());
                    }
                } else {
                    Log.Debug(Scheduler.class,
                            "No warn treshold for " + ItemType + " defined.");
                }
                b++;
            }
        } catch (Exception numberFormatException) {
            Log.Debug(numberFormatException);
        }
        b = 0;
        try {
            while (b < 8) {
                QueryCriteria2 opens = new QueryCriteria2();
                switch (b) {
                    case 0:
                        c = Context.getInvoice();
                        ItemType = "bills";
                        opens.or(
                                new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(c, "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usebills";
                        break;
                    case 1:
                        c = Context.getOrder();
                        ItemType = "order";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useorders";
                        break;
                    case 2:
                        c = Context.getOffer();
                        ItemType = "offer";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useoffers";
                        break;
                    case 3:
                        c = Context.getDelivery();
                        ItemType = "delivery";
                        opens.or(
                                new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(c, "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usedeliverys";
                        break;
                    case 4:
                        c = Context.getConfirmation();
                        ItemType = "confirmation";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useconfirmations";
                        break;
                    case 5:
                        c = Context.getCredit();
                        ItemType = "credit";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usecredit";
                        break;
                    case 6:
                        c = Context.getPartPayment();
                        ItemType = "partpayment";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usepartpayment";
                        break;
                    case 7:
                        c = Context.getDeposit();
                        ItemType = "deposit";
                        opens.and(new QueryParameter(c, "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "usedeposit";
                        break;
                }
                if (uses(use)) {
                    Integer alert = days(ItemType, "alert");
                    if (b < 5) {
                    opens.and(new QueryParameter(c, "inttype", b, QueryParameter.EQUALS));
                    }
                    opens.setOrder("dateadded", true);
                    try {
                        ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens,
                                new vTimeframe(new Date(0), DateConverter.addDays(new Date(), alert * -1)), "dateadded");

                        if (data.hasData()) {
                            Object[][] d = data.getData();
                            for (int i = 0; i < d.length; i++) {
                                int id = Integer.valueOf(d[i][0].toString());
                                try {
                                    Item it = (Item) Item.getObject(c,
                                            id);
                                    if (DateFrame != null) {
                                        if (DateFrame.contains(it.__getDateadded())) {
                                            alerts.add(it);
                                        }
                                    } else {
                                        alerts.add(it);
                                    }
                                } catch (NodataFoundException ex) {
                                    Log.Debug(ex);
                                }
                            }
                        }
                    } catch (NodataFoundException ex) {
                        Log.Debug(Scheduler.class, ex.getLocalizedMessage());
                    }
                } else {
                    Log.Debug(Scheduler.class,
                            "No alert treshold for " + ItemType + " defined.");
                }
                b++;
            }
        } catch (Exception numberFormatException) {
            Log.Debug(numberFormatException);
        }
                
// add revenues that are pending ...
        
        c = Context.getRevenue();
        QueryCriteria2 opens = new QueryCriteria2();
        opens.and(new QueryParameter(c, "status", Item.STATUS_QUEUED, QueryParameter.EQUALS));
        ItemType = "revenues";

        if (!hides("hideunattentedrevenues")
                && uses("userevenues")) {
           opens.setOrder("dateadded", true);
            try {
                ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens);

                if (data.hasData()) {
                    Object[][] d = data.getData();
                    for (int i = 0; i < d.length; i++) {
                        int id = Integer.valueOf(d[i][0].toString());
                        try {
                            Revenue it = (Revenue) Revenue.getObject(
                                    c,
                                    id);
                            if (DateFrame != null) {
                                if (DateFrame.contains(it.__getDateadded())) {
                                    waitings.add(it);
                                }
                            } else {
                                waitings.add(it);
                            }
                        } catch (NodataFoundException ex) {
                            Log.Debug(Scheduler.class,
                                    ex.getMessage());
                        }
                    }
                }
            } catch (NodataFoundException ex) {
                Log.Debug(Scheduler.class, ex.getLocalizedMessage());
            }
        } else {
            Log.Debug(Scheduler.class,
                    "Don't show " + ItemType + " befor alert!");
        }
        if (!hides("hideunattentedrevenues")
                && uses("userevenues")) {
            Integer warn = days("revenue", "warn");
            opens.setOrder("dateadded", true);
            try {
                ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens,
                        new vTimeframe(new Date(0), DateConverter.addDays(new Date(), warn * -1)), "dateadded");

                if (data.hasData()) {
                    Object[][] d = data.getData();
                    for (int i = 0; i < d.length; i++) {
                        int id = Integer.valueOf(d[i][0].toString());
                        try {
                            Revenue it = (Revenue) Revenue.getObject(
                                    c,
                                    id);
                            if (DateFrame != null) {
                                if (DateFrame.contains(it.__getDateadded())) {
                                    warnings.add(it);
                                }
                            } else {
                                warnings.add(it);
                            }
                        } catch (NodataFoundException ex) {
                            Log.Debug(Scheduler.class,
                                    ex.getMessage());
                        }
                    }
                }
            } catch (NodataFoundException ex) {
                Log.Debug(Scheduler.class, ex.getLocalizedMessage());
            }
        } else {
            Log.Debug(Scheduler.class,
                    "No warn treshold for " + ItemType + " defined.");
        }
        if (uses("userevenues")) {
            Integer alert = days("revenue", "alert");
            opens.setOrder("dateadded", true);
            try {
                ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens,
                        new vTimeframe(new Date(0), DateConverter.addDays(new Date(), alert * -1)), "dateadded");

                if (data.hasData()) {
                    Object[][] d = data.getData();
                    for (int i = 0; i < d.length; i++) {
                        int id = Integer.valueOf(d[i][0].toString());
                        try {
                            Revenue it = (Revenue) Revenue.getObject(c,
                                    id);
                            if (DateFrame != null) {
                                if (DateFrame.contains(it.__getDateadded())) {
                                    alerts.add(it);
                                }
                            } else {
                                alerts.add(it);
                            }
                        } catch (NodataFoundException ex) {
                            Log.Debug(ex);
                        }
                    }
                }
            } catch (NodataFoundException ex) {
                Log.Debug(Scheduler.class, ex.getLocalizedMessage());
            }
        } else {
            Log.Debug(Scheduler.class,
                    "No alert treshold for " + ItemType + " defined.");
        }
        
        for (DatabaseObject i : alerts) {//Remove dupes
            if (warnings.contains(i)) {
                warnings.remove(i);
            }
            if (waitings.contains(i)) {
                waitings.remove(i);
            }
        }

        for (DatabaseObject i : warnings) {//Remove dupes
            if (waitings.contains(i)) {
                waitings.remove(i);
            }
        }

        HashMap<Color, List<? extends DatabaseObject>> map = new HashMap<Color, List<? extends DatabaseObject>>();
        map.put(Color.red, alerts);
        map.put(Color.yellow, warnings);
        map.put(Color.green, waitings);

        return map;
    }
}

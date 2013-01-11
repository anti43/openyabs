package mpv5.handler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mpv5.YabsApplication;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Item;
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
        checkForOverdueEvents();
        checkForCreateBillEvents(null);
        while (YabsApplication.getApplication().isReady() == false);
        if (User.getCurrentUser().getProperties().getProperty(homescreen, "disable", false)) {
            Log.Debug(this, "Homescreen disabled by configuration");
        } else {
            mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(homescreen, Messages.HOMESCREEN.toString());
        }
    }

    public void checkForCreateBillEvents(vTimeframe DateFrame) {
        HashMap<Color, List<Schedule>> map;
        map = getScheduledBills(DateFrame);
        Iterator<Color> it = map.keySet().
                iterator();
        while (it.hasNext()) {
            Color c = it.next();
            List<Schedule> data = map.get(c);
            if (!data.isEmpty()) {
                homescreen.show(map, homescreen.getNextEvents());
                break;
            }
        }
    }

    public void checkForOverdueEvents() {
        HashMap<Color, List<Item>> map;
        map = getOverdueEvents();
        Iterator<Color> it = map.keySet().
                iterator();
        while (it.hasNext()) {
            Color c = it.next();
            List<Item> data = map.get(c);
            if (!data.isEmpty()) {
                homescreen.show(map, homescreen.getOverdue());
                break;
            }
        }
    }

    public static HashMap<Color, List<Schedule>> getScheduledBills(vTimeframe DateFrame) {
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

        HashMap<Color, List<Schedule>> map = new HashMap<Color, List<Schedule>>();
        map.put(Color.red, alerts);
        map.put(Color.yellow, warnings);
        map.put(Color.green, waitings);
        return map;
    }

    public static HashMap<Color, List<Item>> getOverdueEvents() {
        List<Item> warnings = new ArrayList<Item>();
        List<Item> alerts = new ArrayList<Item>();
        List<Item> waitings = new ArrayList<Item>();
        int b = 0;
        Context c = Context.getItem();
        String ItemType = "bills";
        String prop = "";
        String use = "";
        try {
            while (b < 5) {
                QueryCriteria2 opens = new QueryCriteria2();
                switch (b) {
                    case 0:
                        ItemType = "bills";
                        prop = "hideunpaidbills";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usebills";
                        break;
                    case 1:
                        ItemType = "order";
                        prop = "hideunattentedorders";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useorders";
                        break;
                    case 2:
                        ItemType = "offer";
                        prop = "hideunacceptedoffers";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useoffers";
                        break;
                    case 3:
                        ItemType = "delivery";
                        prop = "hideunattenteddeliverys";
                        opens.or(
                                new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usedeliverys";
                        break;
                    case 4:
                        ItemType = "confirmation";
                        prop = "hideunattentedconfirmations";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useconfirmations";
                        break;
                }
                if (!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", prop)
                        && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", use)) {
                    opens.and(new QueryParameter(Context.getItem(), "inttype", b, QueryParameter.EQUALS));
                    opens.setOrder("dateadded", true);
                    try {
                        ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens);

                        if (data.hasData()) {
                            Object[][] d = data.getData();
                            for (int i = 0; i < d.length; i++) {
                                int id = Integer.valueOf(d[i][0].toString());
                                try {
                                    Item it = (Item) Item.getObject(
                                            Context.getItem(),
                                            id);
                                    waitings.add(it);
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
            while (b < 5) {
                QueryCriteria2 opens = new QueryCriteria2();
                switch (b) {
                    case 0:
                        ItemType = "bills";
                        prop = "hideunpaidbills";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usebills";
                        break;
                    case 1:
                        ItemType = "order";
                        prop = "hideunattentedorders";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useorders";
                        break;
                    case 2:
                        ItemType = "offer";
                        prop = "hideunacceptedoffers";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useoffers";
                        break;
                    case 3:
                        ItemType = "delivery";
                        prop = "hideunattenteddeliverys";
                        opens.or(
                                new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usedeliverys";
                        break;
                    case 4:
                        ItemType = "confirmation";
                        prop = "hideunattentedconfirmations";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useconfirmations";
                        break;
                }
                if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(ItemType + ".warn.days")
                        && !mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", prop)
                        && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", use)) {
                    Integer warn = Integer.valueOf(mpv5.db.objects.User.getCurrentUser().
                            getProperties().
                            getProperty(ItemType + ".warn.days"));
                    opens.and(new QueryParameter(Context.getItem(), "inttype", b, QueryParameter.EQUALS));
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
                                            Context.getItem(),
                                            id);
                                    warnings.add(it);
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
            while (b < 5) {
                QueryCriteria2 opens = new QueryCriteria2();
                switch (b) {
                    case 0:
                        ItemType = "bills";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usebills";
                        break;
                    case 1:
                        ItemType = "order";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useorders";
                        break;
                    case 2:
                        ItemType = "offer";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useoffers";
                        break;
                    case 3:
                        ItemType = "delivery";
                        opens.or(
                                new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS),
                                new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_FINISHED, QueryParameter.EQUALS));
                        use = "usedeliverys";
                        break;
                    case 4:
                        ItemType = "confirmation";
                        opens.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_QUEUED, QueryParameter.EQUALS));
                        use = "useconfirmations";
                        break;
                }
                if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(ItemType + ".alert.days") 
                         && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", use)) {
                    Integer alert = Integer.valueOf(mpv5.db.objects.User.getCurrentUser().
                            getProperties().
                            getProperty(ItemType + ".alert.days"));
                    opens.and(new QueryParameter(Context.getItem(), "inttype", b, QueryParameter.EQUALS));
                    opens.setOrder("dateadded", true);
                    try {
                        ReturnValue data = QueryHandler.instanceOf().clone(c).select("IDS", opens,
                                new vTimeframe(new Date(0), DateConverter.addDays(new Date(), alert * -1)), "dateadded");

                        if (data.hasData()) {
                            Object[][] d = data.getData();
                            for (int i = 0; i < d.length; i++) {
                                int id = Integer.valueOf(d[i][0].toString());
                                try {
                                    Item it = (Item) Item.getObject(Context.getItem(),
                                            id);
                                    alerts.add(it);
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
        for (Item i : alerts) {//Remove dupes
            if (warnings.contains(i)) {
                warnings.remove(i);
            }
            if (waitings.contains(i)) {
                waitings.remove(i);
            }
        }

        for (Item i : warnings) {//Remove dupes
            if (waitings.contains(i)) {
                waitings.remove(i);
            }
        }

        HashMap<Color, List<Item>> map = new HashMap<Color, List<Item>>();
        map.put(Color.red, alerts);
        map.put(Color.yellow, warnings);
        map.put(Color.green, waitings);
        return map;
    }
}

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
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Item;
import mpv5.db.objects.Schedule;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.panels.HomeScreen;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;

/**
 * This class handles the scheduled events
 */
public class Scheduler extends Thread {

    private HomeScreen g;

    public Scheduler() {
        g = HomeScreen.getInstance();
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        checkForOverdueEvents();
        checkForCreateBillEvents(null);
        while (YabsApplication.getApplication().isReady() == false);
//        if (show) {
        mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(g, Messages.HOMESCREEN.toString());
//        }
    }

    @SuppressWarnings("static-access")
    public void checkForCreateBillEvents(vTimeframe DateFrame) {
        HashMap<Color, List<Schedule>> map = new HashMap<Color, List<Schedule>>();
        map = getScheduledBills(DateFrame);
        Iterator<Color> it = map.keySet().
                iterator();
        while (it.hasNext()) {
            Color c = it.next();
            List<Schedule> data = map.get(c);
            if (!data.isEmpty()) {
                g.show(map, g.getNextEvents());
                break;
            }
        }
    }

    @SuppressWarnings("fallthrough")
    public void checkForOverdueEvents() {
        HashMap<Color, List<Item>> map = new HashMap<Color, List<Item>>();
        map = getOverdueEvents();
        Iterator<Color> it = map.keySet().
                iterator();
        while (it.hasNext()) {
            Color c = it.next();
            List<Item> data = map.get(c);
            if (!data.isEmpty()) {
                g.show(map, g.getOverdue());
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
        String ItemType = "bills";
        String prop = "";
        try {
            while (b < 5) {
                switch (b) {
                    case 0:
                        ItemType = "bills";
                        prop = "hideunpaidbills";
                    case 1:
                        ItemType = "order";
                        prop = "hideunattentedorders";
                    case 2:
                        ItemType = "offer";
                        prop = "hideunacceptedoffers";
                    case 3:
                        ItemType = "delivery";
                        prop = "hideunattenteddeliverys";
                    case 4:
                        ItemType = "confirmation";
                        prop = "hideunattentedconfirmations";
                }
                if (!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", prop)) {
                    String sql = "SELECT ids FROM items WHERE (intstatus = " + Item.STATUS_IN_PROGRESS + " OR intstatus = "
                            + Item.STATUS_FINISHED + ") AND inttype=" + b
                            + " AND invisible = 0";
                    ReturnValue data = QueryHandler.getConnection().
                            freeSelectQuery(sql,
                            MPSecurityManager.VIEW,
                            null);

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
                switch (b) {
                    case 0:
                        ItemType = "bills";
                        prop = "hideunpaidbills";
                    case 1:
                        ItemType = "order";
                        prop = "hideunattentedorders";
                    case 2:
                        ItemType = "offer";
                        prop = "hideunacceptedoffers";
                    case 3:
                        ItemType = "delivery";
                        prop = "hideunattenteddeliverys";
                    case 4:
                        ItemType = "confirmation";
                        prop = "hideunattentedconfirmations";
                }
                if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(ItemType + ".warn.days")
                        && !mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", prop)) {
                    Integer warn = Integer.valueOf(mpv5.db.objects.User.getCurrentUser().
                            getProperties().
                            getProperty(ItemType + ".warn.days"));
                    String sql = "SELECT ids FROM items WHERE dateadded <= '"
                            + DateConverter.getSQLDateString(DateConverter.addDays(
                            new Date(),
                            warn * -1))
                            + "' AND (intstatus = " + Item.STATUS_IN_PROGRESS + " OR intstatus = " + Item.STATUS_FINISHED + ") AND inttype=" + b
                            + " AND invisible = 0";
                    ReturnValue data = QueryHandler.getConnection().
                            freeSelectQuery(sql,
                            MPSecurityManager.VIEW,
                            null);

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
        while (b < 5) {
            switch (b) {
                case 0:
                    ItemType = "bills";
                case 1:
                    ItemType = "order";
                case 2:
                    ItemType = "offer";
                case 3:
                    ItemType = "delivery";
                case 4:
                    ItemType = "confirmation";
            }
            if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(ItemType + ".alert.days")) {
                Integer alert = Integer.valueOf(mpv5.db.objects.User.getCurrentUser().
                        getProperties().
                        getProperty(ItemType + ".alert.days"));
                String sql = "SELECT ids FROM items WHERE dateadded <= '"
                        + DateConverter.getSQLDateString(DateConverter.addDays(
                        new Date(),
                        alert * -1))
                        + "' AND (intstatus = " + Item.STATUS_IN_PROGRESS + " OR intstatus = " + Item.STATUS_FINISHED + ") AND inttype=" + b
                        + " AND invisible = 0";
                ReturnValue data = QueryHandler.getConnection().
                        freeSelectQuery(sql,
                        MPSecurityManager.VIEW,
                        null);

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
            } else {
                Log.Debug(Scheduler.class,
                        "No alert treshold for " + ItemType + " defined.");
            }
            b++;
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

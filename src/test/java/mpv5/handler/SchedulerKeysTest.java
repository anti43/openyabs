package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SchedulerKeysTest {

    @Test
    void daysAreReadUnderTheKeysOfTheSettingsPage() {
        //the settings page stores deliver.*, partpayment.*, revenue.*
        assertEquals("deliver", Scheduler.settingsKey("delivery"));
        assertEquals("partpayment", Scheduler.settingsKey("partial payment"));
        assertEquals("partpayment", Scheduler.settingsKey("partial payments"));
        assertEquals("revenue", Scheduler.settingsKey("revenues"));
        assertEquals("bills", Scheduler.settingsKey("bills"));
    }

    @Test
    void daysOpenOfTheDashboard() {
        java.util.Date today = new java.util.Date();
        assertEquals(10, mpv5.ui.panels.HomeScreenAccess.daysOpen(mpv5.utils.date.DateConverter.addDays(today, -10), today));
        assertEquals(0, mpv5.ui.panels.HomeScreenAccess.daysOpen(mpv5.utils.date.DateConverter.addDays(today, 3), today));
        assertEquals(0, mpv5.ui.panels.HomeScreenAccess.daysOpen(null, today));
    }
}

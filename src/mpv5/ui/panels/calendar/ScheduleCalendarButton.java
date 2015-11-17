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
package mpv5.ui.panels.calendar;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import mpv5.db.objects.Schedule;
import mpv5.globals.Messages;
import mpv5.ui.panels.ScheduleDayEventsPanel;

public class ScheduleCalendarButton extends JButton implements ActionListener {

    private static final long serialVersionUID = 1L;
    private Date date;
    private ArrayList<Schedule> scheduledEvents = new ArrayList<Schedule>();

    protected ScheduleCalendarButton(Date d) {
        super("");
        this.date = d;
    }

    /**
     * @return the date
     */
    protected Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    protected void setDate(Date date) {
        this.date = date;
    }

    protected void addScheduledEvent(Schedule schedule) {
        scheduledEvents.add(schedule);
        this.setFont(this.getFont().deriveFont(Font.BOLD));
//$2GREEN);

        if (scheduledEvents.isEmpty() == false) {
            this.setToolTipText(Messages.DAY_EVENTS.toString());
        }
    }

    protected void removeScheduledEvent(Schedule schedule) {
        scheduledEvents.remove(schedule);
        if (scheduledEvents.isEmpty() == true) {
            this.setFont(this.getFont().deriveFont(Font.PLAIN));
//$2LIGHT_GRAY);
            this.setToolTipText(Messages.DAY_EVENTS.toString());
        }
    }

    protected void clearScheduledEvents() {
        scheduledEvents.clear();
        this.setFont(this.getFont().deriveFont(Font.PLAIN));
//$2LIGHT_GRAY);
    }

    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        String buttonText = button.getText();
        int dayInt = new Integer(buttonText).intValue();
        ScheduleCalendarDayChooser.instanceOf().setDay(dayInt);
        ScheduleDayEventsPanel.instanceOf().setDayEvents(scheduledEvents);
    }

    protected boolean hasEvents() {
        if (scheduledEvents.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<Schedule> getScheduledEvents() {
        return scheduledEvents;
    }
}

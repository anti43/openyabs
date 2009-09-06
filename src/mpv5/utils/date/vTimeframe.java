/*
 * 
 * 
 */

package mpv5.utils.date;

import java.util.Date;

/**
 *
 * This class implements a timeframe between two dates
 */
public class vTimeframe {
    private Date start;
    private Date end;
    private long time;

    /**
     * Create a new timeframe
     * @param start
     * @param ende
     */
    public vTimeframe(Date start, Date ende) {
        this.start = start; 
        this.end = ende;   
        this.time = end.getTime() - start.getTime();   
    }

    /**
     * Create a new timeframe
     * @param von
     * @param bis
     */
    public vTimeframe(vDate von, vDate bis){
        this.start = von.getDate();
        this.end = bis.getDate();
        this.time = end.getTime() - start.getTime();   
    }

    /**
     * Checks whether the given date is within this timeframe
     * @param day
     * @return True if the date is within or at the timeframe`s start or end date
     */
    public boolean contains(Date day) {
        return this.getEnd().equals(day) || this.getStart().equals(day) || (day.after(this.getStart()) && day.before(this.getEnd()));
    }

    /**
     * The start
     * @return
     */
    public Date getStart() {
        return start;
    }

    /**
     * The end date
     * @return
     */
    public Date getEnd() {
        return end;
    }

    /**
     *
     * @return The difference of start and end in millis
     */
    public long getTime() {
        return time;
    }
    
    @Override
    public String toString(){
     return start + " - " + end; 
    }
}

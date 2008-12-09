/*
 * 
 * 
 */

package mpv5.utils.date;

import mpv5.utils.date.vDate;
import java.util.Date;

/**
 *
 * @author anti43
 */
public class vTimeframe {
    private Date start;
    private Date end;
    private long time;

    public vTimeframe(Date start, Date ende) {
        this.start = start; 
        this.end = ende;   
        this.time = end.getTime() - start.getTime();   
    }

    public vTimeframe(vDate von, vDate bis){
        this.start = von.date; 
        this.end = bis.date;   
        this.time = end.getTime() - start.getTime();   
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public long getTime() {
        return time;
    }
    
    @Override
    public String toString(){
     return start + " - " + end; 
    }
}

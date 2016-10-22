package mpv5.db.common;

public class ContextWrap implements Comparable<Context> {

    private String display;
    public Context c;

    public ContextWrap(String display, Context c) {
        this.display = display;
        this.c = c;
    }

    @Override
    public String toString() {
        return display;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        return o.hashCode()==hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.c != null ? this.c.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Context t) {
        return c.compareTo(t);
    }
}

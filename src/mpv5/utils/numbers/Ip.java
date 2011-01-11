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
package mpv5.utils.numbers;

import mpv5.logging.Log;


public class Ip {

    public static boolean isValidIP(String valueOf) {
        try {
            new Ip(valueOf);
            return true;
        } catch (Exception e) {
            Log.Print(e.getMessage());
            return false;
        }
    }
    private int part0 = 0;
    private int part1 = 0;
    private int part2 = 0;
    private int part3 = 0;
    private String[] fullip;
    private String oip;

    public Ip(String ip) {

        oip = ip;
        fullip = ip.split("\\.");
        part0 = Integer.valueOf(fullip[0]).intValue();
        part1 = Integer.valueOf(fullip[1]).intValue();
        part2 = Integer.valueOf(fullip[2]).intValue();
        part3 = Integer.valueOf(fullip[3]).intValue();

    }

    /**
     * @return the part0
     */
    public int getPart0() {
        return part0;
    }

    /**
     * @return the part1
     */
    public int getPart1() {
        return part1;
    }

    /**
     * @return the part2
     */
    public int getPart2() {
        return part2;
    }

    /**
     * @return the part3
     */
    public int getPart3() {
        return part3;
    }

    /**
     * @return the fullip
     */
    public String[] getFullip() {
        return fullip;
    }

    @Override
    public String toString() {
        return oip;
    }

    @Override
    public boolean equals(Object o) {
        return o.toString().equals(oip);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.oip != null ? this.oip.hashCode() : 0);
        return hash;
    }
}

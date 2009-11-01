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
package mpv5.db.common;

import mpv5.logging.Log;

/**
 *
 * @author andreas
 */
public class QueryCriteria2 {

    private String query = "";
    private String order = "";

    /**
     * Add AND conditions
     * @param params
     */
    public void and(QueryParameter... params) {
        for (QueryParameter p : params) {

             Log.Debug(this, "Adding AND param " + p);
            if ((p.getValue() instanceof Number || p.getValue() instanceof Boolean) && p.getCondition() == QueryParameter.LIKE) {
                throw new IllegalArgumentException("You cannot mix LIKE and boolean/number values!");
            }

            String val = "";
            if (p.getValue() instanceof Number) {
                val = String.valueOf(p.getValue());
            } else if (p.getValue() instanceof Boolean) {
                if ((Boolean) p.getValue()) {
                    val = "1";
                } else {
                    val = "0";
                }
            } else {
                val = "'" + p.getValue() + "'";
            }

            switch (p.getCondition()) {
                case QueryParameter.EQUALS:
                    query += " AND " + p.getKey() + " = " + val + " ";
                    break;
                case QueryParameter.NOTEQUAL:
                    query += " AND " + p.getKey() + " <> " + val + " ";
                    break;
                case QueryParameter.LIKE:
                    query += " AND UPPER(" + p.getKey() + ") LIKE '%" + val.replace("'", "").toUpperCase() + "%'" + " ";
                    break;
            }
        }
    }

    /**
     * Add OR conditions
     * @param params
     */
    public void or(QueryParameter... params) {
        if (params.length < 2) {
            throw new IllegalArgumentException("OR conditions must have at least 2 parameters!");
        }

        query += " AND (";
        for (QueryParameter p : params) {

            Log.Debug(this, "Adding OR param " + p);

            if ((p.getValue() instanceof Number || p.getValue() instanceof Boolean) && p.getCondition() == QueryParameter.LIKE) {
                throw new IllegalArgumentException("You cannot mix LIKE and boolean/number values!");
            }

            String val = "";
            if (p.getValue() instanceof Number) {
                val = String.valueOf(p.getValue());
            } else if (p.getValue() instanceof Boolean) {
                if ((Boolean) p.getValue()) {
                    val = "1";
                } else {
                    val = "0";
                }
            } else {
                val = "'" + p.getValue() + "'";
            }

            switch (p.getCondition()) {
                case QueryParameter.EQUALS:
                    query += " OR " + p.getKey() + " = " + val + " ";
                    break;
                case QueryParameter.NOTEQUAL:
                    query += " OR " + p.getKey() + " <> " + val + " ";
                    break;
                case QueryParameter.LIKE:
                    query += " OR UPPER(" + p.getKey() + ") LIKE %" + val.toUpperCase() + "%" + " ";
                    break;
            }
        }
        query = query.replaceFirst("OR", "");
        query += ") ";
    }

    /**
     * @return the query
     */
    public String getQuery() {
        if (query.startsWith(" AND")) {
            query = query.substring(" AND ".length());
        }
        return query;
    }

    /**
     *
     * @param column
     * @param asc
     */
    public void setOrder(String column, boolean asc) {
        this.order = " ORDER BY " + column;
        if (asc) {
            order += " ASC ";
        } else {
            order += " DESC ";
        }
    }

    /**
     * @return the order
     */
    public String getOrder() {
        return order;
    }
}



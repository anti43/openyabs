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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mpv5.utils.arrays.ArrayUtilities;

/**
 *
 * @author andreas
 */
public class QueryCriteria2 {

    private String query = "";
    private String order = "";

    private List<QueryParameter> fields = new ArrayList<QueryParameter>();
    private boolean includeDeleted;

    /**
     * Add AND conditions
     *
     * @param params
     */
    public void and(QueryParameter... params) {
        for (QueryParameter p : params) {
            fields.add(p);
            //Log.Debug(this, "Adding AND param " + p);
            if ((p.getValue() instanceof Number || p.getValue() instanceof Boolean) && (p.getCondition() == QueryParameter.LIKE || p.getCondition() == QueryParameter.NOTLIKE)) {
                throw new IllegalArgumentException("You cannot mix LIKE and boolean/number values!");
            }

            String val;
            if (p.getValue() instanceof Number) {
                val = String.valueOf(p.getValue());
            } else if (p.getValue() instanceof Boolean) {
                if ((Boolean) p.getValue()) {
                    val = "1";
                } else {
                    val = "0";
                }
            } else {
                val = "'" + SqlText.literal(String.valueOf(p.getValue())) + "'";
            }

            switch (p.getCondition()) {
                case QueryParameter.GREATEREQUALS:
                    query += " AND " + p.getKey() + " >= " + val + " ";
                    break;
                case QueryParameter.LOWEREQUALS:
                    query += " AND " + p.getKey() + " <= " + val + " ";
                    break;
                case QueryParameter.EQUALS:
                    query += " AND " + equals(p, val) + " ";
                    break;
                case QueryParameter.NOTEQUAL:
                    query += " AND " + p.getKey() + " <> " + val + " ";
                    break;
                case QueryParameter.LIKE:
                    query += " AND UPPER(" + p.getKey() + ") LIKE '%" + like(p) + "%'" + " ";
                    break;
                case QueryParameter.NOTLIKE:
                    query += " AND UPPER(" + p.getKey() + ") NOT LIKE '%" + like(p) + "%'" + " ";
                    break;
            }
        }
    }

    /**
     * Add OR conditions
     *
     * @param params
     */
    public void or(List<QueryParameter> params) {
        if (params.isEmpty()) {
            return;
        } else if (params.size() == 1) {
            //Log.Debug(this, "Converting 1-sized OR list to AND!!");
            and(params.get(0));
            return;
        }
        /*else if (params.size() < 2) {
        throw new IllegalArgumentException("Params.size must be > 2, is " + params.size());
        }*/
        or(params.get(0), params.toArray(new QueryParameter[params.size()]));
    }

    /**
     * Add OR conditions
     *
     * @param param1
     * @param params
     */
    public void or(QueryParameter param1, List<QueryParameter> params) {
        or(param1, params.toArray(new QueryParameter[params.size()]));
    }

    /**
     * Add OR conditions
     *
     * @param param1
     * @param params
     */
    public void or(QueryParameter param1, QueryParameter... params) {
        if (params.length < 1) {
            return;
        }
        Object[] paramsx = ArrayUtilities.merge(new QueryParameter[]{param1}, params);
        query += " AND (";

        boolean firstrun = true;
        for (Object px : paramsx) {
            QueryParameter p = (QueryParameter) px;
            //Log.Debug(this, "Adding OR param " + p);
            fields.add(p);

            if ((p.getValue() instanceof Number || p.getValue() instanceof Boolean) && (p.getCondition() == QueryParameter.LIKE || p.getCondition() == QueryParameter.NOTLIKE)) {
                throw new IllegalArgumentException("You cannot mix LIKE and boolean/number values!");
            }

            String val = value(p);

            switch (p.getCondition()) {
                 case QueryParameter.GREATEREQUALS:
                    if (!firstrun) {
                        query += " OR " + p.getKey() + " >= " + val + " ";
                    } else {
                        query += " " + p.getKey() + " >= " + val + " ";
                    }
                    break;
                 case QueryParameter.LOWEREQUALS:
                    if (!firstrun) {
                        query += " OR " + p.getKey() + " <= " + val + " ";
                    } else {
                        query += " " + p.getKey() + " <= " + val + " ";
                    }
                    break;
                case QueryParameter.EQUALS:
                    if (!firstrun) {
                        query += " OR " + equals(p, val) + " ";
                    } else {
                        query += " " + equals(p, val) + " ";
                    }
                    break;
                case QueryParameter.NOTEQUAL:
                    if (!firstrun) {
                        query += " OR " + p.getKey() + " <> " + val + " ";
                    } else {
                        query += " " + p.getKey() + " <> " + val + " ";
                    }
                    break;
                case QueryParameter.LIKE:
                    if (!firstrun) {
                        query += " OR UPPER(" + p.getKey() + ") LIKE '%" + like(p) + "%'" + " ";
                    } else {
                        query += " UPPER(" + p.getKey() + ") LIKE '%" + like(p) + "%'" + " ";
                    }
                    break;
                case QueryParameter.NOTLIKE:
                    if (!firstrun) {
                        query += " OR UPPER(" + p.getKey() + ") NOT LIKE '%" + like(p) + "%'" + " ";
                    } else {
                        query += " UPPER(" + p.getKey() + ") NOT LIKE '%" + like(p) + "%'" + " ";
                    }
                    break;
            }
            firstrun = false;
        }
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
            order += "      ";
        }
    }

    /**
     * @return the order
     */
    public String getOrder() {
        return order;
    }

    /**
     *
     * @param queryParameter
     */
    public void is(QueryParameter queryParameter) {
        and(queryParameter);
    }

    /**
     *
     * @param params
     */
    public void and(List<QueryParameter> params) {
        if (params.isEmpty()) {
            return;
        }
        and(params.toArray(new QueryParameter[params.size()]));
    }

    /**
     * All fields
     *
     * @return
     */
    public List<QueryParameter> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public boolean getIncludeInvisible() {
        return includeDeleted;
    }

    /**
     * @param in the in to set
     */
    public void setIncludeInvisible(boolean in) {
        this.includeDeleted = in;
    }

    void list(List<QueryParameter> list) {
        if (list.size() > 0) {
            QueryParameter p = list.get(0);
            String key = p.getKey();
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                QueryParameter get = list.get(i);
                b.append(value(get)).append(",");
            }
            query += " AND " + key + " IN (" + b + value(p) + ")";
        }

    }

    private String value(QueryParameter p) {
        String val;
        if (p.getValue() instanceof Number) {
            val = String.valueOf(p.getValue());
        } else if (p.getValue() instanceof Boolean) {
            if ((Boolean) p.getValue()) {
                val = "1";
            } else {
                val = "0";
            }
        } else {
            val = "'" + SqlText.literal(String.valueOf(p.getValue())) + "'";
        }
        return val;
    }

    /**
     * @return column = value; a text with ' also finds its old form (see SqlText)
     */
    private static String equals(QueryParameter p, String val) {
        if (p.getValue() instanceof String) {
            return SqlText.equalsText(p.getKey(), (String) p.getValue());
        }
        return p.getKey() + " = " + val;
    }

    /**
     * @return the upper case value for LIKE '%...%', escaped; a ' matches ' and the old `
     */
    private static String like(QueryParameter p) {
        return SqlText.likeText(String.valueOf(p.getValue()).toUpperCase());
    }
}

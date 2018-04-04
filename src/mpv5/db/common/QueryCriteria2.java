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
                val = "'" + p.getValue() + "'";
            }

            switch (p.getCondition()) {
                case QueryParameter.GREATEREQUALS:
                    query += " AND " + p.getKey() + " >= " + val + " ";
                    break;
                case QueryParameter.LOWEREQUALS:
                    query += " AND " + p.getKey() + " <= " + val + " ";
                    break;
                case QueryParameter.EQUALS:
                    query += " AND " + p.getKey() + " = " + val + " ";
                    break;
                case QueryParameter.NOTEQUAL:
                    query += " AND " + p.getKey() + " <> " + val + " ";
                    break;
                case QueryParameter.LIKE:
                    query += " AND UPPER(" + p.getKey() + ") LIKE '%" + val.replace("'", "").toUpperCase() + "%'" + " ";
                    break;
                case QueryParameter.NOTLIKE:
                    query += " AND UPPER(" + p.getKey() + ") NOT LIKE '%" + val.replace("'", "").toUpperCase() + "%'" + " ";
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
                        query += " OR " + p.getKey() + " = " + val + " ";
                    } else {
                        query += " " + p.getKey() + " = " + val + " ";
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
                        query += " OR UPPER(" + p.getKey() + ") LIKE '%" + val.toUpperCase().replace("'", "") + "%'" + " ";
                    } else {
                        query += " UPPER(" + p.getKey() + ") LIKE '%" + val.toUpperCase().replace("'", "") + "%'" + " ";
                    }
                    break;
                case QueryParameter.NOTLIKE:
                    if (!firstrun) {
                        query += " OR UPPER(" + p.getKey() + ") NOT LIKE '%" + val.toUpperCase().replace("'", "") + "%'" + " ";
                    } else {
                        query += " UPPER(" + p.getKey() + ") NOT LIKE '%" + val.toUpperCase().replace("'", "") + "%'" + " ";
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
            val = "'" + p.getValue() + "'";
        }
        return val;
    }
}

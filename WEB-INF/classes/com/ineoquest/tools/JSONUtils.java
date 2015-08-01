package com.ineoquest.tools;
            
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.PrintWriter;

/*package*/
class JSONUtils {

    /*package*/
    static JSONArray convert(ResultSet rs, int start, int limit,
            SearchContext searchContext) {

        JSONArray list = new JSONArray();

        try {
            // Determine how big result set is
            int totalRecords = Utils.getNumRows(rs);

            if (totalRecords > 0) {
                
                int startingRow = Math.max(1, Math.min(start, totalRecords));
                int endingRow = Math.min(start + limit, totalRecords);

                // Move cursor to start (bounded by start >= 1 <= last
                rs.absolute(startingRow);

                for (int i = startingRow; i <= endingRow; i++) {
                    list.add(encodeRow(rs, searchContext));

                    // Advance cursor to next result
                    if (! rs.next()) {
                        break;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Unable to convert result set: " +
                    e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
    

    private static JSONObject encodeRow(ResultSet rs,
            SearchContext searchContext)

        throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();
        JSONObject jsonObject = new JSONObject();

        for (int column = 1; column <= metaData.getColumnCount(); column++) {
            String columnName = metaData.getColumnName(column);
            int columnType = metaData.getColumnType(column);
            String value = null;
            Pattern pattern = null;

            if (searchContext != null) {
                pattern = searchContext.getPattern(columnName);
            }

            switch (columnType) {

            case Types.BIGINT:
                Long aLong = rs.getLong(column);
                value = String.valueOf(aLong);
                break;

            case Types.DECIMAL:
            case Types.INTEGER:
                Integer anInt = rs.getInt(column);
                value = String.valueOf(anInt);
                break;

            case Types.BOOLEAN:
                Boolean aBool = rs.getBoolean(column);
                value = String.valueOf(aBool);
                break;

            case Types.DOUBLE:
                Double aDouble = rs.getDouble(column);
                value = String.valueOf(aDouble);
                break;

            case Types.FLOAT:
                Float aFloat = rs.getFloat(column);
                value = String.valueOf(aFloat);
                break;

            case Types.NVARCHAR:
            case Types.VARCHAR:
                String aString = rs.getString(column);
                value = aString;
                break;

            default:
                System.err.println("Don't know how to encode SQL type: " +
                        columnType + ", data for this column is " +
                        rs.getObject(column));
            }

            if (pattern != null) {
                Matcher matcher = pattern.matcher(value);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(sb, "<span class=\"hit\">"
                            + matcher.group() + "</span>");
                }
                matcher.appendTail(sb);
                value = sb.toString();
            }
            
            jsonObject.put(columnName, value);
        }

        return jsonObject;
    }


    static JSONObject getMetaData(ResultSet rs, String db, String table,
            PrintWriter out, boolean refresh) throws SQLException {

        // Fetch metadata from cache if it exists (note: this call
        // is synchronized)
        JSONObject metadata = MetaDataCache.get(table);
        
        // If not, create it now
        if (metadata == null || refresh) {
            JSONArray columns = new JSONArray();

            ResultSetMetaData rsMetaData = rs.getMetaData();

            for (int col = 1; col <= rsMetaData.getColumnCount(); col++) {
                JSONObject columnData = new JSONObject();
                String colName = rsMetaData.getColumnName(col);
                JSONObject colObj = getMetaDataForColumn(db, table, colName, out);
                if (colObj != null) {
                    columns.add(colObj);
                }
            }

            // Create an object to hold metadata response columns
            metadata = new JSONObject();
            metadata.put("columns", columns);
            
            // Update cache (note: this call is synchronized)
            MetaDataCache.put(table, metadata);
        }            
        
        return metadata;
    }
    

    static JSONObject getMetaDataForColumn(String db, String table,
            String columnName, PrintWriter out) 
        throws SQLException {
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject colObj = null;

        try {
            
            conn = Utils.getConnection(db);
            stmt = conn.createStatement();

            // Get column details
            ColumnInfo columnInfo = ColumnCache.getColumnInfo(db, table, columnName);
            
            // Set up default values for fields
            boolean isEnabled = true;
            boolean isEnumField = false;
            String refTable = table;
            String refColumn = columnName;
            String title = Utils.splitAndLowerCamelCase(columnName);
            long width = 120;

            // See if a metadata override exists in MetaData table
            String sql = String.format("SELECT * FROM MetaData where " +
                    "tableName='%s' and columnName='%s';", table, columnName);;

            rs = stmt.executeQuery(sql);

            // If table/column combo exists in MetaData table use that
            if (rs.isBeforeFirst()) {
                rs.next();
                isEnabled = rs.getBoolean("isEnabled");
                isEnumField = rs.getBoolean("isEnumField");
                refTable = rs.getString("refTable");
                refColumn = rs.getString("refColumn");
                title = rs.getString("title");
                width = rs.getInt("width");
            } else {
                // Otherwise, fall back on values in the ColumnInfo object from
                // column cache.
                if (columnInfo.hasForeignKey()) {
                    isEnumField = true;
                    refTable = columnInfo.foreignTable;
                    refColumn = columnInfo.foreignColumn;
                }

                isEnabled = columnInfo.getConfigBoolean("isEnabled", isEnabled);
                width = columnInfo.getConfigLong("width", width);
                title = columnInfo.getConfigString("title", title);
            }
            
            // Now build json metadata
            if (isEnabled) {
                colObj = new JSONObject();                
                // Build json metadata object
                colObj.put("text", title);
                colObj.put("dataIndex", columnName);
                colObj.put("width", width);
                    
                if (isEnumField) {
                    colObj.put("filter", "list");
                } else {
                    JSONObject filter = new JSONObject();
                    filter.put("type", "string");
                    JSONObject itemDefaults = new JSONObject();
                    itemDefaults.put("emptyText", "Search for...");
                    filter.put("itemDefaults", itemDefaults);
                    colObj.put("filter", filter);
                }
            
                JSONObject editor = new JSONObject();
                editor.put("xtype", "combobox");
                
                JSONObject listConfig = new JSONObject();

                // Add common properties
                editor.put("editable", true);
                editor.put("typeAhead", true);
                editor.put("forceSelection", false);
                editor.put("displayField", refColumn);
                editor.put("queryMode", "remote");
                editor.put("minChars", 1);
                    
                if (isEnumField) {
                    editor.put("enableKeyEvents", true);
                    editor.put("valueField", "id");
                    listConfig.put("loadingText", "loading...");
                    JSONObject listeners = new JSONObject();
                    listeners.put("select", "null");
                    listeners.put("keyup", "null");
                    editor.put("listeners", listeners);
                    
                } else {
                    editor.put("selectOnTab", false);
                    editor.put("valueField", refColumn);
                    editor.put("hideTrigger", true);
                    listConfig.put("loadingText", "searching...");
                    listConfig.put("emptyText", "no matches.");
                }
            
                editor.put("listConfig", listConfig);

                JSONObject store = new JSONObject();

                JSONObject fieldObj = new JSONObject();
                fieldObj.put("name", refColumn);
                
                JSONArray fields = new JSONArray();
                fields.add(fieldObj);
                
                store.put("fields", fields);
                store.put("extend", "Ext.data.Store");
                
                if (! isEnumField) {
                    store.put("remoteFilter", true);
                }
                
                JSONObject proxy = new JSONObject();
                proxy.put("type", "ajax");

                String url = String.format("json?op=choices&db=%s&table=%s&field=%s&refresh",
                        db, refTable, refColumn);

                // If this is an enum field, tack on a url param that indicates this.
                // Later, when the ChoiceHandler is invoked, the presence of this
                // param will signal the generateion of a "Modify..." entry at the
                // end of the pick list.
                if (isEnumField) {
                    url += "&isEnum";
                }

                proxy.put("url", url);
                
                JSONObject reader = new JSONObject();
                reader.put("type", "json");
                reader.put("rootProperty", "data");
            
                proxy.put("reader", reader);
                store.put("proxy", proxy);
                editor.put("store", store);
                colObj.put("editor", editor);
            }            

        } finally {
            Utils.cleanup(rs);
            Utils.cleanup(stmt);
            Utils.cleanup(conn);
            return colObj;
        }
    }
}

    

    

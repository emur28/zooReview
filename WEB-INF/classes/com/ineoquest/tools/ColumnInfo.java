package com.ineoquest.tools;

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

// Column
class ColumnInfo {

    // All package scoped variables (basically a struct)
    String dbName = null;
    String tableName = null;
    String columnName = null;
    String title = null;
    int index = -1;
    int dataType = -1;
    int columnSize = -1;
    String comment = null;
    String foreignTable = null;
    String foreignColumn = null;
    JSONObject config = null;
    
    ColumnInfo(String dbName, String tableName, String columnName,
            int index, int dataType, int columnSize, String comment,
            String foreignTable, String foreignColumn) {

        this.dbName = dbName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.title = Utils.splitAndLowerCamelCase(columnName);
        this.index = index;
        this.dataType = dataType;
        this.columnSize = columnSize;
        this.comment = comment;
        this.foreignTable = foreignTable;
        this.foreignColumn = foreignColumn;

        if (this.comment != null && this.comment.startsWith("{")) {
            System.out.println("parsing json comment: " + comment);
            this.config = (JSONObject)JSONValue.parse(this.comment);
            System.out.println("result:" + this.config);
        }
    }

    boolean hasForeignKey() {
        return (foreignTable != null);
    }
    
    
    void setForeignKeyInfo(String foreignTable, String foreignColumn) {
        this.foreignTable = foreignTable;
        this.foreignColumn = foreignColumn;
    }
    

    public String asSQL() {
        String result = null;
        
        if (hasForeignKey()) {
            result = String.format("`%s`.`%s`.`%s` as `%s`",
                    dbName, foreignTable, foreignColumn, columnName);
        } else {
            result = String.format("`%s`.`%s`.`%s` as `%s`",
                    dbName, tableName, columnName, columnName);
        }

        return result;
    }
    
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{dbName:").append(dbName)
                .append(", tableName:").append(tableName)
                .append(", columnName:").append(columnName)
                .append(", title:").append(title)
                .append(", index:").append(index)
                .append(", dataType:").append(dataType)
                .append(", columnSize:").append(columnSize)
                .append(", comment:").append(comment)
                .append(", foreignTable:").append(foreignTable)
                .append(", foreignColumn:").append(foreignColumn)
                .append(", config:{");

        if (config != null) {
            for (Object key : config.keySet()) {
                String s = (String)key;
                sb.append(s).append("-->").append(config.get(s)).append(" ");
            }
            sb.append("}");
        } else {
            sb.append("null}");
        }

        sb.append(", asSQL:").append(asSQL());
        sb.append("}");
        return sb.toString();
    }

    boolean getConfigBoolean(String key, boolean defValue) {
        boolean result = defValue;

        if (config != null) {
            Boolean b = (Boolean)config.get(key);
            if (b != null) {
                result = b;
            }
        }

        return result;
    }


    String getConfigString(String key, String defValue) {
        String result = defValue;

        if (config != null) {
            String s = (String)config.get(key);
            if (s != null) {
                result = s;
            }
        }

        return result;
    }


    int getConfigInt(String key, int defValue) {
        int result = defValue;

        if (config != null) {
            Integer i = (Integer)config.get(key);
            if (i != null) {
                result = i;
            }
        }

        return result;
    }


    long getConfigLong(String key, long defValue) {
        long result = defValue;

        if (config != null) {
            Long i = (Long)config.get(key);
            if (i != null) {
                result = i;
            }
        }

        return result;
    }

}



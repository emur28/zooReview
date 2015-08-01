package com.ineoquest.tools;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ColumnCache {

    private static HashMap<String, HashMap<String,
        LinkedHashMap<String, ColumnInfo>>> CACHE =
        new HashMap<String, HashMap<String,
            LinkedHashMap<String, ColumnInfo>>>();
    

    static ColumnInfo getColumnInfo(String db, String table, String column)
        throws Exception {

        ColumnInfo columnInfo = null;
        
        synchronized(CACHE) {
            HashMap<String, ColumnInfo> columnMap = getAllColumnInfo(db,table);
            columnInfo = columnMap.get(column);
        }

        return columnInfo;
    }
    
    
    static LinkedHashMap<String, ColumnInfo> getAllColumnInfo(
            String db, String table) 
        throws Exception {

        LinkedHashMap<String, ColumnInfo> columnMap = null;
        
        synchronized(CACHE) {

            // Get tables that we've discovered in this db
            HashMap<String, LinkedHashMap<String, ColumnInfo>> tableMap
                    = CACHE.get(db);

            // Haven't learned about any tables in this DB yet.  Discover this
            // table and add it to map.
            if (tableMap == null) {
                System.out.println("Cache Miss for " + db + "." + table);
                // Create new table map and add column map to it
                tableMap = new HashMap<String,
                        LinkedHashMap<String, ColumnInfo>>();
                CACHE.put(db, tableMap);
            }

            // Lookup columns for this table.  If null, we probably haven't
            // learned about them yet.
            columnMap = tableMap.get(table);

            if (columnMap == null) {
                // Discover columns in table
                if ((columnMap = getColumnsForTable(db, table)) != null) {
                    tableMap.put(table, columnMap);
                } else {
                    throw new Exception("Table " + table
                            + " does not exist in " + db + " database.");
                }
            }

            return columnMap;
        }
    }


    static ArrayList<ColumnInfo> getAllColumnInfoAsList(
            String db, String table) throws Exception {

        LinkedHashMap<String, ColumnInfo> map = getAllColumnInfo(db, table);
        ArrayList<ColumnInfo> result = new ArrayList<ColumnInfo>();
        result.addAll(map.values());
        return result;
    }
    
    
    // Given a <table> within a <db> returns a nest hasHashMap of column name
    // to ColumnInfo object
//    private static HashMap<String, HashMap<String, ColumnInfo>>
//            buildTableData(String db, String table) 

    private static LinkedHashMap<String, ColumnInfo> getColumnsForTable(
            String db, String table) throws Exception {

        LinkedHashMap<String, ColumnInfo> columnMap =
                new LinkedHashMap<String, ColumnInfo>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet colRs = null;
        ResultSet viewRs = null;
        ResultSet fkRs = null;

        try {
            // Get connect to database
            conn = Utils.getConnection(db);

            // Get database meta data and column info
            DatabaseMetaData dbm = conn.getMetaData();
            colRs = dbm.getColumns(null, null, table, null);

            // Grab all the useful bits of metadata for this table
            // and store in HashMap that maps column to ColumnInfo.
            int index = 1;
            while(colRs.next()) {
                String columnName = colRs.getString(4);
                ColumnInfo ci = new ColumnInfo(db, table, columnName, index,
                        colRs.getInt(5), colRs.getInt(7), colRs.getString(12),
                        null, null);
                 
               columnMap.put(columnName, ci);
               index++;
            }

            // Have basic column information.  See if this is a view
            String view = String.format("select VIEW_DEFINITION from "
                    + "`information_schema`.`VIEWS` where `TABLE_SCHEMA`='%s'"
                    + " and `TABLE_NAME`='%s'", db, table);
            
            stmt = conn.createStatement();
            viewRs = stmt.executeQuery(view);
            
            // If this is a view
            if (viewRs.isBeforeFirst()) {
                viewRs.next();
                String viewSpec = viewRs.getString(1);
                // Then parse up view spec for "foreign" keys
                ViewParser.parseViewSpec(viewSpec, columnMap);

            } else {
            
                // Otherwise get the foreign keys for this table 
                fkRs = dbm.getImportedKeys(null, null, table);
            
                while(fkRs.next()) {
                    String columnName = fkRs.getString(8);
                    ColumnInfo ci = columnMap.get(columnName);
                    if (ci == null) {
                        throw new Exception(String.format("Couldn't store "
                                        + "foreign keys for table \"%s\", "
                                        + "column \"%s\". Column not found in "
                                        + "column map!", table, columnName));
                    }

                    ci.setForeignKeyInfo(fkRs.getString(3), fkRs.getString(4));
                }
            }

        } finally {
            Utils.cleanup(viewRs);viewRs = null;
            Utils.cleanup(colRs); colRs = null;
            Utils.cleanup(fkRs);  fkRs  = null;
            Utils.cleanup(stmt);  stmt  = null;
            Utils.cleanup(conn);  conn  = null;
            return columnMap;
        }

    }


    static void clear(PrintWriter out) {
        
        synchronized(CACHE) {
            if(out != null)
                out.println("ColumnCache: cleared " + CACHE.size() + " entry(s)");
            CACHE.clear();
        }
    }

    
    static void dump(PrintWriter out) {
        
        out.println("ColumnCache:");
        
        synchronized(CACHE) {

            for (String db : CACHE.keySet()) {

                out.println(String.format("database %s -> ", db));

                HashMap<String, LinkedHashMap<String, ColumnInfo>> tableMap
                        = CACHE.get(db);

                for (String table : tableMap.keySet()) {

                    out.println(String.format("  table %s -> ", table));

                    LinkedHashMap<String, ColumnInfo> columnMap =
                            tableMap.get(table);
            
                    for (String column : columnMap.keySet()) {
                        ColumnInfo ci = columnMap.get(column);
            
                        out.println(String.format("    column %s -> %s",
                                        column, ci));
                    }
                }
            }
        }
    }
}

package com.ineoquest.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

import java.util.HashMap;
import org.json.simple.JSONObject;

class FKCache {

    private static HashMap<String, HashMap<String, String>> FKCACHE =
            new HashMap<String, HashMap<String,String>>();

    public static String get(final String table, final String column) {

        String result = null;

        synchronized(FKCACHE) {
            HashMap<String,String> map = FKCACHE.get(table);

            if (map != null) {
                result = map.get(column);
            }
        }

        return result;
    }


    public static String[] getTuple(final String table, final String column) {
        String tuple = get(table, column);
        String[] result;

        if (tuple != null) {
            result = tuple.split(".");
        } else {
            result = new String[0];
        }
    
        return result;
    }
    
    
    public static void put(final String table, String column,
            final String foreignTableCol) {

        synchronized(FKCACHE) {
            HashMap<String, String> map = FKCACHE.get(table);

            if (map == null) {
                map = new HashMap<String, String>();
                FKCACHE.put(table, map);
            }

            map.put(column, foreignTableCol);
        }
    }


    public static void build(final Connection conn, final String table) 
        throws SQLException {
        
        HashMap<String, String> map = null;
        synchronized(FKCACHE) {
            map = FKCACHE.get(table);
        }

        if (map == null) {
            DatabaseMetaData dbm = conn.getMetaData();

            ResultSet rs = dbm.getImportedKeys(null, null, table);
            
            while(rs.next()) {
                String column = rs.getString(8);
                String foreignTableCol = String.format("%s.%s",
                        rs.getString(3), rs.getString(4));

                put(table, column, foreignTableCol);
            }

            Utils.cleanup(rs);
        }
    }
}

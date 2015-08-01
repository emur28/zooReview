package com.ineoquest.tools;

import java.io.PrintWriter;

import java.util.HashMap;
import org.json.simple.JSONObject;

class SearchClauseCache {

    private static HashMap<String, HashMap<String, String>> CACHE =
            new HashMap<String, HashMap<String,String>>();

    static String get(final String dbName,
            final String tableName) {

        synchronized(CACHE) {
            String result = null;
            HashMap<String, String> tableMap = CACHE.get(dbName);

            if (tableMap != null) {
                result = tableMap.get(tableName);
            }

            return result;
        }
    }

    static void put(final String dbName,
            final String tableName, final String searchClause) {

        synchronized(CACHE) {
            HashMap<String, String> tableMap = CACHE.get(dbName);

            if (tableMap == null) {
                tableMap = new HashMap<String, String>();
                CACHE.put(dbName, tableMap);
            } 
            
            tableMap.put(tableName, searchClause);
        }
    }


    static void clear(PrintWriter out) {

        synchronized(CACHE) {
            out.println("SearchClauseCache: cleared " + CACHE.size() + " entry(s)");
            CACHE.clear();
        }
    }

    static void dump(PrintWriter out) {
        out.println("SearchClauseCache:");

        synchronized(CACHE) {
            for(String key: CACHE.keySet()) {
                out.println(String.format("  %s --> %s", key, CACHE.get(key)));
            }
        }
    }
}

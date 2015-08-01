package com.ineoquest.tools;

import java.io.PrintWriter;

import java.util.HashMap;
import org.json.simple.JSONObject;

class MetaDataCache {

    private static HashMap<String, JSONObject> METADATA =
            new HashMap<String, JSONObject>();

    static JSONObject get(final String tableName) {

        synchronized(METADATA) {
            return METADATA.get(tableName);
        }
    }

    static JSONObject put(final String tableName,
            final JSONObject jsonObject) {

        synchronized(METADATA) {
            return METADATA.put(tableName, jsonObject);
        }
    }

    static void clear(PrintWriter out) {
        
        synchronized(METADATA) {
            if(out != null)
                out.println("MetaDataCache: cleared " + METADATA.size() + " entry(s)");
            METADATA.clear();
        }
    }

    static void dump(PrintWriter out) {

        out.println("MetaDataCache:");

        synchronized(METADATA) {
            for(String key: METADATA.keySet()) {
                out.println(String.format("  %s --> %s", key, METADATA.get(key)));
            }
        }
    }
}

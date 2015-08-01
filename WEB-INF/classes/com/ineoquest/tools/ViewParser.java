package com.ineoquest.tools;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;

public class ViewParser {

    static Pattern selectSplitter =
            Pattern.compile("^select (.*) from .*");

    static Pattern columnSplitter =
            Pattern.compile("^(?:`[^`]+`\\.)?`([^`]+)`\\.`([^`]+)`\\ AS `([^`]+)`$");

    static void parseViewSpec(String viewSpec,
            HashMap<String, ColumnInfo> columnMap) throws Exception {

        Matcher selectMatcher = selectSplitter.matcher(viewSpec);

        if (selectMatcher.matches()) {
            String columnSpec = selectMatcher.group(1);
            String[] columnList = columnSpec.split(",");

            for (String column : columnList) {
                Matcher columnMatcher = columnSplitter.matcher(column);
                if (columnMatcher.matches()) {
                    ColumnInfo ci = columnMap.get(columnMatcher.group(3));
                    ci.setForeignKeyInfo(columnMatcher.group(1),
                            columnMatcher.group(2));
//                    System.out.println(columnMatcher.group(1) + "-" +
//                            columnMatcher.group(2) + "-" +
//                            columnMatcher.group(3));
                } else {
                    System.err.println("Warning: skipping column " + column
                            + " which didn't match column splitter regexp: "
                            + columnSplitter.toString());
                }
            }                

        } else {
            throw new Exception(String.format("Unable to parse view spec: \"%s\"",
                            viewSpec));
        }
    }


    // public static void main(String[] args) {
    //     if (args.length == 0) {
    //         System.err.println("Usage: <view-spec> [...<view-spec>]");
    //         return;
    //     }
    
        
    //     HashMap<String, ColumnInfo> foo = new HashMap<String, ColumnInfo>() {{
    //             put("a", new ColumnInfo("db", "x", "a", 0, 0, "{\"width\":100}", "fx", "fa"));
    //             put("b", new ColumnInfo("db", "x", "b", 0, 0, "{\"width\":100}", "fx", "fb"));
    //             put("c", new ColumnInfo("db", "x", "c", 0, 0, "{\"width\":100}", "fx", "fc"));
    //             put("d", new ColumnInfo("db", "x", "d", 0, 0, "{\"width\":100}", "fx", "fd"));
    //         }};
                                                                             
        
    //     try {
            
    //         for(String view : args) {
    //             parseViewSpec(view, foo);
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}


package com.ineoquest.tools;

import java.io.PrintWriter;

import java.net.URLDecoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;


/*package-scope*/
class Utils {

    // Map CamelCase and/or under_score terms to space delimited lowercase
    public static String splitAndLowerCamelCase(String s) {

        return s.replaceAll("(?<=[A-Z_])(?=[A-Z_][a-z])|"
                + "(?<=[^A-Z_])(?=[A-Z_])|"
                + "(?<=[A-Za-z_])(?=[^A-Za-z_])", " ")
                .replaceAll("_", "")
                .toLowerCase();
    }

    public static String getParameter(
            HttpServletRequest req,
            String property,
            String defaultValue) {

        String result = req.getParameter(property);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }


    public static int getParameterAsInt(
            HttpServletRequest req,
            String property,
            int defaultValue) throws Exception {

        String strResult = getParameter(req, property, null);
        int result = defaultValue;
        
        if (strResult != null) {
            result = Integer.parseInt(strResult);
        }            

        return result;
    }


    public static boolean getFlag(
            HttpServletRequest req,
            String property) {

        String strResult = getParameter(req, property, null);
        boolean result = (strResult == null) ? false : true;
        return result;
    }

    
    static Connection getConnection(String database) 
        throws SQLException, ClassNotFoundException {

        Class.forName(Constants.JDBC_DRIVER);

        return DriverManager.getConnection(Constants.DB_URL + database,
                Constants.USER, Constants.PASS);
    }
   

    static int getNumRows(ResultSet rs) throws SQLException {
        rs.last();
        int row = rs.getRow();
        rs.beforeFirst();
        return row;
    }
    

    static LinkedList<String> getQueryStringStopList(
            HttpServletRequest request) {

        String q = request.getQueryString();
        String[] pieces = q.split("&");
        LinkedList<String> stopList = new LinkedList<String>();
        
        for (String piece : pieces) {
            String[] param = piece.split("=");
            stopList.add(param[0]);
        }

        return stopList;
    }
    
    static SearchContext getSearchContext(final String db,
            final String table, final String search)
        throws Exception {
        
        SearchContext context = null;

        if (db != null && table != null && search != null) {
            ArrayList<String>columns = getColumns(db, table);
            context = SearchParser.parse(search, columns);
        }

        return context;
    }
    
    
    static ArrayList<String> getColumns(String db, String table) 
        throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        HashMap<String, ColumnInfo> map = ColumnCache.getAllColumnInfo(db, table);
        result.addAll(map.keySet());
        return result;
    }
    
    // static ArrayList<String> getColumns(String db, String table)
    //     throws Exception {
        
    //     Connection conn = null;
    //     Statement stmt = null;
    //     ResultSet rs = null;
    //     ArrayList<String> columns = ColumnCache.get(db, table);
        
    //     try {

    //         if (columns == null) {
    //             columns = new ArrayList<String>();

    //             conn = Utils.getConnection(db);
    //             stmt = conn.createStatement();

    //             String sql = String.format("SELECT COLUMN_NAME FROM "
    //                     + "information_schema.COLUMNS where " +
    //                     "TABLE_NAME='%s'", table);

    //             rs = stmt.executeQuery(sql);
    //             StringBuilder sb = new StringBuilder(" where ");

    //             // Build search clause (e.g. " product like '{0}%' or user
    //             // like '{0}%' ... etc."
    //             while(rs.next()) {
    //                 columns.add(rs.getString(1));
    //             }

    //             ColumnCache.put(db, table, columns);
    //         }
    //     } finally {
    //         cleanup(rs);
    //         cleanup(stmt);
    //         cleanup(conn);
    //         return columns;
    //     }
    // }
    
    
    static String getSearchClause(final String db,
            final String table,
            final String search) {

        String decodedSearch = null;

        String result = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            if (search != null) {
                
                // Decode search param and lookup search clause in cache
                decodedSearch = URLDecoder.decode(search, "ISO-8859-1");
                String searchClause = SearchClauseCache.get(db, table);

                // Cache miss
                if (searchClause == null) {
                    // Open up connection to information_schema database and
                    // get the column nams for this db and table.
                    conn = Utils.getConnection(db);
                    stmt = conn.createStatement();

                    String sql = String.format("SELECT COLUMN_NAME FROM "
                            + "information_schema.COLUMNS where " +
                            "TABLE_NAME='%s'", table);

                    rs = stmt.executeQuery(sql);
                    StringBuilder sb = new StringBuilder(" where ");

                    // Build search clause (e.g. " product like '{0}%' or user
                    // like '{0}%' ... etc."
                    while(rs.next()) {
                        sb.append(rs.getString(1))
                                .append(" rlike ''{0}'' or ");
//                                .append(" like ''%{0}%'' or ");
                    }
                            
                    // Delete the last " or" clause off of the end
                    sb.delete(Math.max(0, sb.length() - 4), sb.length());
                    searchClause = sb.toString();

                    // Store in cache for next time
                    SearchClauseCache.put(db, table, searchClause);
                }
                
                // format into a search clause (i.e. replace {0} tokens with
                // search text
                if (decodedSearch.startsWith("!")) {
                    decodedSearch = decodedSearch.substring(1);
                    searchClause = searchClause.replace(" rlike ",
                            " not rlike ").replace(" or ", " and ");
                }

                result = MessageFormat.format(searchClause, decodedSearch);
            
            }

        } catch (Exception e) {
            
        } finally {
            cleanup(rs);
            cleanup(stmt);
            cleanup(conn);
            return result;
        }
    }
    
    
    static void cleanup(PrintWriter out) {
        if (out != null) {
            try { out.close(); } catch (Exception andIgnore) { }
        }
    }

    static void cleanup(Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (Exception andIgnore) { }
        }
    }

    static void cleanup(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (Exception andIgnore) { }
        }
    }

    static void cleanup(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException andIgnore) {}
        }
    }
}

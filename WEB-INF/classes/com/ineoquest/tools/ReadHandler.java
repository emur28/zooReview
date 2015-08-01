package com.ineoquest.tools;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/*package-scope*/
class ReadHandler implements RequestHandler {

    // Value passed via start parameter to signal that metadata is needed
    private static final int WANT_METADATA = -1;
    
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt)
        throws Exception {
    
        JSONObject jsonResponse = new JSONObject();
        
        int start = Utils.getParameterAsInt(request, "start", -1);
        int limit = Utils.getParameterAsInt(request, "limit", 9999999);

        String db = Utils.getParameter(request, "db", "test");
        String table = Utils.getParameter(request, "table", "AllElements");
        String search = Utils.getParameter(request, "search", null);
        boolean refresh = Utils.getFlag(request, "refresh");
        boolean debug = Utils.getFlag(request, "debug");
        
        PrintWriter out = response.getWriter();
        ResultSet rs = null;
        
        String sql = null;
        
        try {
//            String searchClause = Utils.getSearchClause(db, table, search);
            SearchContext searchContext = Utils.getSearchContext(db, table, search);
            
            // Get results from dtabase
            StringBuilder sb = new StringBuilder("SELECT * FROM ")
                    .append("`").append(table).append("`");

            if (searchContext != null) {
                sb.append(searchContext.getWhereClause());
            }

            sql = sb.toString();

            if (debug) {
                out.println("SQL Query: " + sql);
            }
            
            rs = stmt.executeQuery(sql);

            // Convert data to JSON format
            JSONArray data = JSONUtils.convert(rs, start, limit, searchContext);
            int total = Utils.getNumRows(rs);

            // Add total and data fields to response
            jsonResponse.put("total", total);
            jsonResponse.put("data", data);

            // If start is -1 (or &refresh) then we must also fetch metadata
            // for table.  Add that in now
            if ((start == WANT_METADATA) || refresh) {

                JSONObject metaData = JSONUtils.getMetaData(rs, db, table,
                        out, refresh);
                jsonResponse.put("metaData", metaData);
            }
            
            // Finally, write out the response object
            out.write(jsonResponse.toString());
            
        } catch (SQLException s) {
            String message = String.format("Encountered error: %s\nsql: %s",
                    s.getMessage(), (sql == null ? "null" : sql));
            throw new Exception(message, s);

        } catch (Exception e) {
            throw e;
            
        } finally {
            Utils.cleanup(rs);
        }
	}
}



package com.ineoquest.tools;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/*package-scope*/
class ChoiceHandler implements RequestHandler {

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt)
        throws Exception {
    
        JSONObject jsonResponse = new JSONObject();
        
        String db = Utils.getParameter(request, "db", "test");
        String table = Utils.getParameter(request, "table", null);
        String column = Utils.getParameter(request, "field", null);
        String query = Utils.getParameter(request, "query", null);
        boolean debug = Utils.getFlag(request, "debug");
        boolean isEnum = Utils.getFlag(request, "isEnum");

        PrintWriter out = response.getWriter();
        ResultSet rs = null;
        
        try {
            if (column == null) {
                throw new Exception("Error: field not specified");
            }
            if (db == null) {
                throw new Exception("Error: db not specified");
            }

            String likeClause;

            if (query != null) {
                likeClause=String.format("where %s like '%s%%'",
                        column, query);
                
            } else {
                likeClause="";
            }

            // Get results from database
            String sql = String.format(
                    "SELECT %s, id FROM %s %s group by %s asc",
                    column, table, likeClause, column);

            if (debug) {
                out.println("query: " + sql);
            }            

            rs = stmt.executeQuery(sql);
            int total = Utils.getNumRows(rs);

            if (debug) {
                out.println("num results: " + total);
            }

            // Convert data to JSON format
            JSONArray data = JSONUtils.convert(rs, 1, total, null);

            // If this column is marked as an enum (isEnum) and there are
            // entries to return then add a "Modify" entry to the end of
            // the list.  The GUI will look for this an present a popup
            // allowing the user to add/edit entries.
            if (isEnum && (data.size() > 0)) {
                JSONObject sample = (JSONObject)data.get(data.size() - 1);
                JSONObject modifyEntry = new JSONObject();

                // Since we don't know what the key fields are we special
                // case id.  An "id" field gets -1, any other field gets
                // the text "Modify..."
                for (Object obj : sample.keySet()) {
                    String key = (String)obj;
                    if (key.equalsIgnoreCase("id")) {
                        modifyEntry.put(key, -1);
                    } else {
                        modifyEntry.put(key, "Modify...");
                    }
                }

                data.add(modifyEntry);
            }

            
            // Add total and data fields to response
            jsonResponse.put("data", data);

            // Finally, write out the response object
            out.write(jsonResponse.toString());
            
        } catch (SQLException s) {
            throw new Exception("Couldn't read because: " + s.getMessage(), s);

        } catch (Exception e) {
            throw e;
            
        } finally {
            Utils.cleanup(rs);
        }
	}
}



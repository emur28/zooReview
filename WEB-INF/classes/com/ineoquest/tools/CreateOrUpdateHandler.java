package com.ineoquest.tools;

import java.io.PrintWriter;

import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/*package-scope*/
class CreateOrUpdateHandler implements RequestHandler {

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt)
        throws Exception {

        String db = Utils.getParameter(request, "db", "test");
        String table = Utils.getParameter(request, "table", "AllElements");
        String id = Utils.getParameter(request, "id", null);
        boolean debug = Utils.getFlag(request, "debug");
        
        if (id == null) {
            throw new Exception("Error: create/update missing id parameter");
        }
        
		PrintWriter out = response.getWriter();

        // Get mapping of all parameters (query and POSTed in content body)
        Map<String,String[]> map = request.getParameterMap();

        // Get list of query parameters to form a stop list
        LinkedList<String> stopList = Utils.getQueryStringStopList(request);

        if (debug) {
            out.println("stopList:" + stopList);
        }
        
        String sql;
        
        if (id.equals("-1")) {
            sql = buildInsertStatement(db, table, map, stopList, debug, out);
        } else {
            sql = buildUpdateStatement(db, table, map, stopList, id, debug, out);
        }
        
        if (debug) {
            out.println("Resultant sql query is: " + sql.toString());
        }

        try {
            stmt.executeUpdate(sql);
            out.println("{failure:false, message:\"ok\"}");

        } catch (SQLException s) {
            throw new Exception("Couldn't create/update: " + s.getMessage(), s);
        }
    }

    private String buildInsertStatement(String db, String table,
            Map<String,String[]> map, LinkedList<String> stopList,
            boolean debug, PrintWriter out) {

        StringBuilder sql = new StringBuilder("insert into `")
                .append(db).append("`.`").append(table).append("` (");

        StringBuilder values = new StringBuilder("values (");

        for (String key : map.keySet()) {
            String[] paramValues = map.get(key);
            String param = paramValues[0];

            if (! stopList.contains(key)) {
                if (debug) { out.println(key + " = " + param); }
                sql.append("`").append(key).append("`,");
                values.append("'").append(paramValues[0]).append("',");
            }
        }
        
        sql.replace(Math.max(0,sql.length() - 1), sql.length(), ") ");
        sql.append(values).replace(Math.max(0, sql.length() - 1), sql.length(), ")");

        return sql.toString();
    }


    private String buildUpdateStatement(String db, String table,
            Map<String,String[]> map, LinkedList<String> stopList,
            String id, boolean debug, PrintWriter out) {

        StringBuilder sql = new StringBuilder("update `")
                .append(db).append("`.`").append(table).append("` set ");

        for (String key : map.keySet()) {
            String[] paramValues = map.get(key);
            String param = paramValues[0];

            if (! stopList.contains(key)) {
                if (debug) { out.println(key + " = " + param);}
                sql.append("`").append(key).append("`='")
                        .append(paramValues[0]).append("',");
            }
        }
        
        sql.replace(Math.max(0,sql.length() - 1), sql.length(), "where `id`='");
        sql.append(id).append("'");

        return sql.toString();
    }
}

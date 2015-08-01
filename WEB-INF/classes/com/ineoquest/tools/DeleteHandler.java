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


/*package-scope*/
class DeleteHandler implements RequestHandler {

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
        String sql = buildDeleteStatement(db, table, id);

        if (debug) {
            out.println("sql statement: " + sql.toString());
        }

//        try {
//            stmt.executeUpdate(sql);
            out.println("{failure:false, message:\"ok\"}");

//        } catch (SQLException s) {
//            throw new Exception("Couldn't create/update: " + s.getMessage(), s);
//        }
    }

    String buildDeleteStatement(String db, String table, String id) {
        StringBuilder sql = new StringBuilder("delete from `")
                .append(db)
                .append("`.`")
                .append(table)
                .append("` where `id`=")
                .append(id);

        return sql.toString();
    }
}




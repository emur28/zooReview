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
class TestHandler implements RequestHandler {

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt)
        throws Exception {
    
        PrintWriter out = response.getWriter();
        out.println("request uri:  " + request.getRequestURI());
        out.println("request url:  " + request.getRequestURL());
        out.println("servlet path: " + request.getServletPath());
        out.println("query param:  " + request.getParameter("query"));

        String q = request.getQueryString();
        out.println("query string: " + q);

        String[] pieces = q.split("&");
        for (int i = 0; i < pieces.length; i++) {
            out.println(i + " = " + pieces[i]);
        }

        out.println("Stop list:");
        for (String param : Utils.getQueryStringStopList(request)) {
            out.println(param);
        }
	}
}



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
class NullHandler implements RequestHandler {

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt)
        throws Exception {
    
        throw new Exception("op parameter may not be null");
	}
}



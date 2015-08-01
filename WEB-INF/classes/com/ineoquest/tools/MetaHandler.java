package com.ineoquest.tools;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;


/*package-scope*/
class MetaHandler implements RequestHandler {

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt)
        throws Exception {

        try {
            throw new Exception("meta operation not implemented");

        } catch (SQLException s) {
            throw new Exception("Couldn't delete because: " + s.getMessage(), s);
        }
    }
}



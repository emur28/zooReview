package com.ineoquest.tools;

import java.sql.Statement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*package-scope*/
interface RequestHandler {

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt) throws Exception;    

}

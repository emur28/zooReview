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
class CacheHandler implements RequestHandler {

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Statement stmt)
        throws Exception {
    
        PrintWriter out = response.getWriter();
        boolean refresh = Utils.getFlag(request, "clear");

        if (refresh) {
            ColumnCache.clear(out);
            MetaDataCache.clear(out);        
            SearchClauseCache.clear(out);        
        }
        
        ColumnCache.dump(out);
        MetaDataCache.dump(out);        
        SearchClauseCache.dump(out);        
	}
}



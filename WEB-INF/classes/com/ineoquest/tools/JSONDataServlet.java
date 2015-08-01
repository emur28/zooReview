// compile with: javac -cp /opt/apache-tomcat-8.0.24/lib/mysql-connector-java-5.1.35-bin.jar:/opt/apache-tomcat-8.0.24/lib/json_simple-1.1.jar:/opt/apache-tomcat-8.0.24/lib/servlet-api.jar:. *.java

package com.ineoquest.tools;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JSONDataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

    private static final HashMap<String, RequestHandler> HANDLERS;


    static {
        HANDLERS = new HashMap<String, RequestHandler>() {{
                put("create",         new CreateOrUpdateHandler());
                put("read",           new ReadHandler());
                put("update",         new CreateOrUpdateHandler());
                put("delete",         new DeleteHandler());
                put("meta",           new MetaHandler());
                put("choice",         new ChoiceHandler());
                put("choices",        new ChoiceHandler());
                put("test",           new TestHandler());
                put("views",          new ViewsHandler());
                put("viewsTree",      new ViewsTreeHandler());
                put("facet",          new FacetHandler());
                put("itemSelector",   new ItemSelectorHandler());
                put("cache",          new CacheHandler());
                put(null,             new NullHandler());
            }};
    }
    

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        handle(request, response);
    }
    
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        handle(request, response);
    }

    protected void doHead(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        handle(request, response);
    }

    
    protected void handle(HttpServletRequest request,
            HttpServletResponse response) {
            
        PrintWriter out = null;
        Connection conn = null;
        Statement stmt = null;
        
        try {
            out = response.getWriter();
            response.setContentType("text/plain");
            RequestHandler handler = getHandler(request);

            if (handler == null) {
                throw new Exception("Don't know how to handle op="
                        + Utils.getParameter(request, "op", "{null}"));
            }
            
            conn = Utils.getConnection(
                    Utils.getParameter(request, "db", "test"));
            stmt = conn.createStatement();

            handler.handle(request, response, stmt);
            
        } catch (Exception e) {
            if (Utils.getFlag(request, "stack") || Utils.getFlag(request, "debug")) {
                e.printStackTrace(out);
            }

            out.print("{failure:true, message:\"" + e.getMessage() +"\"}");

        } finally {
            Utils.cleanup(stmt); stmt = null;
            Utils.cleanup(conn); conn = null;
            Utils.cleanup(out); out = null;
        }
    }


    private RequestHandler getHandler(HttpServletRequest request) {
        return HANDLERS.get(request.getParameter("op"));
    }
    

}




package com.ineoquest.tools;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;


public interface Term {
    public String getQuery();
    public ArrayList<String> getColumns();
}


class OpTerm implements Term {

    protected static ArrayList<String> NULL = new ArrayList<String>(0);
    protected String _op;
    
    OpTerm(String op) {
        _op = op;
    }
    

    public String getQuery() {
        return null;
    }
    

    public ArrayList<String> getColumns() {
        return NULL;
    }
    

    public String toString() {
        return _op;
    }
}

    
class BinaryOpTerm extends OpTerm {

    BinaryOpTerm(String op) {
        super(op);
    }
}


class UnaryOpTerm extends OpTerm {

    UnaryOpTerm(String op) {
        super(op);
    }
}


class NotOpTerm extends UnaryOpTerm {
    NotOpTerm(String op) {
        super(op);
    }
}


class ColTerm implements Term {
    String _col;
    String _op;
    String _query;
    String _sqlQuery;
    ArrayList<String> _columns;
    
    ColTerm(String col, String op, String query, ArrayList<String> columns) {
        _col = col;
        _op = op;
        _query = query;
        _columns = columns;
        _sqlQuery = null;
        
    }
    

    public String getQuery() {
        return _query;
    }


    public ArrayList<String> getColumns() {
        return new ArrayList<String>() {{ add(_col); }};
    }

    public String toString() {

        if (_sqlQuery == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("(`").append(_col).append("` rlike '")
                    .append(_query).append("')");
            _sqlQuery = sb.toString();
        }

        return _sqlQuery;
    }
}

    
class AllTerm implements Term {
    String _query;
    String _sqlQuery;
    ArrayList<String> _columns;
    
    AllTerm(String query, ArrayList<String> columns) {
        _query = query;
        _columns = columns;
        _sqlQuery = null;
    }
    
    
    public String getQuery() {
        return _query;
    }


    public ArrayList<String> getColumns() {
        return _columns;
    }

    
    public String toString() {

        if (_sqlQuery == null) {
            StringBuilder sb = new StringBuilder("(");

            for (String col : _columns) {
                sb.append("`").append(col).append("` rlike '").append(_query)
                        .append("' or ");
            }

            sb.delete(Math.max(0, sb.length() - 4), sb.length());
            sb.append(")"); 
            _sqlQuery = sb.toString();
        }

        return _sqlQuery;
    }
}

    
class TermFactory {

    static Pattern operators = Pattern.compile("^(and|or|not)$");
    
    static Pattern colExpr = Pattern.compile(
            "^([a-zA-Z][a-zA-Z0-9_]*)(:)(.*)$");

    public static Term makeTerm(String query, ArrayList<String>columns) {
        Matcher matcher = colExpr.matcher(query);

        if (matcher.matches()) {
            String column = matcher.group(1);
            String op = matcher.group(2);
            String term = matcher.group(3);
            return new ColTerm(column, op, term, columns);

        } else if (operators.matcher(query).matches()) {
            if (query.equalsIgnoreCase("not")) {
                return new NotOpTerm(query);
            } else {
                return new OpTerm(query);
            }

        } else {
            return new AllTerm(query, columns);
        }
    }
}



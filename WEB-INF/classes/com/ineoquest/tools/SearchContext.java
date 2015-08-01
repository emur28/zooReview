package com.ineoquest.tools;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;


class SearchContext {
    String _whereClause = null;
    HashMap<String, String> _columnRegexps = new HashMap<String, String>();
    HashMap<String, Pattern> _columnPatterns = new HashMap<String, Pattern>();

    String getWhereClause() {
        return _whereClause;
    }
    
    
    Pattern getPattern(String column) {
        return _columnPatterns.get(column);
    }
    
    
    HashMap<String, String> getColumnRegexps() {
        return _columnRegexps;
    }
    
    
    private void addRegexpToColumn(String column, String regexp) {

        String re = _columnRegexps.get(column);
        if (re == null) {
            _columnRegexps.put(column, regexp);
        } else {
            _columnRegexps.put(column, new StringBuilder(re)
                    .append("|").append(regexp).toString());
        }
    }

            
    SearchContext(LinkedList<Term> terms, ArrayList<String> columns) {
        
        StringBuilder sql = new StringBuilder(" where ");
        boolean skipTerm = false;

        for (Term term : terms) {
            sql.append(term).append(" ");

            // remember if operator is NOT
                
            if (term instanceof NotOpTerm) {
                skipTerm = true;
            } else {
                if (! skipTerm) {
                    for (String col : term.getColumns()) {
                        addRegexpToColumn(col, term.getQuery());
                    }
                }
                skipTerm = false;
            }
        }

        sql.delete(Math.max(0, sql.length() - 1), sql.length());
        _whereClause = sql.toString();

        for (String key : _columnRegexps.keySet()) {
            _columnPatterns.put(key,
                    Pattern.compile(_columnRegexps.get(key),
                            Pattern.CASE_INSENSITIVE));
        }
    }        

    public String toString() {
        StringBuilder sb = new StringBuilder(
                "{whereClause:")
                .append(_whereClause)
                .append("}, {columnRegexps:{ ");
        for (String col : _columnRegexps.keySet()) {
            sb.append(String.format("\"%s\" --> \"%s\"", col,
                            _columnRegexps.get(col))).append(" ");
        }
        sb.append("}");
        return sb.toString();
    }
}

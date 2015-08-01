package com.ineoquest.tools;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchParser {
    
    private static Term AND_OP = new BinaryOpTerm("and");
   

    static SearchContext parse(final String queryString,
            final ArrayList<String> columns)
        throws Exception {
        String[] args = queryString.split(" ");
        return parse(args, columns);
    }

    
    static SearchContext parse(final String[] args,
            final ArrayList<String> columns) 
        throws Exception {
        
        LinkedList<Term> terms = new LinkedList<Term>();
            
        for (String arg : args) {
            Term term = TermFactory.makeTerm(arg, columns);

            // If term is a simple search term (i.e. not an operator)
            if (! (term instanceof OpTerm)) {
                // If it's not the first in the list see if previous term
                // was a search term.  If so, inject and "and" operator in
                // between.
                if ((terms.size() > 0) &&
                        (! (terms.getLast() instanceof OpTerm))) {
                    terms.add(AND_OP);
                }                                
                // and add the term
                terms.add(term);
            } else {
                // We have an operator.  We have to check for several edge
                // cases.  If this the first term, the only valid operator
                // is a unary operator.
                if (terms.size() == 0) {
                    if (!(term instanceof UnaryOpTerm)) {
                        throw new Exception("Can't start expression with "
                                + "binary operator");
                    }

                    terms.add(term);
                } else {
                    // Else, we have a previous term.  Check to see that it
                    // is either a search term or we have a binary - unary
                    // operator sequence.  Everything else is an error.
                    Term lastTerm = terms.getLast();

                    if (! (lastTerm instanceof OpTerm)) {
                        if (term instanceof UnaryOpTerm) {
                            terms.add(AND_OP);
                        }
                    } else if (! ((lastTerm instanceof BinaryOpTerm) ||
                                    (term instanceof UnaryOpTerm))) {
                        throw new Exception("Invalid operator sequence \"" 
                                + lastTerm + " " + term + "\".  A unary "
                                + "operator may only appear at the "
                                + "beginning of line or after a binary "
                                + "operator.");
                    }

                    terms.add(term);
                } // else terms.size() > 0
            }  // else have a search term
        } // for
        
        return new SearchContext(terms, columns);
    }
}


/*
 * Created on Sep 21, 2006
 */
package com.clarkparsia.sesame.sail;

import java.util.Iterator;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

public class CacheStatementIterator implements StatementIterator {
    public static boolean SUBJ = true;  
    public static boolean OBJ  = false;
    
    private Iterator iter;
    private Value node;
    private URI pred;
    private boolean position;
    
    private CacheStatementIterator( URI pred, Value node, Iterator iter, boolean position ) {
        this.node = node;
        this.pred = pred;
        this.position = position;
        this.iter = iter;
    }
    
    public static StatementIterator iterateSubjects( URI pred, Value obj, Iterator iter ) {
        return new CacheStatementIterator( pred, obj, iter, SUBJ );
    }
    
    public static StatementIterator iterateObjects( URI pred, Value subj, Iterator iter ) {
        return new CacheStatementIterator( pred, subj, iter, OBJ );
    }

    public boolean hasNext() {
        return iter.hasNext();
    }

    public Statement next() {
        Value value = (Value) iter.next();
        Statement statement = (position == SUBJ)
            ? SailUtils.createStatement( (Resource) value, pred, node )
            : SailUtils.createStatement( (Resource) node, pred, value );
        
        return statement;
    }

    public void close() {
        iter = null;
    }

}

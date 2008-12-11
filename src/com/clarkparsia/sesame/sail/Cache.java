/*
 * Created on Sep 21, 2006
 */
package com.clarkparsia.sesame.sail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sail.util.EmptyStatementIterator;
import org.openrdf.sesame.sail.util.SingleStatementIterator;

/**
 * Cache results of triple patterns.
 * 
 * TODO Configuration to control how much memory is spent for cached results 
 * 
 * @author Evren Sirin
 *
 */
public class Cache implements ICache {
    protected Map cache = new HashMap();

    public static class CacheEntry {
        Map subjects = new HashMap();
        Map objects = new HashMap();

        long timestamp;
        
        CacheEntry() {
            timestamp = System.currentTimeMillis();
        }
        
        Map getSubjects() {
            return subjects;
        }
        
        Map getObjects() {
            return objects;
        }

        void updateTimestamp() {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public Cache() {
    }

    public boolean canCache( Resource subj, URI pred, Value obj ) {
        return (subj != null && pred != null && obj == null)
            || (subj == null && pred != null && obj != null);
    }
    
    public boolean isCached( Resource subj, URI pred, Value obj ) {
        if( !canCache( subj, pred, obj ) )
            return false;

        CacheEntry entry = (CacheEntry) cache.get( pred );

        if( entry == null )
            return false;
        
        if( subj == null ) 
            return entry.getObjects().containsKey( obj );        
        else 
            return entry.getSubjects().containsKey( subj );
    }    

    public StatementIterator cache( Resource subj, URI pred, Value obj, StatementIterator si ) {
        if( !canCache( subj, pred, obj ) )
            return si;
                
        Set values = new HashSet();
        while( si.hasNext() ) {
            Statement statement = si.next();
            if( subj == null )
                values.add( statement.getSubject() );
            else
                values.add( statement.getObject() );
        }

        si.close();
        
        CacheEntry entry = (CacheEntry) cache.get( pred );

        if( entry == null ) {
            entry = new CacheEntry();
            cache.put( pred, entry );
        }


        if( subj == null ) {
            entry.getObjects().put( obj, values );
            si = CacheStatementIterator.iterateSubjects( pred, obj, values.iterator() );
        }
        else {
            entry.getSubjects().put( subj, values );
            si = CacheStatementIterator.iterateObjects( pred, subj, values.iterator() );
        }

        return si;
    }

    public StatementIterator getCached( Resource subj, URI pred, Value obj ) {
        if( pred == null )
            return null;

        CacheEntry entry = (CacheEntry) cache.get( pred );

        if (entry != null) {
            entry.updateTimestamp();
        }
        
        StatementIterator result = null;
        if( entry == null )
            result = null;
        else if( subj == null ) {
            if( obj == null )
                result = null;
            else {
                Collection subjects = (Collection) entry.getObjects().get( obj );
                if( subjects != null )
                    result = CacheStatementIterator.iterateSubjects( pred, obj, subjects.iterator() );
            }
        }
        else if( obj == null ) {
            Collection objects = (Collection) entry.getSubjects().get( subj );
            if( objects != null )
                result = CacheStatementIterator.iterateObjects( pred, subj, objects.iterator() );
        }
        else {
            Collection subjects = (Collection) entry.getObjects().get( obj );
            if( subjects == null ) {
                Collection objects = (Collection) entry.getSubjects().get( subj );
                if( objects != null ) {
                    if( objects.contains( obj ) )
                        result = new SingleStatementIterator( pred, subj, obj );
                    else
                        result = new EmptyStatementIterator();
                }
            }
            else if( subjects.contains( subj ) )
                result = new SingleStatementIterator( pred, subj, obj );
            else
                result = new EmptyStatementIterator();
        }
            
        return result;
    }
    
    public void clear() {
        cache.clear();
    }

    public Boolean contains( Resource subj, URI pred, Value obj ) {        
        if( pred == null )
            return null;
        
        CacheEntry entry = (CacheEntry) cache.get( pred );

        Boolean result = null;
        if( entry == null )
            result = null;
        else if( subj == null ) { 
            if( obj == null )
                // TODO we can do better here
                result = null;
            else {
                Collection subjects = (Collection) entry.getObjects().get( obj );
                result = (subjects == null) ? null : subjects.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        else if( obj == null ) {
            Collection objects = (Collection) entry.getSubjects().get( subj );
            result = (objects == null) ? null : objects.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
        }
        else {
            Collection subjects = (Collection) entry.getObjects().get( obj );
            if( subjects == null ) {
                Collection objects = (Collection) entry.getSubjects().get( subj );
                result = (objects == null) ? null : objects.contains( obj ) ? Boolean.TRUE : Boolean.FALSE;
            }
            else
                result = (subjects == null) ? null : subjects.contains( subj ) ? Boolean.TRUE : Boolean.FALSE;
        }
        
        return result;
    }
}

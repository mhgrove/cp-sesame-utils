package com.clarkparsia.sesame.utils;

import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.model.Statement;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Nov 1, 2006 12:49:07 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class StmtIterator implements StatementIterator
{
    private Iterator mIter;

    public StmtIterator() {
        mIter = Collections.EMPTY_LIST.iterator();
    }

    public StmtIterator(Iterator theIter) {
        mIter = theIter;
    }

    public StmtIterator(List theList) {
        this(theList.iterator());
    }

    public StmtIterator(Statement[] theStmts) {
        this(Arrays.asList(theStmts));
    }

    public boolean hasNext() {
        return mIter.hasNext();
    }

    public void close() {
    }

    public Statement next() {
        return (Statement)mIter.next();
    }
}
package com.clarkparsia.sesame.utils;

import org.openrdf.model.Statement;
import org.openrdf.sesame.sail.StatementIterator;

import java.util.Iterator;

/**
 * Title: <br/>
* Description: <br/>
* Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
* Created: May 1, 2009 1:07:27 PM <br/>
*
* @author Michael Grove <mike@clarkparsia.com>
*/
public class StatementIteratorAsIterator implements Iterator<Statement> {
	private StatementIterator mIter;

	public StatementIteratorAsIterator(StatementIterator theIter) {
		mIter = theIter;
	}

	public boolean hasNext() {
		return mIter.hasNext();
	}

	public Statement next() {
		return mIter.next();
	}

	public void remove() {
		throw new IllegalArgumentException();
	}
}

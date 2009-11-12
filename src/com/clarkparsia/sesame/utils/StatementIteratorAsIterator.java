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

	private boolean mAutoClose = false;

	/**
	 * Create a new StatementIteratorAsIterator
	 * @param theIter the underlying iterator 
	 */
	public StatementIteratorAsIterator(StatementIterator theIter) {
		mIter = theIter;
	}

	/**
	 * Create a new StatementIteratorAsIterator
	 * @param theIter the underlying iterator
	 * @param theAutoClose Whether or not to automatically close the underlying StatementIterator when done iterating
	 */
	public StatementIteratorAsIterator(StatementIterator theIter, boolean theAutoClose) {
		this(theIter);

		mAutoClose = theAutoClose;
	}

	public boolean hasNext() {
		return mIter.hasNext();
	}

	public Statement next() {
		Statement aNext = mIter.next();

		if (!hasNext() && mAutoClose) {
			mIter.close();
		}

		return aNext;
	}

	public void close() {
		mIter.close();
	}

	public void remove() {
		throw new IllegalArgumentException();
	}
}

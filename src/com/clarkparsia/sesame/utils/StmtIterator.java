/*
 * Copyright (c) 2005-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.sesame.utils;

import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.model.Statement;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;

/**
 * <p>Implementation of the Sesame StatementIterator interface which is also Iterable, and a normal Java Iterator
 * over a set of statements.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class StmtIterator implements StatementIterator, Iterator<Statement>, Iterable<Statement> {

	/**
	 * The internal iterator over the set of statements
	 */
    private Iterator<Statement> mIter;

    public StmtIterator() {
		List<Statement> aList = Collections.emptyList();
        mIter = aList.iterator();
    }

	/**
	 * Wrap the Sesame StatementIterator as a StmtIterator
	 * @param theIter the Sesame StatementIterator to wrap
	 */
	public StmtIterator(StatementIterator theIter) {
		this(new StatementIteratorAsIterator(theIter));
	}

	/**
	 * Wrap the Iterable of Statements as a StmtIterator
	 * @param theIter the Iterable to wrap
	 */
	public StmtIterator(Iterable<Statement> theIter) {
		this(theIter.iterator());
	}

	/**
	 * Wrap the Iterator over some Statements as a StmtIterator
	 * @param theIter the iterator to wrap
	 */
    public StmtIterator(Iterator<Statement> theIter) {
        mIter = theIter;
    }

	/**
	 * Create a new StmtIterator over the List of statements
	 * @param theList the list of statements to iterate over
	 */
    public StmtIterator(List<Statement> theList) {
        this(theList.iterator());
    }

	/**
	 * Create a new StmtIterator over the array of statements
	 * @param theStmts the statements to iterate over
	 */
    public StmtIterator(Statement[] theStmts) {
        this(Arrays.asList(theStmts));
    }

	/**
	 * @inheritDoc
	 */
    public boolean hasNext() {
        return mIter.hasNext();
    }

	/**
	 * @inheritDoc
	 */
    public void close() {
    }

	/**
	 * @inheritDoc
	 */
    public Statement next() {
        return mIter.next();
    }

	/**
	 * @inheritDoc
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	public Iterator<Statement> iterator() {
		return this;
	}

	/**
	 * Return the contents of the iterator as a Sesame Graph
	 * @return the statements of this iterator added to a Graph
	 */
	public Graph asGraph() {
		Graph aGraph = new GraphImpl();

		while (hasNext()) {
			aGraph.add(next());
		}

		return aGraph;
	}

	/**
	 * <p>Adapter class for making a Sesame StatementIterator a JavaIterator</p>
	 *
	 * @author Michael Grove
	 * @since 1.0
	 */
	private static class StatementIteratorAsIterator implements Iterator<Statement> {

		/**
		 * The underlying sesame StatementIterator
		 */
		private StatementIterator mIter;

		/**
		 * Whether or not to automatically close the underlying Sesame StatementIterator when iteration is complete
		 */
		private boolean mAutoClose = true;

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

		/**
		 * @inheritDoc
		 */
		public boolean hasNext() {
			return mIter.hasNext();
		}

		/**
		 * @inheritDoc
		 */
		public Statement next() {
			Statement aNext = mIter.next();

			if (!hasNext() && mAutoClose) {
				mIter.close();
			}

			return aNext;
		}

		/**
		 * Close this iterator and any resources associated with it.
		 */
		public void close() {
			mIter.close();
		}

		/**
		 * @inheritDoc
		 */
		public void remove() {
			throw new IllegalArgumentException();
		}
	}
}
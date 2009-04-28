package com.clarkparsia.sesame.utils;

import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.model.Statement;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import com.clarkparsia.utils.CollectionUtil;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Nov 1, 2006 12:49:07 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class StmtIterator implements StatementIterator, Iterator<Statement>, Iterable<Statement>
{
    private Iterator<Statement> mIter;

    public StmtIterator() {
		List<Statement> aList = Collections.emptyList();
        mIter = aList.iterator();
    }

	public StmtIterator(StatementIterator theIter) {
		this();

		// TODO: this is not efficient if sesame was good about keeping a cursor and using a fetching result set
		// because this requires the entire statement iterator to be kept in memory, and if it was pulling from
		// something that was fetching things from the repo as they were requested, you'd lose that benefit.

		List<Statement> aList = new ArrayList<Statement>();
		while (theIter.hasNext()) {
			aList.add(theIter.next());
		}

		mIter = aList.iterator();
	}

    public StmtIterator(Iterator<Statement> theIter) {
        mIter = theIter;
    }

    public StmtIterator(List<Statement> theList) {
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
        return mIter.next();
    }

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Iterator<Statement> iterator() {
		return this;
	}

	public Graph asGraph() {
		Graph aGraph = new GraphImpl();

		while (hasNext()) {
			aGraph.add(next());
		}

		return aGraph;
	}
}
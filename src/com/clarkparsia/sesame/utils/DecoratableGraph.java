package com.clarkparsia.sesame.utils;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.sesame.sail.StatementIterator;

import java.util.Collection;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jun 2, 2009 2:43:55 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */

public class DecoratableGraph implements Graph {
	private Graph mGraph;

	private SesameValueFactory mValueFactory = new SesameValueFactory();

	public DecoratableGraph(Graph theGraph) {
		mGraph = theGraph;
	}

	public void add(final Resource theResource, final URI theURI, final Value theValue) {
		mGraph.add(theResource, theURI, theValue);
	}

	public void add(final Statement theStatement) {
		mGraph.add(theStatement);
	}

	public void add(final StatementIterator theStatementIterator) {
		mGraph.add(theStatementIterator);
	}

	public void add(final StatementIterator theStatementIterator, final boolean theBool) {
		mGraph.add(theStatementIterator, theBool);
	}

	public void add(final Collection theCollection) {
		mGraph.add(theCollection);
	}

	public void add(final Collection theCollection, final boolean theBool) {
		mGraph.add(theCollection, theBool);
	}

	public void add(final Graph theGraph) {
		mGraph.add(theGraph);
	}

	public void add(final Graph theGraph, final boolean theBool) {
		mGraph.add(theGraph, theBool);
	}

	public boolean contains(final Resource theResource, final URI theURI, final Value theValue) {
		return mGraph.contains(theResource, theURI, theValue);
	}

	public boolean contains(final Statement theStatement) {
		return mGraph.contains(theStatement);
	}

	public StatementIterator getStatements() {
		return mGraph.getStatements();
	}

	public StatementIterator getStatements(final Resource theResource, final URI theURI, final Value theValue) {
		return mGraph.getStatements(theResource, theURI, theValue);
	}

	public Collection getStatementCollection(final Resource theResource, final URI theURI, final Value theValue) {
		return mGraph.getStatementCollection(theResource, theURI, theValue);
	}

	public int remove(final Resource theResource, final URI theURI, final Value theValue) {
		return mGraph.remove(theResource, theURI, theValue);
	}

	public int remove(final Statement theStatement) {
		return mGraph.remove(theStatement);
	}

	public int remove(final StatementIterator theStatementIterator) {
		return mGraph.remove(theStatementIterator);
	}

	public int remove(final Graph theGraph) {
		return mGraph.remove(theGraph);
	}

	public void clear() {
		mGraph.clear();
	}

	public ValueFactory getValueFactory() {
		return mValueFactory;
	}

	public SesameValueFactory getSesameValueFactory() {
		return mValueFactory;
	}
}


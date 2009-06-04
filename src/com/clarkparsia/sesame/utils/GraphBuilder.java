package com.clarkparsia.sesame.utils;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.URIImpl;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Mar 25, 2009 3:07:15 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class GraphBuilder {
    private ExtendedGraph mGraph;

    public GraphBuilder() {
        mGraph = new ExtendedGraph(new GraphImpl());
    }

    public ExtendedGraph graph() {
        return mGraph;
    }

    public void reset() {
        mGraph.clear();
    }

    public ResourceBuilder uri(URI theURI) {
        return new ResourceBuilder(mGraph, mGraph.getValueFactory().createURI(theURI.getURI()));
    }

    public ResourceBuilder uri(String theURI) {
        return instance(null, theURI);
    }

    public ResourceBuilder instance(URI theType) {
        return instance(theType, (String) null);
    }

	/**
	 * Create an un-typed BNode in the graph
	 * @return a ResourceBuilder wrapping the bnode
	 */
	public ResourceBuilder instance() {
		return instance(null, (String) null);
	}

	public ResourceBuilder instance(URI theType, java.net.URI theURI) {
		return instance(theType, theURI.toString());
	}

    public ResourceBuilder instance(URI theType, String theURI) {
        Resource aRes = theURI == null
                        ? mGraph.getValueFactory().createBNode()
                        : mGraph.getValueFactory().createURI(theURI);

        if (theType != null) {
            mGraph.add(aRes, URIImpl.RDF_TYPE, theType);
        }

        return new ResourceBuilder(mGraph, aRes);
    }

	public SesameValueFactory getSesameValueFactory() {
		return graph().getSesameValueFactory();
	}
}

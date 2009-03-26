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
    private Graph mGraph;

    public GraphBuilder() {
        mGraph = new GraphImpl();
    }

    public Graph graph() {
        return mGraph;
    }

    public void reset() {
        mGraph.clear();
    }

    public ResourceBuilder instance(URI theType) {
        return instance(theType, null);
    }

    public ResourceBuilder instance(URI theType, String theURI) {
        Resource aRes = theURI == null
                        ? mGraph.getValueFactory().createBNode()
                        : mGraph.getValueFactory().createURI(theURI);

        mGraph.add(aRes, URIImpl.RDF_TYPE, theType);

        return new ResourceBuilder(mGraph, aRes);
    }
}

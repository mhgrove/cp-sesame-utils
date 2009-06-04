package com.clarkparsia.sesame.utils;

import org.openrdf.model.Resource;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.BNode;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.vocabulary.XmlSchema;

import java.util.List;
import java.util.Iterator;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Mar 25, 2009 3:11:22 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ResourceBuilder {
    private ExtendedGraph mGraph;
    private Resource mRes;

    public ResourceBuilder(Resource theRes) {
        this(new ExtendedGraph(), theRes);
    }

    ResourceBuilder(ExtendedGraph theGraph, Resource theRes) {
        mRes = theRes;
        mGraph = theGraph;
    }

	public ResourceBuilder addProperty(URI theProperty, java.net.URI theURI) {
		return addProperty(theProperty, mGraph.getSesameValueFactory().createURI(theURI));
	}

	public ResourceBuilder addProperty(URI theProperty, List<? extends Resource> theList) {
		Resource aListRes = mGraph.getSesameValueFactory().createBNode();

		mGraph.add(getResource(), theProperty, aListRes);

		Iterator<? extends Resource> aResIter = theList.iterator();
		while (aResIter.hasNext()) {
			mGraph.add(aListRes, URIImpl.RDF_FIRST, aResIter.next());
			if (aResIter.hasNext()) {
				BNode aNextListElem = mGraph.getSesameValueFactory().createBNode();
				mGraph.add(aListRes, URIImpl.RDF_REST, aNextListElem);
				aListRes = aNextListElem;
			}
			else {
				mGraph.add(aListRes, URIImpl.RDF_REST, URIImpl.RDF_NIL);
			}
		}

		return this;
	}

    public ResourceBuilder addProperty(URI theProperty, Value theValue) {
        if (theValue != null) {
            mGraph.add(mRes, theProperty, theValue);
        }

        return this;
    }

    public Resource getResource() {
        return mRes;
    }

    public Graph graph() {
        return mGraph;
    }

    public ResourceBuilder addProperty(URI theProperty, ResourceBuilder theBuilder) {
        if (theBuilder != null) {
            addProperty(theProperty, theBuilder.getResource());
            mGraph.add(theBuilder.mGraph);
        }

        return this;
    }

    public ResourceBuilder addProperty(URI theProperty, String theValue) {
		if (theValue != null) {
			return addProperty(theProperty, mGraph.getSesameValueFactory().createLiteral(theValue));
		}
		else {
			return this;
		}
    }

    public ResourceBuilder addProperty(URI theProperty, int theValue) {
        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
    }


    public ResourceBuilder addProperty(URI theProperty, long theValue) {
        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
    }


    public ResourceBuilder addProperty(URI theProperty, short theValue) {
        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
    }


    public ResourceBuilder addProperty(URI theProperty, double theValue) {
        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
    }


    public ResourceBuilder addProperty(URI theProperty, float theValue) {
        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
    }

    public ResourceBuilder addProperty(URI theProperty, boolean theValue) {
        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
    }

    public ResourceBuilder addLabel(String theLabel) {
        return addProperty(URIImpl.RDFS_LABEL, theLabel);
    }

    public ResourceBuilder addType(URI theType) {
        return addProperty(URIImpl.RDF_TYPE, theType);
    }
}

package com.clarkparsia.sesame.utils;

import org.openrdf.model.Resource;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.vocabulary.XmlSchema;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Mar 25, 2009 3:11:22 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ResourceBuilder {
    private Graph mGraph;
    private Resource mRes;

    ResourceBuilder(Graph theGraph, Resource theRes) {
        mRes = theRes;
        mGraph = theGraph;
    }

    public ResourceBuilder addProperty(URI theProperty, Value theValue) {
        mGraph.add(mRes, theProperty, theValue);

        return this;
    }

    public ResourceBuilder addProperty(URI theProperty, String theValue) {
        return addProperty(theProperty, mGraph.getValueFactory().createLiteral(theValue,
                                                                               mGraph.getValueFactory().createURI(XmlSchema.STRING)));
    }

    public ResourceBuilder addProperty(URI theProperty, int theValue) {
        return addProperty(theProperty, mGraph.getValueFactory().createLiteral(String.valueOf(theValue),
                                                                               mGraph.getValueFactory().createURI(XmlSchema.INT)));
    }


    public ResourceBuilder addProperty(URI theProperty, long theValue) {
        return addProperty(theProperty, mGraph.getValueFactory().createLiteral(String.valueOf(theValue),
                                                                               mGraph.getValueFactory().createURI(XmlSchema.LONG)));
    }


    public ResourceBuilder addProperty(URI theProperty, short theValue) {
        return addProperty(theProperty, mGraph.getValueFactory().createLiteral(String.valueOf(theValue),
                                                                               mGraph.getValueFactory().createURI(XmlSchema.SHORT)));
    }


    public ResourceBuilder addProperty(URI theProperty, double theValue) {
        return addProperty(theProperty, mGraph.getValueFactory().createLiteral(String.valueOf(theValue),
                                                                               mGraph.getValueFactory().createURI(XmlSchema.DOUBLE)));
    }


    public ResourceBuilder addProperty(URI theProperty, float theValue) {
        return addProperty(theProperty, mGraph.getValueFactory().createLiteral(String.valueOf(theValue),
                                                                               mGraph.getValueFactory().createURI(XmlSchema.FLOAT)));
    }

    public ResourceBuilder addProperty(URI theProperty, boolean theValue) {
        return addProperty(theProperty, mGraph.getValueFactory().createLiteral(String.valueOf(theValue),
                                                                               mGraph.getValueFactory().createURI(XmlSchema.BOOLEAN)));
    }
}

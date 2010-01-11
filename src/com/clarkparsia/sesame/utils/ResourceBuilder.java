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

import org.openrdf.model.Resource;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.BNode;
import org.openrdf.model.impl.URIImpl;

import java.util.List;
import java.util.Iterator;
import java.util.Date;

/**
 * <p>Utility class for creating statements about a particular resource.</p>
 *
 * @author Michael Grove
 * @since 1.0
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

	public ResourceBuilder addProperty(URI theProperty, List<? extends Value> theList) {
		Resource aListRes = mGraph.getSesameValueFactory().createBNode();

		mGraph.add(getResource(), theProperty, aListRes);

		Iterator<? extends Value> aResIter = theList.iterator();
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
			return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
    }

    public ResourceBuilder addProperty(URI theProperty, Integer theValue) {
		if (theValue != null) {
        	return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
    }


    public ResourceBuilder addProperty(URI theProperty, Long theValue) {
		if (theValue != null) {
	        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
    }


    public ResourceBuilder addProperty(URI theProperty, Short theValue) {
		if (theValue != null) {
	        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
    }


    public ResourceBuilder addProperty(URI theProperty, Double theValue) {
		if (theValue != null) {
        	return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
    }

	public ResourceBuilder addProperty(URI theProperty, Date theValue) {
		if (theValue != null) {
			return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
	}

    public ResourceBuilder addProperty(URI theProperty, Float theValue) {
		if (theValue != null) {
	        return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
    }

    public ResourceBuilder addProperty(URI theProperty, Boolean theValue) {
		if (theValue != null) {
        	return addProperty(theProperty, mGraph.getSesameValueFactory().createTypedLiteral(theValue));
		}
		else {
			return this;
		}
    }

	public ResourceBuilder addProperty(URI theProperty, Object theObject) {
		if (theObject == null) {
			return this;
		}
		else if (theObject instanceof Boolean) {
			return addProperty(theProperty, (Boolean) theObject);
		}
		else if (theObject instanceof Long) {
			return addProperty(theProperty, (Long) theObject);
		}
		else if (theObject instanceof Integer) {
			return addProperty(theProperty, (Integer) theObject);
		}
		else if (theObject instanceof Short) {
			return addProperty(theProperty, (Short) theObject);
		}
		else if (theObject instanceof Float) {
			return addProperty(theProperty, (Float) theObject);
		}
		else if (theObject instanceof Date) {
			return addProperty(theProperty, (Date) theObject);
		}
		else if (theObject instanceof Double) {
			return addProperty(theProperty, (Double) theObject);
		}
		else if (theObject instanceof Value) {
			return addProperty(theProperty, (Value) theObject);
		}
		else if (theObject instanceof List) {
			try {
				return addProperty(theProperty, (List<Value>) theObject);
			}
			catch (ClassCastException e) {
				e.printStackTrace();
				return this;
			}
		}
		else if (theObject instanceof ResourceBuilder) {
			return addProperty(theProperty, (ResourceBuilder) theObject);
		}
		else if (theObject instanceof java.net.URI) {
			return addProperty(theProperty, (java.net.URI) theObject);
		}
		else {
			return addProperty(theProperty, theObject.toString());
		}
	}

    public ResourceBuilder addLabel(String theLabel) {
        return addProperty(URIImpl.RDFS_LABEL, theLabel);
    }

    public ResourceBuilder addType(URI theType) {
        return addProperty(URIImpl.RDF_TYPE, theType);
    }
}

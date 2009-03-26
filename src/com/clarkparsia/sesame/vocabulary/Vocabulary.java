package com.clarkparsia.sesame.vocabulary;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Mar 24, 2009 3:27:08 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public abstract class Vocabulary {
    private static final ValueFactory FACTORY = new ValueFactoryImpl();

    private String mURI;

    public Vocabulary(String theURI) {
        mURI = theURI;
    }

    public URI term(String theName) {
        return FACTORY.createURI(mURI + theName);
    }

    public java.net.URI uri() {
        return java.net.URI.create(mURI);
    }
}

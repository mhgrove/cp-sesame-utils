package com.clarkparsia.sesame.vocabulary;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 22, 2008 6:08:43 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class FOAF {
    private static final ValueFactory FACTORY = new ValueFactoryImpl();

    private static final java.net.URI FOAF_URI = java.net.URI.create("http://xmlns.com/foaf/0.1/");

    public static java.net.URI getURI() {
        return FOAF_URI;
    }

    public static final URI Person = FACTORY.createURI(getURI().toString() + "Person");
    public static final URI Organization = FACTORY.createURI(getURI().toString() + "Organization");
    public static final URI Image = FACTORY.createURI(getURI().toString() + "Image");

    public static final URI firstName = FACTORY.createURI(getURI().toString() + "firstName");
    public static final URI surname = FACTORY.createURI(getURI().toString() + "surname");
    public static final URI mbox = FACTORY.createURI(getURI().toString() + "mbox");
    public static final URI depicts = FACTORY.createURI(getURI().toString() + "depicts");
    public static final URI depiction = FACTORY.createURI(getURI().toString() + "depiction");
    public static final URI maker = FACTORY.createURI(getURI().toString() + "maker");
    public static final URI phone = FACTORY.createURI(getURI().toString() + "phone");
    public static final URI fax = FACTORY.createURI(getURI().toString() + "fax");

    @Deprecated
    public static final URI regionDepicts = FACTORY.createURI(getURI().toString() + "regionDepicts");

    @Deprecated
    public static final URI regionDepiction = FACTORY.createURI(getURI().toString() + "regionDepiction");
}

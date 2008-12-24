package com.clarkparsia.sesame.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 26, 2008 3:27:11 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class DC {
    public static final String ONT_URI = "http://purl.org/dc/elements/1.1/";

    private static final ValueFactory FACTORY = new ValueFactoryImpl();

    public static final URI title = FACTORY.createURI(ONT_URI + "title");
    public static final URI date = FACTORY.createURI(ONT_URI + "date");
    public static final URI creator = FACTORY.createURI(ONT_URI + "creator");
    public static final URI subject = FACTORY.createURI(ONT_URI + "subject");
    public static final URI rights = FACTORY.createURI(ONT_URI + "rights");
}

package com.clarkparsia.sesame.utils.query;

import org.openrdf.model.Value;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;

import java.util.HashMap;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2008 8:13:33 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class Binding extends HashMap<String, Value> {
    public Literal getLiteral(String theString) {
        return (Literal) get(theString);
    }

    public URI getURI(String theString) {
        return (URI) get(theString);
    }

    public BNode getBNode(String theString) {
        return (BNode) get(theString);
    }

    public Resource getResource(String theString) {
        return (Resource) get(theString);
    }
}

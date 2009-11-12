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
        return safeGet(theString, Literal.class);
    }

    public URI getURI(String theString) {
        return safeGet(theString, URI.class);
    }

    public BNode getBNode(String theString) {
        return safeGet(theString, BNode.class);
    }

    public Resource getResource(String theString) {
        return safeGet(theString, Resource.class);
    }

	private <T> T safeGet(String theKey, Class<T> theClass) {
		try {
			if (containsKey(theKey)) {
				return theClass.cast(get(theKey));
			}
			else {
				return null;
			}
		}
		catch (ClassCastException e) {
			return null;
		}
	}
}

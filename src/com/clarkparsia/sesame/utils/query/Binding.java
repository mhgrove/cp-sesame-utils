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

package com.clarkparsia.sesame.utils.query;

import org.openrdf.model.Value;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Simple key-value query solution binding class with some convenience methods to get the keys as specific Sesame API objects.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class Binding extends HashMap<String, Value> {

	public Binding() {
	}
	
	public Binding(Map<String, Value> theMap) {
		super();

		putAll(theMap);
	}

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

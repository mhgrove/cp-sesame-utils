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

package com.clarkparsia.sesame.vocabulary;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.clarkparsia.sesame.utils.SesameValueFactory;

/**
 * <p>Base class for creating an term factory for an ontology</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public abstract class Vocabulary {
    protected static final SesameValueFactory FACTORY = new SesameValueFactory();

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

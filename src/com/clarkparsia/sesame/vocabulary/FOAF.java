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

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 * <p>Term constants for the FOAF ontology</p>
 *
 * @author Michael Grove
 * @since 1.0
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
    public static final URI based_near = FACTORY.createURI(getURI().toString() + "based_near");
	public static final URI thumbnail = FACTORY.createURI(getURI().toString() + "thumbnail");
	public static final URI homepage = FACTORY.createURI(getURI().toString() + "homepage");
	public static final URI birthday = FACTORY.createURI(getURI().toString() + "birthday");
	public static final URI knows = FACTORY.createURI(getURI().toString() + "knows");

    @Deprecated
    public static final URI regionDepicts = FACTORY.createURI(getURI().toString() + "regionDepicts");

    @Deprecated
    public static final URI regionDepiction = FACTORY.createURI(getURI().toString() + "regionDepiction");
}

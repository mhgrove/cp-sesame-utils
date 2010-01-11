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

import org.openrdf.model.URI;

/**
 * <p>Term constants for the VCard ontology</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class VCard extends Vocabulary {
    private static final VCard VOCAB = new VCard("http://www.w3.org/2006/vcard/ns#");

    private VCard(String theURI) {
        super(theURI);
    }

    public static VCard ontology() {
        return VOCAB;
    }

    public final URI Address = term("Address");

	public final URI street_address = term("street-address");
	public final URI locality = term("locality");
	public final URI region = term("region");
	public final URI postal_code = term("postal-code");

	@Deprecated
    public final URI Street = term("Street");

	@Deprecated
    public final URI Locality = term("Locality");

	@Deprecated
    public final URI Region = term("Region");
}

package com.clarkparsia.sesame.vocabulary;

import org.openrdf.model.URI;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Mar 25, 2009 8:19:49 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class VCard extends Vocabulary {
    private static final VCard VOCAB = new VCard("http://www.w3.org/2001/vcard-rdf/3.0#");

    private VCard(String theURI) {
        super(theURI);
    }

    public static VCard ontology() {
        return VOCAB;
    }

    public final URI ADR = term("ADR");
    public final URI Street = term("Street");
    public final URI Locality = term("Locality");
    public final URI Region = term("Region");
}

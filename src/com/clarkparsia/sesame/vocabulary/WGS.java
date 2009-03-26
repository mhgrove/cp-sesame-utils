package com.clarkparsia.sesame.vocabulary;

import org.openrdf.model.URI;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Mar 25, 2009 8:26:16 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class WGS extends Vocabulary {
    private static WGS VOCAB = new WGS("http://www.w3.org/2003/01/geo/wgs84_pos#");

    private WGS(String theURI) {
        super(theURI);
    }

    public static WGS ontology() {
        return VOCAB;
    }

    public final URI lat = term("lat");
    public final URI _long = term("long");
    public final URI alt = term("alt");
    public final URI lat_long = term("lat_long");
    public final URI Point = term("Point");
    public final URI SpatialThing = term("SpatialThing");
}

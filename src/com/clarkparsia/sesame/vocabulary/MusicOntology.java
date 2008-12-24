package com.clarkparsia.sesame.vocabulary;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 18, 2008 8:46:13 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class MusicOntology {
    public static final String ONT_URI = "http://purl.org/ontology/mo/";

    private static final ValueFactory FACTORY = new ValueFactoryImpl();

    // properties
    public static final URI track = FACTORY.createURI(ONT_URI + "track");
    public static final URI release_type = FACTORY.createURI(ONT_URI + "release_type");
    public static final URI release_status = FACTORY.createURI(ONT_URI + "release_status");
    public static final URI track_number = FACTORY.createURI(ONT_URI + "track_number");
    public static final URI length = FACTORY.createURI(ONT_URI + "length");
    public static final URI made = FACTORY.createURI(ONT_URI + "made");
    public static final URI musicbrainz = FACTORY.createURI(ONT_URI + "musicbrainz");
    public static final URI olga = FACTORY.createURI(ONT_URI + "olga");
    public static final URI genre = FACTORY.createURI(ONT_URI + "genre");
    public static final URI sample_rate = FACTORY.createURI(ONT_URI + "sample_rate");
    public static final URI bitsPerSample = FACTORY.createURI(ONT_URI + "bitsPerSample");

    // cp properties
    public static final URI rating = FACTORY.createURI(ONT_URI + "rating");
    public static final URI albumRating = FACTORY.createURI(ONT_URI + "albumRating");
    public static final URI year = FACTORY.createURI(ONT_URI + "year");
    public static final URI location = FACTORY.createURI(ONT_URI + "location");
    
    // classes
    public static final URI Genre = FACTORY.createURI(ONT_URI + "Genre");
    public static final URI Record = FACTORY.createURI(ONT_URI + "Record");
    public static final URI Track = FACTORY.createURI(ONT_URI + "Track");
    public static final URI MusicArtist = FACTORY.createURI(ONT_URI + "MusicArtist");
    public static final URI MusicGroup = FACTORY.createURI(ONT_URI + "MusicGroup");

    // individuals
    public static final URI Metal = FACTORY.createURI(Genre.getURI() + "/Metal");
    public static final URI Rock = FACTORY.createURI(Genre.getURI() + "/Rock");
    public static final URI Alternative = FACTORY.createURI(Genre.getURI() + "/Alternative");
    public static final URI Pop = FACTORY.createURI(Genre.getURI() + "/Pop");
    public static final URI Punk = FACTORY.createURI(Genre.getURI() + "/Punk");
    public static final URI Funk = FACTORY.createURI(Genre.getURI() + "/Funk");
    public static final URI Soundtrack = FACTORY.createURI(Genre.getURI() + "/Soundtrack");
    public static final URI Blues = FACTORY.createURI(Genre.getURI() + "/Blues");
    public static final URI Jazz = FACTORY.createURI(Genre.getURI() + "/Jazz");
    public static final URI Vocal = FACTORY.createURI(Genre.getURI() + "/Vocal");

    public static final URI album = FACTORY.createURI(ONT_URI + "album");
    public static final URI official = FACTORY.createURI(ONT_URI + "official");
}

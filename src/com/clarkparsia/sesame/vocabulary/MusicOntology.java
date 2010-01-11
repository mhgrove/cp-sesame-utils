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
import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;

/**
 * <p>Term constants for the DC music ontology</p>
 *
 * @author Michael Grove
 * @since 1.0
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
	public static final URI Country = FACTORY.createURI(Genre.getURI() + "/Country");

    public static final URI album = FACTORY.createURI(ONT_URI + "album");
    public static final URI official = FACTORY.createURI(ONT_URI + "official");
}

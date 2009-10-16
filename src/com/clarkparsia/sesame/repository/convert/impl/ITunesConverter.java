package com.clarkparsia.sesame.repository.convert.impl;

import org.openrdf.sesame.sail.RdfRepository;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.export.RdfExport;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.vocabulary.XmlSchema;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import com.clarkparsia.sesame.utils.BuildRepositoryStatementHandler;
import com.clarkparsia.sesame.vocabulary.DC;
import com.clarkparsia.sesame.vocabulary.FOAF;
import com.clarkparsia.sesame.vocabulary.MusicOntology;
import com.clarkparsia.sesame.repository.convert.Converter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.OutputStream;
import java.net.URL;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 23, 2008 9:21:06 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ITunesConverter implements Converter {
    private enum Key {
        Artist("Artist"),
        Rating("Rating"),
        Location("Location"),
        Album("Album"),
        Genre("Genre"),
        Year("Year"),
        AlbumRating("Album Rating"),
        Composer("Composer"),
        BitRate("Bit Rate"),
        SampleRate("Sample Rate"),
        TrackNumber("Track Number"),
        TotalTime("Total Time"),
        Name("Name");

        private String mKeyName;

        Key(String theKeyName) {
            mKeyName = theKeyName;
        }

        String key() {
            return mKeyName;
        }
    }

    public RdfRepository convert(URL theFile) throws Exception {
        RdfRepository aSource = new org.openrdf.sesame.sailimpl.memory.RdfRepository();
        aSource.startTransaction();

        convert(theFile, new BuildRepositoryStatementHandler(aSource));

        aSource.commitTransaction();

        return aSource;
    }

    public void convert(URL theFile, OutputStream theStream) throws Exception {
        convert(theFile, new TurtleWriter(theStream));
    }

    public void convert(URL theFile, RdfDocumentWriter theWriter) throws Exception {
        new RdfExport().exportRdf(convert(theFile), theWriter, true);
    }

    public void convert(URL theFile, StatementHandler theHandler) throws Exception {
        DocumentBuilderFactory aFactory = DocumentBuilderFactory.newInstance();

        aFactory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder aBuilder = aFactory.newDocumentBuilder();

        Document aITunesLibrary = aBuilder.parse(theFile.openStream());

        NodeList aNodeList = findStartNode(aITunesLibrary).getChildNodes();

        for (int i = 0; i < aNodeList.getLength(); i++) {
            Node aTrack = aNodeList.item(i);

            if (aTrack.getNodeName().equals("dict")) {

                Graph aGraph = new GraphImpl();

                // TODO: create the URI from the music brainz id?
                // TODO: can i get location information from dbtune.org?
                URI aSubj = makeURI(i+"/track", get(aTrack, Key.Name));
                
                aGraph.add(aSubj, URIImpl.RDF_TYPE, MusicOntology.Track);

                String aValue = get(aTrack, Key.TrackNumber);
                if (aValue != null) {
                    aGraph.add(aSubj,
                               MusicOntology.track_number,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.INT)));
                }

                aValue = get(aTrack, Key.TotalTime);
                if (aValue != null) {
                    aGraph.add(aSubj, MusicOntology.length,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.INT)));
                }

                aValue = get(aTrack, Key.SampleRate);
                if (aValue != null) {
                    aGraph.add(aSubj, MusicOntology.sample_rate,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.INT)));

                }

                aValue = get(aTrack, Key.BitRate);
                if (aValue != null) {
                    aGraph.add(aSubj, MusicOntology.bitsPerSample,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.INT)));
                }

                aValue = get(aTrack, Key.Genre);
                if (aValue != null) {
                    for (URI aGenre : makeGenre(aValue)) {
                        // TODO: need a label for each genre
                        aGraph.add(aSubj, MusicOntology.genre, aGenre);
                        aGraph.add(aGenre, URIImpl.RDF_TYPE, MusicOntology.Genre);
                    }
                }

                aValue = get(aTrack, Key.Rating);
                if (aValue != null) {
                    aGraph.add(aSubj, MusicOntology.rating,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.INT)));
                }

                aValue = get(aTrack, Key.AlbumRating);
                if (aValue != null) {
                    aGraph.add(aSubj, MusicOntology.albumRating,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.INT)));
                }

                aValue = get(aTrack, Key.Year);
                if (aValue != null) {
                    aGraph.add(aSubj, MusicOntology.year,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.INT)));
                }

                aValue = get(aTrack, Key.Name);
                if (aValue != null) {
                    aGraph.add(aSubj, DC.ontology().title,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.STRING)));
                }

                aValue = get(aTrack, Key.Album);
                if (aValue != null) {
                    aGraph.add(album(aValue), MusicOntology.track, aSubj);
                    aGraph.add(album(aValue), MusicOntology.release_status, MusicOntology.official);
                    aGraph.add(album(aValue), MusicOntology.release_type, MusicOntology.album);
                    // TODO: is this the correct type?
                    aGraph.add(album(aValue), URIImpl.RDF_TYPE, MusicOntology.Record);
					aGraph.add(album(aValue), URIImpl.RDFS_LABEL, aGraph.getValueFactory().createLiteral(aValue,
                                                                                                          aGraph.getValueFactory().createURI(XmlSchema.STRING)));
					aGraph.add(album(aValue), DC.ontology().title, aGraph.getValueFactory().createLiteral(aValue,
                                                                                                          aGraph.getValueFactory().createURI(XmlSchema.STRING)));
                }

                aValue = get(aTrack, Key.Artist);
                if (aValue != null) {
                    String aAlbum = get(aTrack, Key.Album);

                    aGraph.add(album(aAlbum), FOAF.maker, artist(aValue));
                    aGraph.add(artist(aValue), URIImpl.RDF_TYPE, MusicOntology.MusicGroup);
                    aGraph.add(artist(aValue), URIImpl.RDFS_LABEL, aGraph.getValueFactory().createLiteral(aValue,
                                                                                                          aGraph.getValueFactory().createURI(XmlSchema.STRING)));
                }

                aValue = get(aTrack, Key.Composer);
                if (aValue != null) {
                    String aAlbum = get(aTrack, Key.Album);

                    for (String aName : composers(aValue)) {
                        URI aURI = makeURI("artist", aName);
                        aGraph.add(aSubj, FOAF.maker, aURI);
                        aGraph.add(album(aAlbum), FOAF.maker, aURI);

                        aGraph.add(aURI, URIImpl.RDF_TYPE, MusicOntology.MusicArtist);
                        aGraph.add(aURI, URIImpl.RDFS_LABEL, aGraph.getValueFactory().createLiteral(aName,
                                                                                                          aGraph.getValueFactory().createURI(XmlSchema.STRING)));
                    }
                }
                else {
                    String aArtist = get(aTrack, Key.Artist);
                    String aAlbum = get(aTrack, Key.Album);
                    aGraph.add(aSubj, FOAF.maker, makeURI("artist", aArtist));
                    aGraph.add(album(aAlbum), FOAF.maker, makeURI("artist", aArtist));
                }

                aValue = get(aTrack, Key.Location);
                if (aValue != null) {
                    aGraph.add(aSubj, MusicOntology.location,
                               aGraph.getValueFactory().createLiteral(aValue,
                                                                      aGraph.getValueFactory().createURI(XmlSchema.STRING)));
                }

                // TODO: olga, musicbrainz, wikipedia, image

                StatementIterator sIter = aGraph.getStatements();
                while (sIter.hasNext()) {
                    Statement aStmt = sIter.next();
                    theHandler.handleStatement(aStmt.getSubject(), aStmt.getPredicate(), aStmt.getObject());
                }
                sIter.close();
            }
        }
    }

    private URI makeURI(String theSlug, String theLabel) {
        String aURI = "http://www.clarkparsia.com/jspace/itunes/" + theSlug + "/" + clean(theLabel);
        // this just validates that we have a valid URI
        java.net.URI.create(aURI);
        return new ValueFactoryImpl().createURI(aURI);
    }

    private URI album(String theURI) {
        return makeURI("album", theURI);
    }

    private URI artist(String theURI) {
        return makeURI("artist", theURI);
    }

    private String clean(String theStr) {
        return theStr.trim().replaceAll(" ", "_").replaceAll("\\[", "(").replaceAll("\\]", ")").replaceAll("\"", "_").replaceAll("'", "_").replaceAll("`", "_");
    }

    private List<String> composers(String theNames) {
        List<String> aList = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(theNames, "/,&");
        while (st.hasMoreTokens()) {
            String aName = st.nextToken().trim();

            if (aName.equalsIgnoreCase("and")) {
                continue;
            }

            aList.add(aName);
        }

        return aList;
    }

    private List<URI> makeGenre(String theGenre) {
        List<URI> aList = new ArrayList<URI>();

        for (String aName : theGenre.split("&")) {
            aName = aName.trim();

            if (aName.equalsIgnoreCase("Alternative")) {
                aList.add(MusicOntology.Alternative);
            }
            else if (aName.equalsIgnoreCase("Blues")) {
                aList.add(MusicOntology.Blues);
            }
            else if (aName.equalsIgnoreCase("Rock")) {
                aList.add(MusicOntology.Rock);
            }
            else if (aName.equalsIgnoreCase("Punk")) {
                aList.add(MusicOntology.Punk);
            }
            else if (aName.equalsIgnoreCase("Pop")) {
                aList.add(MusicOntology.Pop);
            }
            else if (aName.equalsIgnoreCase("Funk")) {
                aList.add(MusicOntology.Funk);
            }
            else if (aName.equalsIgnoreCase("Metal")) {
                aList.add(MusicOntology.Metal);
            }
            else if (aName.equalsIgnoreCase("Jazz")) {
                aList.add(MusicOntology.Jazz);
            }
            else if (aName.equalsIgnoreCase("Soundtrack")) {
                aList.add(MusicOntology.Soundtrack);
            }
            else if (aName.equalsIgnoreCase("Vocal")) {
                aList.add(MusicOntology.Vocal);
            }
			else if (aName.equalsIgnoreCase("Country")) {
				aList.add(MusicOntology.Country);
			}
            else {
                throw new RuntimeException("unknown genre: " + aName);
            }
        }

        return aList;
    }

    private static Node findStartNode(Node theNode) {
            for (int i = 0; i < theNode.getChildNodes().getLength(); i++) {
                Node aNode = theNode.getChildNodes().item(i);
                if (aNode.getNodeName() != null && aNode.getNodeName().equals("key") && aNode.getFirstChild().getNodeValue().equals("Tracks")) {
                    return theNode.getChildNodes().item(i+1);
                }
                else {
                    aNode = findStartNode(aNode);
                    if (aNode != null)
                        return aNode;
                }
            }

        return null;
    }

//    private static int findSongIndex(Node theTrack, List theSongs) {
//        for (int i = 0; i < theSongs.size(); i++) {
//            List aSongInfo = (List) theSongs.get(i);
//
//            if (getArtist(theTrack).equals(aSongInfo.get(0)) &&
//                getTrackName(theTrack).equals(aSongInfo.get(1)))
//                return i;
//        }
//
//        return -1;
//    }

    private static String get(Node theTrack, Key theKey) {
        NodeList aList = theTrack.getChildNodes();
        for (int i = 0; i < aList.getLength(); i++) {
            Node aNode = aList.item(i);
            if (aNode.getNodeName().equals("key") && aNode.getFirstChild() != null && aNode.getFirstChild().getNodeValue().equals(theKey.key())) {
                return aList.item(i+1).getFirstChild().getNodeValue();
            }
        }
        return null;
    }
}

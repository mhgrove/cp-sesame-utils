package com.clarkparsia.sesame.utils;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Reader;
import java.io.InputStreamReader;

import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.URIImpl;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Statement;
import org.openrdf.model.Literal;

import org.openrdf.sesame.repository.SesameRepository;

import org.openrdf.sesame.constants.QueryLanguage;

import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.UnknownRepositoryException;

import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.ParseException;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.rio.RdfDocumentWriter;

import org.openrdf.rio.rdfxml.RdfXmlParser;
import org.openrdf.rio.rdfxml.AbbreviatedRdfXmlWriter;
import org.openrdf.rio.ntriples.NTriplesParser;

import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;

import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.Sesame;

import org.openrdf.vocabulary.RDF;
import com.clarkparsia.sesame.utils.query.SesameQueryUtils;
import com.clarkparsia.sesame.repository.ExtendedSesameRepository;
import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.CollectionUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 * @version 1.0
 */
public class SesameUtils
{
    private static final ValueFactory FACTORY = new ValueFactoryImpl();

	public static ExtendedSesameRepository decorate(Graph theGraph) {
		return decorate(sesameRepository(theGraph));
	}

	public static ExtendedSesameRepository decorate(SesameRepository theRepo) {
		return new ExtendedSesameRepository(theRepo);
	}

    public static SesameRepository createInMemSource() {
        try {
            return Sesame.getService().createRepository(BasicUtils.getRandomString(5), false);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<Resource> getInstancesWithType(Graph theModel, Resource theType) {
        // TODO: this function is absurdly inefficient
        Set<Resource> filter = new HashSet<Resource>();

        try {
            SesameRepository aTempRepo = sesameRepository(theModel);
            Set types = getSubClassesOf(aTempRepo, (URI) theType, false);

            types.add(theType);

            Iterator iter = listIndividuals(theModel);

            while (iter.hasNext()) {
                Resource inst = (Resource)iter.next();

                Set aTypes = getTypes(aTempRepo, (URI) inst);

                if (CollectionUtil.containsAny(aTypes, types) && !filter.contains(inst)) {
                    filter.add(inst);
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        return filter;
    }

    public static Iterator<Resource> listIndividuals(Graph theGraph) {
        return listSubjectsWithProperty(theGraph, URIImpl.RDF_TYPE);
    }

    public static Iterator<Resource> listSubjectsWithProperty(Graph theGraph, URI theProperty) {
        return listSubjectsWithProperty(theGraph, theProperty, null);
    }
    
    public static Iterator<Resource> listSubjectsWithProperty(Graph theGraph, URI theProperty, Value theValue) {
        Set<Resource> aSubjList = new HashSet<Resource>();

        StatementIterator sIter = theGraph.getStatements(null, theProperty, theValue);
        while (sIter.hasNext()) {
            Statement aStmt = sIter.next();
            aSubjList.add(aStmt.getSubject());
        }

        sIter.close();

        return aSubjList.iterator();
    }


    /**
     * Given a URI, get the local name of the resource
     * @param theURI the uri
     * @return the local name of the resource identified by the URI, or the entire URI if a local name cannot be found
     */
	public static String getLocalName(String theURI) {

		int aIndex = theURI.lastIndexOf( "#" );

		if( aIndex == -1 )
			aIndex = theURI.lastIndexOf( "/" );

		return theURI.substring( aIndex + 1 );
	}
    
    public static boolean isList(SesameRepository theRepo, Resource theRes) {
        return theRes.equals(URIImpl.RDF_NIL) || getValue(theRepo, theRes, URIImpl.RDF_FIRST) != null;
    }

    public static List<Value> asList(Graph theGraph, Resource theRes) {
        ArrayList<Value> aList = new ArrayList<Value>();

        Resource aListRes = theRes;

        while (aListRes != null) {

            Resource aFirst = (Resource) getValue(theGraph, aListRes, URIImpl.RDF_FIRST);
            Resource aRest = (Resource) getValue(theGraph, aListRes, URIImpl.RDF_REST);

            if (aFirst != null) {
               aList.add(aFirst);
            }

            if (aRest == null || aRest.equals(URIImpl.RDF_NIL)) {
               aListRes = null;
            }
            else {
                aListRes = aRest;
            }
        }

        return aList;
    }

    public static boolean isList(Graph theGraph, Resource theRes) {
        StatementIterator sIter = theGraph.getStatements(theRes, URIImpl.RDF_FIRST, null);

        try {
            return theRes != null && theRes.equals(URIImpl.RDF_NIL) || sIter.hasNext();
        }
        finally {
            sIter.close();
        }
    }

    /**
     * Returns the namepace used by the given URI identifier
     * @param theURI the uri
     * @return the namespace
     */
	public static String getNamespace(String theURI) {

		int aIndex = theURI.lastIndexOf( "#" );

		if( aIndex == -1 )
			aIndex = theURI.lastIndexOf( "/" );

		return theURI.substring( 0, aIndex );
	}

    public static SesameRepository sesameRepository(Graph theGraph) {
        try {
            SesameRepository aRepo = Sesame.getService().createRepository("test-"+System.currentTimeMillis(), false);
            aRepo.addGraph(theGraph);

            return aRepo;
        }
        catch (Exception e) {
            e.printStackTrace();
            
            throw new RuntimeException(e);
        }
    }

    public static Set<Resource> getTypes(SesameRepository theRepo, URI theRes) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        HashSet<Resource> aTypes = new HashSet<Resource>();

        String aQuery = "select aType from {<" + theRes + ">} rdf:type {aType}";

        QueryResultsTable aTable = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

        for (int i = 0; i < aTable.getRowCount(); i++) {
            URI aType = (URI)aTable.getValue(i,0);

            aTypes.add(aType);

			// TODO: this is wrong, this should get all the superclasses of aType, not the other types of type.
            aTypes.addAll(getTypes(theRepo, aType));
        }

        return aTypes;
    }

    private static Set<URI> mProcessedClassList = new HashSet<URI>();
    public static Set<URI> getSubClassesOf(SesameRepository theRepo, URI theRes) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        return getSubClassesOf(theRepo, theRes, false);
    }

    public static Set<URI> getSubClassesOf(SesameRepository theRepo, URI theRes, boolean theDirect) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        mProcessedClassList = new HashSet<URI>();
        return helpGetSubClassesOf(theRepo, theRes, theDirect);
    }

    private static Set<URI> helpGetSubClassesOf(SesameRepository theRepo, URI theRes, boolean theDirect) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        HashSet<URI> aSubClasses = new HashSet<URI>();

        String aQuery = "select sc from {sc} rdfs:subClassOf {<" + theRes + ">}";

        QueryResultsTable aTable = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

        for (int i = 0; i < aTable.getRowCount(); i++) {
            URI aSC = (URI)aTable.getValue(i,0);
            aSubClasses.add(aSC);

            if (!theDirect && !mProcessedClassList.contains(aSC)) {
                aSubClasses.addAll(getSubClassesOf(theRepo, aSC));
            }
        }

        return aSubClasses;
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph turtleToGraph(InputStream theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        return turtleToGraph(new InputStreamReader(theInput), theBase);
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph turtleToGraph(String theTurtle, String theBase) throws IOException, ParseException, StatementHandlerException {
        return turtleToGraph(new StringReader(theTurtle), theBase);
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph turtleToGraph(Reader theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        Graph aGraph = new GraphImpl();
        TurtleParser aRDFParser = new TurtleParser(aGraph.getValueFactory());
        StatementHandler aHandler = new BuildGraphStatementHandler(aGraph);
        aRDFParser.setStatementHandler(aHandler);
        aRDFParser.parse(theInput, theBase);
        return aGraph;
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph rdfToGraph(InputStream theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        return rdfToGraph(new InputStreamReader(theInput), theBase);
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph rdfToGraph(Reader theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        Graph aGraph = new GraphImpl();
        RdfXmlParser aRDFParser = new RdfXmlParser(aGraph.getValueFactory());
        StatementHandler aHandler = new BuildGraphStatementHandler(aGraph);
        aRDFParser.setStatementHandler(aHandler);
        aRDFParser.parse(theInput, theBase);
        return aGraph;
    }

	/**
	 * @see SesameIO.readRepository
	 */
	@Deprecated
    public static SesameRepository rdfToRepository(InputStream theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        Graph aGraph = rdfToGraph(theInput, theBase);

        try {
            SesameRepository aRepo = Sesame.getService().createRepository("", false);
            aRepo.addGraph(aGraph);

            return aRepo;
        }
        catch (Exception e) {
            // TODO: better error handling
            throw new RuntimeException(e);
        }
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph ntriplesToGraph(InputStream theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        return ntriplesToGraph(new InputStreamReader(theInput), theBase);
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph ntriplesToGraph(String theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        return ntriplesToGraph(new StringReader(theInput), theBase);
    }

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph ntriplesToGraph(Reader theInput, String theBase) throws IOException, ParseException, StatementHandlerException {
        Graph aGraph = new GraphImpl();
        NTriplesParser aRDFParser = new NTriplesParser(aGraph.getValueFactory());
        StatementHandler aHandler = new BuildGraphStatementHandler(aGraph);
        aRDFParser.setStatementHandler(aHandler);
        aRDFParser.parse(theInput, theBase);
        return aGraph;
    }

    public static Graph rdfToGraph(String theRDF, String theBase) throws IOException, ParseException, StatementHandlerException {
        Graph aGraph = new GraphImpl();
        RdfXmlParser aRDFParser = new RdfXmlParser(aGraph.getValueFactory());
        StatementHandler aHandler = new BuildGraphStatementHandler(aGraph);
        aRDFParser.setStatementHandler(aHandler);
        aRDFParser.parse(new StringReader(theRDF), theBase);
        return aGraph;
    }

    public static Graph tableToGraph(QueryResultsTable theResults)
    {
        try {
            Graph aGraph = new GraphImpl();

            for (int aRow = 0; aRow < theResults.getRowCount(); aRow++) {
                Resource aSubj = null;
                URI aPred = null;
                Value aObj = null;

                for (int aCol = 0; aCol < theResults.getColumnCount(); aCol++) {
                    if (theResults.getColumnName(aCol).equals("s"))
                        aSubj = (Resource)theResults.getValue(aRow, aCol);
                    else if (theResults.getColumnName(aCol).equals("p"))
                        aPred = (URI)theResults.getValue(aRow, aCol);
                    else if (theResults.getColumnName(aCol).equals("o")) {
                        aObj = theResults.getValue(aRow, aCol);
                    }
                }

                if (aSubj != null && aPred != null && aObj != null) {
                    aGraph.add(aSubj, aPred, aObj);
                }
            }

            return aGraph;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

	/**
	 * @see SesameIO.writeGraph
	 */
	@Deprecated
    public static String graphAsRDF(Graph theGraph) throws IOException {
        StringWriter aWriter = new StringWriter();

        AbbreviatedRdfXmlWriter aRDFWriter = new AbbreviatedRdfXmlWriter(aWriter);

        writeGraph(theGraph, aRDFWriter);

        return aWriter.toString();
    }

	/**
	 * @see SesameIO.writeGraph
	 */
	@Deprecated
    public static String graphAsTurtle(Graph theGraph) throws IOException {
        StringWriter aWriter = new StringWriter();

        RdfDocumentWriter aRDFWriter = new TurtleWriter(aWriter);

        writeGraph(theGraph, aRDFWriter);

        return aWriter.toString();
    }

	/**
	 * @see SesameIO.writeGraph
	 */
	@Deprecated
    private static void writeGraph(Graph theGraph, RdfDocumentWriter theWriter) throws IOException {
        theWriter.startDocument();
        StatementIterator sIter = theGraph.getStatements();

        while (sIter.hasNext()) {
            Statement aStmt = sIter.next();
            theWriter.writeStatement(aStmt.getSubject(), aStmt.getPredicate(), aStmt.getObject());
        }
        theWriter.endDocument();
        sIter.close();
    }

    public static StmtIterator getStatements(SesameRepository theRepo, Resource theSubj, URI thePred, Value theObj) {
        String aQuery = "construct * from {s} p {o} ";


        try {
            if (theSubj != null || thePred != null || theObj != null) {
                aQuery += " where ";
			}

            boolean needsAnd = false;
            if (theSubj != null) {
                aQuery += " (s = "+ SesameQueryUtils.getQueryString(theSubj)+ ") ";
                needsAnd = true;
            }

            if (thePred != null) {
                if (needsAnd) {
                    aQuery += " and ";
				}
                aQuery += " (p = "+ SesameQueryUtils.getQueryString(thePred)+") ";
                needsAnd = true;
            }

            if (theObj != null) {
                if (needsAnd) {
                    aQuery += " and ";
				}
                aQuery += " (o = "+ SesameQueryUtils.getQueryString(theObj)+")";
            }

            return new StmtIterator(theRepo.performGraphQuery(QueryLanguage.SERQL, aQuery).getStatements());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return new StmtIterator();
        }
    }

    public static boolean isType(Graph theGraph, URI theRes, Resource theType) {
        try {
            return getTypes(sesameRepository(theGraph), theRes).contains(theType);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isType(SesameRepository theRepo, URI theRes, Resource theType) {
        try {
            return getTypes(theRepo, theRes).contains(theType);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasStatement(SesameRepository theRepo, Resource theSubj, URI thePred, Value theObj) {
        try {
//            String aQuery = "select * from {s} p {o} where (s = "+ SesameQueryUtils.getQueryString(theSubj)+ ") and (p = "+ SesameQueryUtils.getQueryString(thePred)+") and (o = "+ SesameQueryUtils.getQueryString(theObj)+")";
//
//            QueryResultsTable aTable = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);
//
//            return aTable.getRowCount() > 0;

            String aQuery = "select distinct s from {s} p {o} ";

            if (theSubj != null || thePred != null || theObj != null)
                aQuery += " where ";

            boolean needsAnd = false;
            if (theSubj != null) {
                aQuery += " (s = "+SesameQueryUtils.getQueryString(theSubj)+ ") ";
                needsAnd = true;
            }

            if (thePred != null) {
                if (needsAnd)
                    aQuery += " and ";
                aQuery += " (p = "+SesameQueryUtils.getQueryString(thePred)+") ";
                needsAnd = true;
            }

            if (theObj != null) {
                if (needsAnd)
                    aQuery += " and ";
                aQuery += " (o = "+SesameQueryUtils.getQueryString(theObj)+")";
            }

            aQuery += " limit 1";

            return theRepo.performTableQuery(QueryLanguage.SERQL, aQuery).getRowCount() > 0;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static Value getValue(SesameRepository theRepo, Resource theSubj, URI thePred) {
        Iterator<Value> aIter = getValues(theRepo, theSubj, thePred);
        
        if (aIter.hasNext()) {
            return aIter.next();
        }
        else return null;
    }

    public static Literal getLiteral(SesameRepository theRepository, Resource theSubj, URI thePred) {
        return (Literal) getValue(theRepository, theSubj, thePred);
    }

    public static Literal getLiteral(Graph theGraph, Resource theSubj, URI thePred) {
        return (Literal) getValue(theGraph, theSubj, thePred);
    }

    public static Value getValue(Graph theGraph, Resource theSubj, URI thePred) {
        Iterator<Value> aIter = getValues(theGraph, theSubj, thePred);

        if (aIter.hasNext()) {
            return aIter.next();
        }
        else {
            return null;
        }
    }

    public static Iterator<Value> getValues(Graph theGraph, Resource theSubj, URI thePred) {
        StatementIterator sIter = theGraph.getStatements(theSubj, thePred, null);
        HashSet<Value> aSet = new HashSet<Value>();
        while (sIter.hasNext()) {
            aSet.add(sIter.next().getObject());
        }
        sIter.close();

        return aSet.iterator();
    }

    public static Iterator<Value> getValues(SesameRepository theRepo, Resource theSubj, URI thePred) {
        try {
            String aQuery = "select value from {"+ SesameQueryUtils.getQueryString(theSubj)+"} <"+thePred+"> {value}";

            QueryResultsTable aTable = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

            Set<Value> aSet = new LinkedHashSet<Value>();
            for (int i = 0; i < aTable.getRowCount(); i++)
                aSet.add(aTable.getValue(i,0));

            return aSet.iterator();
        }
        catch (Exception ex) {
            //System.err.println("Error getting value for "+theSubj+", "+thePred);
        }

        return new HashSet<Value>().iterator();
    }

    public static URI getType(SesameRepository theRepo, Resource theRes) {
        return (URI)getValue(theRepo, theRes, FACTORY.createURI(RDF.TYPE));
    }

    public static Iterator<Resource> getSubjects(SesameRepository theRepo, URI thePred, Value theObject) {
        String aQuery = "select uri from {uri} " + (thePred == null ? "p" : SesameQueryUtils.getQueryString(thePred)) + " {" + (theObject == null ? "o" : SesameQueryUtils.getQueryString(theObject)) + "}";

        Set<Resource> aSet = new LinkedHashSet<Resource>();

        try {
            QueryResultsTable aTable = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

            for (int i = 0; i < aTable.getRowCount(); i++)
                aSet.add( (Resource) aTable.getValue(i,0));

            return aSet.iterator();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static int numStatements(Graph theGraph) {
        int aCount = 0;

        StatementIterator sIter = theGraph.getStatements();
        while (sIter.hasNext()) {
            sIter.next();
            aCount++;
        }
        sIter.close();

        return aCount;
    }
}
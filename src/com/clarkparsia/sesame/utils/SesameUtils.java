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

package com.clarkparsia.sesame.utils;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

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
import org.openrdf.sesame.constants.RDFFormat;

import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import org.openrdf.sesame.config.AccessDeniedException;

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
import com.clarkparsia.sesame.utils.query.Binding;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;
import com.clarkparsia.sesame.repository.ExtendedSesameRepository;
import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.Predicate;
import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * <p>Utility functions for working with the Sesame API</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class SesameUtils {
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

	public static Graph asGraph(StmtIterator theStatements) {
		Graph aGraph = new GraphImpl();

		while (theStatements.hasNext()) {
			aGraph.add(theStatements.next());
		}

		theStatements.close();

		return aGraph;
	}

	public static Graph asGraph(Statement... theStatements) {
		return asGraph(new StmtIterator(theStatements));
	}

    public static Set<Resource> getInstancesWithType(Graph theModel, Resource theType) {
        // TODO: this function is absurdly inefficient
        Set<Resource> filter = new HashSet<Resource>();

        try {
            final SesameRepository aTempRepo = sesameRepository(theModel);
            final Set<Resource> types = getSubClassesOf(aTempRepo, theType, false);

            types.add(theType);

            Iterator<Resource> aIter = listIndividuals(theModel);

			Collection<Resource> aFilteredList = CollectionUtil.filter(aIter, new Predicate<Resource>() {
				public boolean accept(Resource theRes) {
					try {
						return !CollectionUtil.intersection(types, getTypes(aTempRepo, theRes)).isEmpty();
					}
					catch (Exception e) {
						// TODO: better error handling than this
						e.printStackTrace();
						return false;
					}
				}
			});

			// this will remove any dups
            filter.addAll(aFilteredList);
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
	@Deprecated
	public static String getLocalName(String theURI) {

		int aIndex = theURI.lastIndexOf( "#" );

		if( aIndex == -1 )
			aIndex = theURI.lastIndexOf( "/" );

		return theURI.substring( aIndex + 1 );
	}

	@Deprecated
    public static boolean isList(SesameRepository theRepo, Resource theRes) {
        return decorate(theRepo).isList(theRes);
    }

	@Deprecated
    public static List<Value> asList(Graph theGraph, Resource theRes) {
		return decorateGraph(theGraph).asList(theRes);
    }

	@Deprecated
    public static boolean isList(Graph theGraph, Resource theRes) {
		return decorateGraph(theGraph).isList(theRes);
    }

    /**
     * Returns the namepace used by the given URI identifier
     * @param theURI the uri
     * @return the namespace
     */
	@Deprecated
	public static String getNamespace(String theURI) {

		int aIndex = theURI.lastIndexOf( "#" );

		if( aIndex == -1 )
			aIndex = theURI.lastIndexOf( "/" );

		return theURI.substring( 0, aIndex );
	}

	/**
	 * Returns the graph as a sesame repository
	 * @param theGraph the graph to convert to a SesameRepository
	 * @return a sesame repository with the same set of statements as the graph
	 */
    public static SesameRepository sesameRepository(Graph theGraph) {
        try {
            SesameRepository aRepo = createInMemSource();
			
            aRepo.addGraph(theGraph);

            return aRepo;
        }
        catch (Exception e) {
            e.printStackTrace();
            
            throw new RuntimeException(e);
        }
    }

	/**
	 * Returns all the types belonging to the given instance
	 * @param theRepo the repository to query
	 * @param theRes the instance whose types we want to get
	 * @return the set of types for the given resource
	 * @throws IOException thrown if there is an error while querying
	 * @throws AccessDeniedException thrown if you cannot access the given repository
	 * @throws MalformedQueryException thrown if there is an error while querying
	 * @throws QueryEvaluationException thrown if there is an error while querying
	 */
	@Deprecated
	/**
	 * @see nothing -- i think we will do away with this faux-reasoning
	 */
    public static Set<Resource> getTypes(SesameRepository theRepo, Resource theRes) throws IOException, AccessDeniedException, QueryEvaluationException {
        Set<Resource> aTypes = new HashSet<Resource>();

        String aQuery = "select aType from {" + SesameQueryUtils.getQueryString(theRes) + "} rdf:type {aType}";

		try {
			QueryResultsTable aTable = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

			for (Binding aBinding : IterableQueryResultsTable.iterable(aTable)) {
				Resource aType = aBinding.getResource("aType");

				aTypes.add(aType);

				// TODO: this is wrong, this should get all the superclasses of aType, not the other types of type.
				aTypes.addAll(getTypes(theRepo, aType));
			}
		}
		catch (MalformedQueryException e) {
			// we know this won't happy since we created the query above, we can safely ignore this
		}

		return aTypes;
    }

    private static Set<Resource> mProcessedClassList = new HashSet<Resource>();
    public static Set<Resource> getSubClassesOf(SesameRepository theRepo, Resource theRes) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        return getSubClassesOf(theRepo, theRes, false);
    }

    public static Set<Resource> getSubClassesOf(SesameRepository theRepo, Resource theRes, boolean theDirect) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        mProcessedClassList = new HashSet<Resource>();
        return helpGetSubClassesOf(theRepo, theRes, theDirect);
    }

    private static Set<Resource> helpGetSubClassesOf(SesameRepository theRepo, Resource theRes, boolean theDirect) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        Set<Resource> aSubClasses = new HashSet<Resource>();

        String aQuery = "select sc from {sc} rdfs:subClassOf {" + SesameQueryUtils.getQueryString(theRes) + "}";

        QueryResultsTable aTable = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

		for (Binding aBinding : IterableQueryResultsTable.iterable(aTable)) {
            Resource aSC = aBinding.getResource("sc");

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

	/**
	 * @see SesameIO#readGraph
	 */
	@Deprecated
    public static Graph rdfToGraph(String theRDF, String theBase) throws IOException, ParseException, StatementHandlerException {
        Graph aGraph = new GraphImpl();
        RdfXmlParser aRDFParser = new RdfXmlParser(aGraph.getValueFactory());
        StatementHandler aHandler = new BuildGraphStatementHandler(aGraph);
        aRDFParser.setStatementHandler(aHandler);
        aRDFParser.parse(new StringReader(theRDF), theBase);
        return aGraph;
    }

    public static Graph tableToGraph(QueryResultsTable theResults) {
        try {
            Graph aGraph = new GraphImpl();

			for (Binding aBinding : IterableQueryResultsTable.iterable(theResults)) {
				Resource aSubj = aBinding.getResource("s");
				URI aPred = aBinding.getURI("p");
				Value aObj = aBinding.get("o");

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

	@Deprecated
    public static StmtIterator getStatements(SesameRepository theRepo, Resource theSubj, URI thePred, Value theObj) {
		return new StmtIterator(decorate(theRepo).getStatements(theSubj, thePred, theObj));
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

	@Deprecated
    public static boolean hasStatement(SesameRepository theRepo, Resource theSubj, URI thePred, Value theObj) {
		return decorate(theRepo).hasStatement(theSubj, thePred, theObj);
    }

	@Deprecated
	/**
	 * @see ExtendedSesameRepository#getValue
	 */
    public static Value getValue(SesameRepository theRepo, Resource theSubj, URI thePred) {
		return decorate(theRepo).getValue(theSubj, thePred);
    }

	@Deprecated
    public static Literal getLiteral(SesameRepository theRepository, Resource theSubj, URI thePred) {
        return (Literal) getValue(theRepository, theSubj, thePred);
    }

	@Deprecated
    public static Literal getLiteral(Graph theGraph, Resource theSubj, URI thePred) {
        return (Literal) getValue(theGraph, theSubj, thePred);
    }

	@Deprecated
    public static Value getValue(Graph theGraph, Resource theSubj, URI thePred) {
		return decorateGraph(theGraph).getValue(theSubj, thePred);
    }

	@Deprecated
    public static Iterator<Value> getValues(Graph theGraph, Resource theSubj, URI thePred) {
		return decorateGraph(theGraph).getValues(theSubj, thePred);
    }

	@Deprecated
    public static Iterator<Value> getValues(SesameRepository theRepo, Resource theSubj, URI thePred) {
		return decorate(theRepo).getValues(theSubj, thePred).iterator();
    }

    public static URI getType(SesameRepository theRepo, Resource theRes) {
        return (URI) getValue(theRepo, theRes, FACTORY.createURI(RDF.TYPE));
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

	@Deprecated
    public static int numStatements(Graph theGraph) {
		return decorateGraph(theGraph).numStatements();
    }

	private static ExtendedGraph decorateGraph(Graph theGraph) {
		return new ExtendedGraph(theGraph);
	}
}
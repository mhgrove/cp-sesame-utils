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

import org.openrdf.model.Graph;
import org.openrdf.model.Value;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.vocabulary.XmlSchema;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.rio.ParseException;

import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import java.io.Writer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.clarkparsia.utils.io.IOUtil;

import com.clarkparsia.utils.collections.CollectionUtil;
import static com.clarkparsia.utils.collections.CollectionUtil.transform;

import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.FunctionUtil;
import static com.clarkparsia.utils.FunctionUtil.compose;
import com.clarkparsia.sesame.utils.query.SesameQuery;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;

/**
 * <p>A decorator for a Sesame graph which provides some useful utility functions for common operations not present in
 * the Sesame core API.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class ExtendedGraph extends DecoratableGraph implements Graph, Iterable<Statement> {

	/**
	 * Create a new (empty) ExtendedGraph
	 */
	public ExtendedGraph() {
		super(new GraphImpl());
	}

	/**
	 * Create a new ExtendedGraph
	 * @param theGraph the initial contents of the graph
	 */
	public ExtendedGraph(Graph theGraph) {
		super(theGraph);
	}

	/**
	 * Method which proxies for {@link #getStatements} but instead returns a {@link StmtIterator} which allows these calls
	 * to be used in for-each loops.
	 * @return an iterator over all statements in the graph
	 */
	public StmtIterator statements() {
		return new StmtIterator(getStatements());
	}

	/**
	 * Return the superclasses of the given resource
	 * @param theRes the resource
	 * @return the resource's superclasses
	 */
	public Iterable<Resource> getSuperclasses(Resource theRes) {
		return transform(statements(theRes, URIImpl.RDFS_SUBCLASSOF, null), compose(new StatementToValue(Position.Object), new FunctionUtil.Cast<Value, Resource>(Resource.class)));
	}

	/**
	 * Return the subclasses of the given resource
	 * @param theRes the resource
	 * @return the subclasses of the resource
	 */
	public Iterable<Resource> getSubclasses(Resource theRes) {
		return transform(statements(null, URIImpl.RDFS_SUBCLASSOF, theRes), compose(new StatementToValue(Position.Subject), new FunctionUtil.Cast<Value, Resource>(Resource.class)));
	}

	/**
	 * Method which proxies for {@link #getStatements} but instead returns a {@link StmtIterator} over all statements
	 * matching the provided spo pattern.
	 * @param theSubj the subject to match, or null for any
	 * @param thePred the predicate to match, or null for any
	 * @param theObj the object to match, or null for any
	 * @return a StmtIterator over all matching statements
	 */
	public StmtIterator statements(Resource theSubj, URI thePred, Value theObj) {
		return new StmtIterator(getStatements(theSubj, thePred, theObj));
	}

	/**
	 * Create a new ExtendedGraph
	 * @param theStatements the statements which make up the initial contents of the Graph
	 */
	public ExtendedGraph(Iterable<Statement> theStatements) {
		this();

		add(new StmtIterator(theStatements));
	}

	/**
	 * @inheritDoc
	 */
	public Iterator<Statement> iterator() {
		return new StmtIterator(getStatements());
	}

	/**
	 * Returns whether or not the given resource is a rdf:List
	 * @param theRes the resource to check
	 * @return true if its a list, false otherwise
	 */
	public boolean isList(Resource theRes) {
        StatementIterator sIter = getStatements(theRes, URIImpl.RDF_FIRST, null);

        try {
            return theRes != null && theRes.equals(URIImpl.RDF_NIL) || sIter.hasNext();
        }
        finally {
            sIter.close();
        }
	}

	/**
	 * Return the contents of the given list by following the rdf:first/rdf:rest structure of the list
	 * @param theRes the resouce which is the head of the list
	 * @return the contents of the list.
	 */
	public List<Value> asList(Resource theRes) {
        List<Value> aList = new ArrayList<Value>();

        Resource aListRes = theRes;

        while (aListRes != null) {

            Resource aFirst = (Resource) getValue(aListRes, URIImpl.RDF_FIRST);
            Resource aRest = (Resource) getValue(aListRes, URIImpl.RDF_REST);

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

	/**
	 * Return the value of the property for the given subject.  If there are multiple values, only the first value will
	 * be returned.  Use {@link #getValues} if you want all values for the property.
	 * @param theSubj the subject
	 * @param thePred the property of the subject whose value should be retrieved
	 * @return the value of the the property for the subject, or null if there is no value.
	 */
	public Value getValue(Resource theSubj, URI thePred) {
		Iterator<Value> aIter = getValues(theSubj, thePred).iterator();

		if (aIter.hasNext()) {
			return aIter.next();
		}
		else {
			return null;
		}
	}

	/**
	 * Return the rdf:type of the resource
	 * @param theSubj the resource
	 * @return the rdf:type, or null if it is not typed.
	 */
	public URI getType(Resource theSubj) {
		return (URI) getValue(theSubj, URIImpl.RDF_TYPE);
	}

	/**
	 * Return the value of of the property as a Literal
	 * @param theRes the resource
	 * @param theProp the property whose value is to be retrieved
	 * @return the property value as a literal, or null if the value is not a literal, or the property does not have a value
	 */
	public Literal getLiteral(Resource theRes, URI theProp) {
		try {
			return (Literal) getValue(theRes, theProp);
		}
		catch (ClassCastException e) {
			return null;
		}
	}

	/**
	 * Return an Iterable over all the values of the property on the given resource
	 * @param theSubj the resource
	 * @param thePred the property
	 * @return all values of the property on the resource.
	 */
	public Iterable<Value> getValues(Resource theSubj, URI thePred) {
        StatementIterator sIter = getStatements(theSubj, thePred, null);

		return CollectionUtil.transform(new StmtIterator(sIter), new Function<Statement, Value>() {
			public Value apply(final Statement theIn) {
				return theIn.getObject();
			}
		});
	}

	/**
	 * Return whether or not the resource has the specified property
	 * @param theRes the resource
	 * @param theProp the property
	 * @return true if the resource has at least one assertion of the given property.
	 */
	public boolean hasProperty(Resource theRes, URI theProp) {
		return getValues(theRes, theProp).iterator().hasNext();
	}

	/**
	 * Return the rdfs:label of the given resource
	 * @param theRes the resource to get a label for
	 * @return the rdfs:label of the resource, or null if it does not have one
	 */
	public Value label(Resource theRes) {
		return getValue(theRes, URIImpl.RDFS_LABEL);
	}

	/**
	 * Returns all the instances of the specified type
	 * @param theType the type for instances to return
	 * @return all instances in the graph rdf:type'd to the given type.
	 */
	public Set<Resource> instancesOf(Resource theType) {
		return SesameUtils.getInstancesWithType(this, theType);
	}

	/**
	 * Returns the value of the property on the given resource as a boolean.
	 * @param theRes the resource
	 * @param theProp the property
	 * @return the value of the property as a boolean, or null if it doesnt have a value, or if the value is not a boolean.
	 */
	public Boolean getBooleanValue(Resource theRes, URI theProp) {
		Value aVal = getValue(theRes, theProp);

		if (aVal instanceof Literal) {
			Literal aLit = (Literal) aVal;

			if ((aLit.getDatatype() != null && aLit.getDatatype().getURI().equals(XmlSchema.BOOLEAN)) ||
				aLit.getLabel().equalsIgnoreCase("true") ||
				aLit.getLabel().equalsIgnoreCase("false")) {
				return Boolean.valueOf(aLit.getLabel());
			}
			else {
				// TODO: is this an error?
				return null;
			}
		}
		else {
			// TODO: is this an error?
			return null;
		}
	}

	/**
	 * Return the number of statements in this graph
	 * @return the statement count
	 */
	public int numStatements() {
		int aCount = 0;

		StatementIterator sIter = getStatements();
		while (sIter.hasNext()) {
			sIter.next();
			aCount++;
		}
		sIter.close();

		return aCount;
	}

	/**
	 * Write the contents of this graph in the specified format to the output
	 * @param theWriter the output to write to
	 * @param theFormat the format to write the graph data in
	 * @throws IOException thrown if there is an error while writing the data
	 */
	public void write(Writer theWriter, RDFFormat theFormat) throws IOException {
		SesameIO.writeGraph(this, theWriter, theFormat);
	}

	/**
	 * Write the contents of this graph in the specified format to the output stream
	 * @param theStream the stream to write to
	 * @param theFormat the format to write the data in
	 * @throws IOException if there is an error while writing to the stream
	 */
	public void write(OutputStream theStream, RDFFormat theFormat) throws IOException {
		write(new OutputStreamWriter(theStream), theFormat);
	}

	public void read(InputStream theInput, RDFFormat theFormat) throws IOException, ParseException {
		read(new InputStreamReader(theInput), theFormat);
	}

	public void read(Reader theReader, RDFFormat theFormat) throws IOException, ParseException {
		add(SesameIO.readGraph(theReader, theFormat));
	}

	public void read(InputStream theStream) throws IOException, ParseException {
		read(new InputStreamReader(theStream));
	}

	public void read(Reader theReader) throws IOException, ParseException {
		String aFileData = IOUtil.readStringFromReader(theReader);

		Graph aGraph;

		try {
			aGraph = SesameIO.readGraph(new StringReader(aFileData), RDFFormat.TURTLE);
		}
		catch (ParseException e) {
			// ok, so its probably not turtle, lets try rdf/xml

			try {
				aGraph = SesameIO.readGraph(new StringReader(aFileData), RDFFormat.RDFXML);
			}
			catch (ParseException e1) {
				// ok, not rdf/xml either, lets try ntriples

				try {
					aGraph = SesameIO.readGraph(new StringReader(aFileData), RDFFormat.NTRIPLES);
				}
				catch (ParseException e2) {
					throw new IOException("Unable to parse input, not a known (ttl, rdf/xml, nt) format!");
				}
			}
		}

		if (aGraph != null) {
			add(aGraph);
		}
	}

	/**
	 * Perform the select query on this graph
	 * @param theQuery the query to execute
	 * @return the results of the query
	 * @throws MalformedQueryException if the query is not in valid syntax
	 * @throws IOException if there is an error while sending the query
	 * @throws QueryEvaluationException if there is an error while evaluating the query
	 * @throws AccessDeniedException if this graph cannot be accessed.
	 */
	public IterableQueryResultsTable query(SesameQuery theQuery) throws MalformedQueryException, IOException, QueryEvaluationException, AccessDeniedException {
		return SesameUtils.decorate(this).performSelectQuery(theQuery);
	}

	private enum Position {
		Subject, Predicate, Object
	}

	private class StatementToValue implements Function<Statement, Value> {

		private Position mPosition;

		private StatementToValue(Position thePos) {
			mPosition = thePos;
		}

		public Value apply(final Statement theIn) {
			switch (mPosition) {
				case Subject:
					return theIn.getSubject();
				case Predicate:
					return theIn.getPredicate();
				case Object:
					return theIn.getObject();
				default:
					return null;
			}
		}
	}
}

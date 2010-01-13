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

package com.clarkparsia.sesame.repository;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.admin.StdOutAdminListener;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.util.io.IOUtil;
import org.openrdf.rio.ParseException;

import com.clarkparsia.sesame.utils.SesameUtils;
import com.clarkparsia.sesame.utils.SesameIO;
import com.clarkparsia.sesame.utils.ExtendedGraph;
import com.clarkparsia.sesame.utils.StmtIterator;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;
import com.clarkparsia.sesame.utils.query.SesameQuery;
import com.clarkparsia.sesame.utils.query.SesameQueryUtils;
import com.clarkparsia.sesame.utils.query.Binding;
import com.clarkparsia.utils.collections.CollectionUtil;
import com.clarkparsia.utils.Function;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

/**
 * <p>Decorator for a SesameRepository that adds some utility functions, very similar in scope and function to
 * {@link ExtendedGraph}</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class ExtendedSesameRepository extends BaseSesameRepository implements SesameRepository {

	/**
	 * Create an in-memory ExtendedSesameRepository
	 */
	public ExtendedSesameRepository() {
		this(SesameUtils.createInMemSource());
	}

	/**
	 * Wrap the provided SesameRepository as an ExtendedSesameRepository
	 * @param theRepo the repository to decorate
	 */
	public ExtendedSesameRepository(SesameRepository theRepo) {
		super(theRepo);

		// TODO: maybe move all the stuff we use from sesame utils into here?  this class should
		// make a lot of that junk moot
	}

	/**
	 * List all the subjects which have the given predicate and object.
	 * @param thePredicate the predicate to search for, or null for any predicate
	 * @param theObject the object to search for, or null for any object
	 * @return the list of subjects who have properties matching the po pattern.
	 */
	public Iterable<Resource> getSubjects(URI thePredicate, Value theObject) {
		return CollectionUtil.list(SesameUtils.getSubjects(this, thePredicate, theObject));
	}

	/**
	 * Return the values of the subject for the given property
	 * @param theSubj the subject
	 * @param thePred the property of the subject to get values for
	 * @return an iterable set of values of the property
	 */
	public Iterable<Value> getValues(Resource theSubj, URI thePred) {
		if (theSubj == null || thePred == null) {
			return Collections.emptySet();
		}

        try {
            String aQuery = "select value from {"+ SesameQueryUtils.getQueryString(theSubj)+"} <"+thePred+"> {value}";

            IterableQueryResultsTable aTable = performSelectQuery(QueryLanguage.SERQL, aQuery);

            return CollectionUtil.transform(aTable.iterator(), new Function<Binding, Value>() {
				public Value apply(final Binding theIn) {
					return theIn.get("value");
				}
			});
        }
        catch (Exception ex) {
            //System.err.println("Error getting value for "+theSubj+", "+thePred);
        }

        return new HashSet<Value>();
	}

	/**
	 * Return whether or not the given resource represents an rdf:List.
	 * @param theRes the resource to inspect
	 * @return true if it is an rdf:List, false otherwise
	 */
	public boolean isList(Resource theRes) {
		return theRes.equals(URIImpl.RDF_NIL) || getValue(theRes, URIImpl.RDF_FIRST) != null;
	}

	public IterableQueryResultsTable performSelectQuery(QueryLanguage theLang, String theQuery) throws AccessDeniedException,
																									   IOException,
																									   MalformedQueryException,
																									   QueryEvaluationException {
		return IterableQueryResultsTable.iterable(performTableQuery(theLang, theQuery));
	}

	/**
	 * Query this repository to see if the statement exists.  Some or all of the arguments can be null, for example
	 * <code>hasStatement(null, URIImpl.RDF_TYPE, FOAF.Person)</code> would return true if there are <i>any</i>
	 * statements of the pattern (*,rdf:type,foaf:Person) in the repository.  But be careful when using null arguments
	 * as this could make the queries much more difficult to evaluate.
	 * @param theSubj the subject to look for, or null to look for any subject
	 * @param thePred the predicate to look for, or null to look for any predicate
	 * @param theObj the object to look for, or null to look for any object
	 * @return returns true if any statement in the repository matches the given arguments
	 */
	public boolean hasStatement(Resource theSubj, URI thePred, Value theObj) {
        try {
            String aQuery = "select distinct s from {s} p {o} ";

            if (theSubj != null || thePred != null || theObj != null)
                aQuery += " where ";

            boolean needsAnd = false;
            if (theSubj != null) {
                aQuery += " (s = "+SesameQueryUtils.getQueryString(theSubj)+ ") ";
                needsAnd = true;
            }

            if (thePred != null) {
                if (needsAnd) {
                    aQuery += " and ";
				}
                aQuery += " (p = "+SesameQueryUtils.getQueryString(thePred)+") ";
                needsAnd = true;
            }

            if (theObj != null) {
                if (needsAnd) {
                    aQuery += " and ";
				}
                aQuery += " (o = "+SesameQueryUtils.getQueryString(theObj)+")";
            }

            aQuery += " limit 1";

            return performTableQuery(QueryLanguage.SERQL, aQuery).getRowCount() > 0;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
	}

	/**
	 * Perform a select query on this repository
	 * @param theQuery the query to execute
	 * @return the results of the query
	 * @throws AccessDeniedException if the current user cannot access this repository
	 * @throws IOException if there is an error during IO of the query
	 * @throws MalformedQueryException if the provided query string is not valid
	 * @throws QueryEvaluationException thrown if there is an error while evaluating the query
	 */
	public IterableQueryResultsTable performSelectQuery(SesameQuery theQuery) throws AccessDeniedException,
																					 IOException,
																					 MalformedQueryException,
																					 QueryEvaluationException {
		return performSelectQuery(theQuery.getLanguage(), theQuery.getQueryString());
	}

	/**
	 * Perform a construct query on this Repository
	 * @param theQuery the construct query to execute
	 * @return the results of the construct query
	 * @throws AccessDeniedException thrown if the current user cannot access this Repository
	 * @throws IOException thrown if there is an error during IO of the query or results
	 * @throws MalformedQueryException thrown if the provided query is not valid
	 * @throws QueryEvaluationException thrown if there is an error while evaluating the query
	 */
	public ExtendedGraph performConstructQuery(SesameQuery theQuery) throws AccessDeniedException, IOException,
																			MalformedQueryException,
																			QueryEvaluationException {
		return new ExtendedGraph(performGraphQuery(theQuery.getLanguage(), theQuery.getQueryString()));
	}

	/**
	 * Return an Iterable set of statements over this SesameRepository.
	 * @return the statements in this repository as an Iterable
	 */
	public Iterable<Statement> getStatements() {
		return getStatements(null, null, null);
	}

	/**
	 * Return an Iterable over the statements in this Repository which match the given spo pattern.
	 * @param theSubj the subject to search for, or null for any
	 * @param thePred the predicate to search for, or null for any
	 * @param theObj the object to search for, or null for any
	 * @return an Iterable over the matching statements
	 */
	public Iterable<Statement> getStatements(Resource theSubj, URI thePred, Value theObj) {
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

            return new StmtIterator(performGraphQuery(QueryLanguage.SERQL, aQuery).getStatements());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return new StmtIterator();
        }
	}

	/**
	 * Return a graph which describes the given URI
	 * @param theURI the URI to describe
	 * @return the graph which describes the URI
	 */
	public ExtendedGraph describe(URI theURI) {
		// TODO: need a null check for theURI
		// TODO: make this align closer to a SPARQL describe
		return new ExtendedGraph(getStatements(theURI, null, null));
	}

	/**
	 * Write the contents of the Repository to the stream in the given format
	 * @param theStream the stream to write to
	 * @param theFormat the format to write the RDF as
	 * @throws IOException thrown if there is an error while writing to the stream
	 */
	public void write(OutputStream theStream, RDFFormat theFormat) throws IOException {
		write(new OutputStreamWriter(theStream), theFormat);
	}

	/**
	 * Write the contents of the Repository to the Writer in the given RDF format
	 * @param theStream the stream to write to
	 * @param theFormat the format to write the RDF as
	 * @throws IOException thrown if there is an error while writing to the stream
	 */
	public void write(Writer theStream, RDFFormat theFormat) throws IOException {
		try {
			IOUtil.transfer(new InputStreamReader(extractRDF(theFormat, true, true, true, true)), theStream);

			theStream.flush();
		}
		catch (AccessDeniedException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Add the statements to the repository
	 * @param theStatement the statement(s) to add
	 * @throws IOException thrown if there is an error while adding
	 */
	public void add(Statement... theStatement) throws IOException {
		try {
			addGraph(SesameUtils.asGraph(theStatement));
		}
		catch (AccessDeniedException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Read data in the specified format from the stream and insert it into this Repository
	 * @param theStream the stream to read data from
	 * @param theFormat the format the data is in
	 * @throws IOException thrown if there is an error while reading from the stream
	 * @throws ParseException thrown if the data cannot be parsed into the specified format
	 */
	public void read(InputStream theStream, RDFFormat theFormat) throws IOException, ParseException {
		try {
			addData(SesameIO.readRepository(theStream, theFormat), new StdOutAdminListener());
		}
		catch (AccessDeniedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return the value of the property on the resource
	 * @param theSubj the subject
	 * @param thePred the property to get from the subject
	 * @return the first value of the property for the resource, or null if it does not have the specified property or does not exist.
	 */
	public Value getValue(Resource theSubj, URI thePred) {
        Iterable<Value> aIter = getValues(theSubj, thePred);

        if (aIter.iterator().hasNext()) {
            return aIter.iterator().next();
        }
        else {
			return null;
		}
	}

	/**
	 * Return the value of the property on the resource as a literal
	 * @param theSubj the subject
	 * @param thePred the property to get from the subject
	 * @return the value of the property
	 * @see #getValue
	 */
	public Literal getLiteral(Resource theSubj, URI thePred) {
		return (Literal) getValue(theSubj, thePred);
	}

	// TODO: add an ask method
}

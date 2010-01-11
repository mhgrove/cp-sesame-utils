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

package com.clarkparsia.sesame.utils.query;

import org.openrdf.sesame.query.TableQuery;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.GraphQuery;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.serql.SerqlEngine;
import org.openrdf.sesame.sailimpl.memory.RdfRepository;
import org.openrdf.sesame.sail.query.Query;
import org.openrdf.sesame.sail.query.ValueExpr;
import org.openrdf.sesame.sail.query.LiteralExpr;
import org.openrdf.sesame.sail.query.ResourceExpr;
import org.openrdf.sesame.sail.query.SelectQuery;
import org.openrdf.sesame.sail.query.ConstructQuery;
import org.openrdf.sesame.sail.query.Union;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.BNode;

/**
 * <p>Collectino of utility methods for dealing with the Sesame Query API</p>
 *
 * @author Michael Grove
 * @since 1.0
 */

public class SesameQueryUtils {
    public static final String SPARQL = "sparql";

	/**
	 * Return a TableQuery object for the given table query string
	 * @param theQuery the query string
	 * @return a table query object
	 * @throws MalformedQueryException thrown if the query cannot be parsed
	 */
    public static TableQuery tableQuery(String theQuery) throws MalformedQueryException {
        return new SerqlEngine(new RdfRepository()).parseTableQuery(theQuery);
    }

	/**
	 * Return a GraphQuery object for the given graph query string
	 * @param theQuery the query string
	 * @return a graph query object
	 * @throws MalformedQueryException thrown if the query cannot be parsed
	 */
    public static GraphQuery graphQuery(String theQuery) throws MalformedQueryException {
        return new SerqlEngine(new RdfRepository()).parseGraphQuery(theQuery);
    }

	/**
	 * Given a select query string, return a select query object
	 * @param theQuery the query string
	 * @return a select query object
	 * @throws MalformedQueryException thrown if the query cannot be parsed
	 */
    public static SelectQuery selectQuery(String theQuery) throws MalformedQueryException {
        return (SelectQuery) tableQuery(theQuery).getQuery();
    }

	/**
	 * Given a construct query string, return the construct query object
	 * @param theQuery the query string
	 * @return a construct query object
	 * @throws MalformedQueryException thrown if the query cannot be parsed
	 */
    public static ConstructQuery constructQuery(String theQuery) throws MalformedQueryException {
        return (ConstructQuery) graphQuery(theQuery).getQuery();
    }

    /**
     * Create a Sesame variable expression for the given Node
     * @param theValue the Node we want an expression for
     * @return the equivalent Sesame variable expression object
     */
    public static ValueExpr createExpr(Value theValue) {
        if (theValue instanceof Literal) {
            return new LiteralExpr( (Literal) theValue );
        }
        else if (theValue instanceof Resource) {
            return new ResourceExpr( (Resource) theValue );
        }
        else throw new IllegalArgumentException("Invalid value to create expression for - " + theValue);
    }

    /**
     * Get a query renderer
     * @param theLang the language to render the query to
     * @return the query renderer for the given language
     */
    public static QueryRenderer getQueryRenderer(String theLang) {
        if (theLang.equalsIgnoreCase(SPARQL)) {
            return new SPARQLQueryRenderer();
        }
        else if (theLang.equalsIgnoreCase("serql")) {
            return new SERQLQueryRenderer();
        }
        else {
            throw new IllegalArgumentException("Unknown argument supplied");
        }
    }

    // this is the list of things to work on for the serql-sparql code
    // TODO: handle unions, intersection, other set ops
    // TODO: handle syntax for chaining graph patterns together?  like (s p o; p2 o2; p3 o3),
    // TODO: handle any and all keywords, as well as the exists keyword.


    /**
     * Converts Query objects from one language to another.
     * @param theQuery the input query
     * @param theSource the source language to convert from
     * @param theTarget the query language to convert to
     * @return the equivalent query represented in a different query language
     * @throws MalformedQueryException throw if there is an error while converting the query
     */
    public static String convertQuery(String theQuery, String theSource, String theTarget) throws MalformedQueryException {
        if (!theSource.equals("serql") && theTarget.equals(SPARQL)) {
            throw new UnsupportedOperationException("Can only convert SeRQL -> SPARQL");
        }

        Query aQuery = null;

        try {
            // select queries are the most common, so lets try this first, see if we can parse in the incoming query as such
            aQuery = new SerqlEngine(new RdfRepository()).parseTableQuery(theQuery).getQuery();
        }
        catch (MalformedQueryException pe) {

            try {
                // ok, so it failed to parse as a table/select query, lets try a graph/construct query.  if this fails
                // then its something we cant convert...
                aQuery = new SerqlEngine(new RdfRepository()).parseGraphQuery(theQuery).getQuery();
            }
            catch (MalformedQueryException mqe) {
                throw pe;
            }
        }

        if (aQuery != null) {
            return getQueryRenderer(theTarget).render(aQuery);
        }
        else {
            throw new UnsupportedOperationException();
        }

    }

    public static String getQueryString(Value theValue) {
        String aStr = theValue.toString();

        if (theValue instanceof URI)
            aStr = "<"+theValue.toString()+">";
        else if (theValue instanceof BNode)
            aStr = "_:"+((BNode)theValue).getID();
        else if (theValue instanceof Literal) {
            Literal aLit = (Literal)theValue;
            aStr = "\"" + escape(aLit.getLabel()) + "\"" + (aLit.getLanguage() != null ? "@"+aLit.getLanguage() : "") ;
            if (aLit.getDatatype() != null)
                aStr += "^^<"+aLit.getDatatype().toString()+">";
        }

        return aStr;
    }

    public static String escape(String theString) {
        theString = theString.replaceAll("\"", "\\\\\"");

        return theString;
    }

    public static int indexOfVariable(QueryResultsTable theResults, String theVarName) {
        for (int i = 0; i < theResults.getColumnCount(); i++) {
            if (theResults.getColumnName(i).equals(theVarName))
                return i;
        }
        return -1;
    }
}

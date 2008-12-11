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
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 16, 2007 11:45:42 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class SesameQueryUtils {
    public static final String SPARQL = "sparql";

    public static SelectQuery selectQuery(String theQuery) throws MalformedQueryException {
        return (SelectQuery) new SerqlEngine(new RdfRepository()).parseTableQuery(theQuery).getQuery();
    }

    public static ConstructQuery constructQuery(String theQuery) throws MalformedQueryException {
        return (ConstructQuery) new SerqlEngine(new RdfRepository()).parseGraphQuery(theQuery).getQuery();
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

//    /**
//     * Converts Query objects from one language to another.
//     * @param theQuery the input query
//     * @param theTarget the query language to convert to
//     * @return the equivalent query represented in a different query language
//     * @throws RuntimeException throw if there is an error while converting the query
//     */
//    public static String convertQuery(Query theQuery, String theTarget) throws RuntimeException {
//        // only supporting serql to sparql right now
//        // but evren has some code in the performance module that goes from sparql to serql (and iTQL)
//        // which should probably be integrated at some point
//        if (!theTarget.equals(SPARQL)) {
//            throw new UnsupportedOperationException("Can only convert SeRQL -> SPARQL");
//        }
//
//        // I will make no claims of the completeness or accuracy of this conversion code.  it will be good
//        // enough to hold down the fort and do what we need to do for the time being, but i'm sure
//        // there are probably some loopholes in the conversion, bnodes in sparql for one.
//
//        if (theQuery instanceof TableQuery) {
//            // select queries are the most common, so lets try this first, see if we can parse in the incoming query as such
//            TableQuery aTableQuery = (TableQuery) theQuery;
//
//            return getQueryRenderer(SPARQL).render(aTableQuery.getQuery());
//        }
//        else if (theQuery instanceof GraphQuery) {
//            // ok, so it failed to parse as a table/select query, lets try a graph/construct query.  if this fails
//            // then its something we cant convert...
//            GraphQuery aGraphQuery = (GraphQuery) theQuery;
//
//            return QueryUtils.getQueryRenderer("sparql").render(aGraphQuery.getQuery());
//        }
//        else {
//            throw new UnsupportedOperationException("Unknown query type");
//        }
//    }

    public static String getQueryString(Value theValue) {
        String aStr = theValue.toString();

        if (theValue instanceof URI)
            aStr = "<"+theValue.toString()+">";
        else if (theValue instanceof BNode)
            aStr = "_:"+((BNode)theValue).getID();
        else if (theValue instanceof Literal) {
            Literal aLit = (Literal)theValue;
            aStr = "\"" + escape(aLit.getLabel()) + (aLit.getLanguage() != null ? "@"+aLit.getLanguage() : "") +"\"";
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

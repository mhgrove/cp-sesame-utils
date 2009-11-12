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

import com.clarkparsia.utils.Function;

/**
 * Title: A decorated Sesame graph object which provides utility functions for many common operations not present in
 * Sesame's API.<br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jun 2, 2009 2:46:04 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ExtendedGraph extends DecoratableGraph implements Graph {

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
	 * Create a new ExtendedGraph
	 * @param theStatements the statements which make up the initial contents of the Graph
	 */
	public ExtendedGraph(Iterable<Statement> theStatements) {
		this();

		add(new StmtIterator(theStatements));
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
		Iterator<Value> aIter = getValues(theSubj, thePred);

		if (aIter.hasNext()) {
			return aIter.next();
		}
		else {
			return null;
		}
	}

	public Literal getLiteral(Resource theRes, URI theProp) {
		return (Literal) getValue(theRes, theProp);
	}

	public Iterator<Value> getValues(Resource theSubj, URI thePred) {
        StatementIterator sIter = getStatements(theSubj, thePred, null);

		return CollectionUtil.transform(new StatementIteratorAsIterator(sIter), new Function<Statement, Value>() {
			public Value apply(final Statement theIn) {
				return theIn.getObject();
			}
		}).iterator();
	}

	public boolean hasProperty(Resource theRes, URI theProp) {
		return getValues(theRes, theProp).hasNext();
	}

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
}

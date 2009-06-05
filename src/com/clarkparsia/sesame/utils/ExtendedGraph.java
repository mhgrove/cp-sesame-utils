package com.clarkparsia.sesame.utils;

import org.openrdf.model.Graph;
import org.openrdf.model.Value;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.vocabulary.XmlSchema;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.rio.ParseException;

import java.util.Iterator;
import java.util.Set;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.clarkparsia.utils.io.IOUtil;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jun 2, 2009 2:46:04 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ExtendedGraph extends DecoratableGraph {

	public ExtendedGraph() {
		super(new GraphImpl());
	}

	public ExtendedGraph(Graph theGraph) {
		super(theGraph);
	}

	public Value getValue(Resource theRes, URI theProp) {
		return SesameUtils.getValue(this, theRes, theProp);
	}

	public Literal getLiteral(Resource theRes, URI theProp) {
		return (Literal) SesameUtils.getValue(this, theRes, theProp);
	}

	public Iterator<Value> getValues(Resource theRes, URI theProp) {
		return SesameUtils.getValues(this, theRes, theProp);
	}

	public boolean hasProperty(Resource theRes, URI theProp) {
		return SesameUtils.getValues(this, theRes, theProp).hasNext();
	}

	public Value label(Resource theRes) {
		return SesameUtils.getValue(this, theRes, URIImpl.RDFS_LABEL);
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
		return SesameUtils.numStatements(this);
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

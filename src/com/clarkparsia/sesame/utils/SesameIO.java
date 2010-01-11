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

import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.admin.DummyAdminListener;
import org.openrdf.sesame.admin.StdOutAdminListener;
import org.openrdf.sesame.admin.AdminMsgCollector;
import org.openrdf.sesame.admin.AdminMsg;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.Sesame;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.Parser;
import org.openrdf.rio.ParseException;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RdfXmlParser;
import org.openrdf.rio.rdfxml.RdfXmlWriter;
import org.openrdf.util.io.IOUtil;

import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.File;

import com.clarkparsia.sesame.utils.query.Binding;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;
import com.clarkparsia.sesame.utils.query.SesameQueryUtils;
import com.clarkparsia.sesame.repository.ExtendedSesameRepository;

/**
 * <p>Collection of utility methods for dealing with IO using the Sesame API</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class SesameIO {
	public static ExtendedSesameRepository readRepository(URL theFile, RDFFormat theFormat) throws IOException, ParseException {
		InputStream aStream = theFile.openStream();

		try {
			return readRepository(aStream, theFormat);
		}
		finally {
			aStream.close();
		}
	}

	public static ExtendedSesameRepository readRepository(InputStream theStream, RDFFormat theFormat) throws IOException, ParseException {
		InputStreamReader aReader = new InputStreamReader(theStream);

		try {
			return readRepository(aReader, theFormat);
		}
		finally {
			aReader.close();
		}
	}

	public static ExtendedSesameRepository readRepository(Reader theReader, RDFFormat theFormat) throws IOException, ParseException {
		SesameRepository aRepo = SesameUtils.createInMemSource();

		try {
			AdminMsgCollector aCollector = new AdminMsgCollector();
			aRepo.addData(theReader, "", theFormat, true, aCollector);
			if (aCollector.getErrors().size() > 0) {
				StringBuffer aBuffer = new StringBuffer();
				for (Object aObj : aCollector.getErrors()) {
					AdminMsg aMsg = (AdminMsg) aObj;
					aBuffer.append("[").append(aMsg.getLineNo()).append(":").append(aMsg.getColumnNo()).append("] ").append(aMsg.getMessage()).append("\n");
				}

				throw new IOException(aBuffer.toString());
			}
		}
		catch (AccessDeniedException e) {
			throw new IOException(e.getMessage());
		}

		return new ExtendedSesameRepository(aRepo);
	}

	public static ExtendedGraph readGraph(URL theFile, RDFFormat theFormat) throws IOException, ParseException {
		InputStream aStream = theFile.openStream();

		try {
			return readGraph(aStream, theFormat);
		}
		finally {
			aStream.close();
		}
	}

	public static ExtendedGraph readGraph(InputStream theStream, RDFFormat theFormat) throws IOException, ParseException {
		InputStreamReader aReader = new InputStreamReader(theStream);

		try {
			return readGraph(aReader, theFormat);
		}
		finally {
			aReader.close();
		}
	}

	public static ExtendedGraph readGraph(Reader theReader, RDFFormat theFormat) throws IOException, ParseException {
        Graph aGraph = new GraphImpl();

        Parser aRDFParser;

		if (theFormat == RDFFormat.TURTLE) {
			aRDFParser = new TurtleParser(aGraph.getValueFactory());
		}
		else if (theFormat == RDFFormat.NTRIPLES) {
			aRDFParser = new NTriplesParser(aGraph.getValueFactory());
		}
		else if (theFormat == RDFFormat.RDFXML) {
			aRDFParser = new RdfXmlParser(aGraph.getValueFactory());
		}
		else {
			throw new IllegalArgumentException("Unknown format: " + theFormat);
		}

        StatementHandler aHandler = new BuildGraphStatementHandler(aGraph);
        aRDFParser.setStatementHandler(aHandler);

		try {
			aRDFParser.parse(theReader, "http://example.org/");
		}
		catch (StatementHandlerException e) {
			throw new IOException(e.getMessage());
		}

		return new ExtendedGraph(aGraph);
	}

	public static void writeGraph(Graph theGraph, Writer theWriter, RDFFormat theFormat) throws IOException {
		RdfDocumentWriter aRdfWriter;

		if (theFormat == RDFFormat.TURTLE) {
			aRdfWriter = new TurtleWriter(theWriter);
		}
		else if (theFormat == RDFFormat.NTRIPLES) {
			aRdfWriter = new NTriplesWriter(theWriter);
		}
		else if (theFormat == RDFFormat.RDFXML) {
			aRdfWriter = new RdfXmlWriter(theWriter);
		}
		else {
			throw new IllegalArgumentException("Unknown format: " + theFormat);
		}

		writeGraph(theGraph, aRdfWriter);
	}

	public static void writeGraph(SesameRepository theRepo, Writer theWriter, RDFFormat theFormat) throws IOException {
		try {
			IOUtil.transfer(new InputStreamReader(theRepo.extractRDF(theFormat, true, true, true, true)),
							theWriter);
		}
		catch (AccessDeniedException e) {
			throw new IOException(e.getMessage());
		}
	}

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
}

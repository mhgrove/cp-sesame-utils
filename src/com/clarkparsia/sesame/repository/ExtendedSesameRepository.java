package com.clarkparsia.sesame.repository;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.admin.StdOutAdminListener;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.util.io.IOUtil;
import org.openrdf.rio.ParseException;

import com.clarkparsia.sesame.utils.SesameUtils;
import com.clarkparsia.sesame.utils.SesameIO;
import com.clarkparsia.sesame.utils.ExtendedGraph;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;
import com.clarkparsia.sesame.utils.query.SesameQuery;
import com.clarkparsia.utils.CollectionUtil;

import java.util.Iterator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.InputStream;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Apr 23, 2009 10:47:17 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ExtendedSesameRepository extends BaseSesameRepository implements SesameRepository {
	public ExtendedSesameRepository(SesameRepository theRepo) {
		super(theRepo);

		// TODO: maybe move all the stuff we use from sesame utils into here?  this class should
		// make a lot of that junk moot
	}

	public Iterable<Resource> getSubjects(URI thePredicate, Value theObject) {
		return CollectionUtil.list(SesameUtils.getSubjects(this, thePredicate, theObject));
	}

	public Iterable<Value> getValues(Resource theSubj, URI thePred) {
		return CollectionUtil.list(SesameUtils.getValues(this, theSubj, thePred));
	}

	public IterableQueryResultsTable performSelectQuery(QueryLanguage theLang, String theQuery) throws AccessDeniedException,
																									   IOException,
																									   MalformedQueryException,
																									   QueryEvaluationException {
		return IterableQueryResultsTable.iterable(performTableQuery(theLang, theQuery));
	}

	public boolean hasStatement(Resource theSubj, URI thePred, Value theObj) {
		return SesameUtils.hasStatement(this, theSubj, thePred, theObj);
	}

	public IterableQueryResultsTable performSelectQuery(SesameQuery theQuery) throws AccessDeniedException,
																					 IOException,
																					 MalformedQueryException,
																					 QueryEvaluationException {
		return performSelectQuery(theQuery.getLanguage(), theQuery.getQueryString());
	}

	public ExtendedGraph performConstructQuery(SesameQuery theQuery) throws AccessDeniedException, IOException,
																			MalformedQueryException,
																			QueryEvaluationException {
		return new ExtendedGraph(performGraphQuery(theQuery.getLanguage(), theQuery.getQueryString()));
	}

	public Iterable<Statement> getStatements() {
		return getStatements(null, null, null);
	}

	public Iterable<Statement> getStatements(Resource theSubj, URI thePred, Value theObj) {
		return SesameUtils.getStatements(this, theSubj, thePred, theObj);
	}

	public ExtendedGraph describe(URI theURI) {
		// TODO: need a null check for theURI
		// TODO: make this align closer to a SPARQL describe
		return new ExtendedGraph(SesameUtils.getStatements(this, theURI, null, null).asGraph());
	}

	public void write(OutputStream theStream, RDFFormat theFormat) throws IOException {
		write(new OutputStreamWriter(theStream), theFormat);
	}

	public void write(Writer theStream, RDFFormat theFormat) throws IOException {
		try {
			IOUtil.transfer(new InputStreamReader(extractRDF(theFormat, true, true, true, true)), theStream);

			theStream.flush();
		}
		catch (AccessDeniedException e) {
			throw new IOException(e.getMessage());
		}
	}

	public void add(Statement... theStatement) throws IOException {
		try {
			addGraph(SesameUtils.asGraph(theStatement));
		}
		catch (AccessDeniedException e) {
			throw new IOException(e.getMessage());
		}
	}

	public void read(InputStream theStream, RDFFormat theFormat) throws IOException, ParseException {
		try {
			addData(SesameIO.readRepository(theStream, theFormat), new StdOutAdminListener());
		}
		catch (AccessDeniedException e) {
			throw new RuntimeException(e);
		}
	}

	public Value getValue(Resource theSubj, URI thePred) {
		return SesameUtils.getValue(this, theSubj, thePred);
	}

	public Literal getLiteral(Resource theSubj, URI thePred) {
		return (Literal) getValue(theSubj, thePred);
	}

	// TODO: add an ask method
}

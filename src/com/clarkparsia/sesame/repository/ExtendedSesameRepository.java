package com.clarkparsia.sesame.repository;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Graph;

import com.clarkparsia.sesame.utils.SesameUtils;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;
import com.clarkparsia.sesame.utils.query.SesameQuery;
import com.clarkparsia.utils.CollectionUtil;

import java.util.Iterator;
import java.io.IOException;

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

	public Iterable<Statement> getStatements() {
		return getStatements(null, null, null);
	}

	public Iterable<Statement> getStatements(Resource theSubj, URI thePred, Value theObj) {
		return SesameUtils.getStatements(this, theSubj, thePred, theObj);
	}

	public Graph describe(URI theURI) {
		// TODO: need a null check for theURI
		// TODO: make this align closer to a SPARQL describe
		return SesameUtils.getStatements(this, theURI, null, null).asGraph();
	}

	// TODO: add an ask method
}

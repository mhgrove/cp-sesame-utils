package com.clarkparsia.sesame.repository;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;

import com.clarkparsia.sesame.utils.SesameUtils;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;

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

	public Iterator<Resource> getSubjects(URI thePredicate, Value theObject) {
		return SesameUtils.getSubjects(this, thePredicate, theObject);
	}

	public Iterator<Value> getValues(Resource theSubj, URI thePred) {
		return SesameUtils.getValues(this, theSubj, thePred);
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
}

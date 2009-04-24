package com.clarkparsia.sesame.repository.convert;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;

import java.io.IOException;

import com.clarkparsia.sesame.utils.query.SesameQueryUtils;
import com.clarkparsia.sesame.repository.BaseSesameRepository;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 23, 2008 9:37:58 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class AutoUpdatingSesameRepository extends BaseSesameRepository implements SesameRepository {

    public AutoUpdatingSesameRepository(SesameRepository theRepo) {
        super(theRepo);
    }

    public boolean hasUpdates() throws AccessDeniedException, IOException, MalformedQueryException, QueryEvaluationException {
        String aQuery = "select aValue from {s} <" + ConversionBaseSail.UPDATE_PREDICATE.getURI() + "> {aValue}";

        QueryResultsTable aResults = performTableQuery(QueryLanguage.SERQL, aQuery);

        return aResults.getRowCount() != 0 && Boolean.valueOf(((Literal) aResults.getValue(0, 0)).getLabel());
    }

    public Graph performConstructQuery(String theQuery) throws AccessDeniedException, IOException, MalformedQueryException, QueryEvaluationException {
        return performGraphQuery(QueryLanguage.SERQL, SesameQueryUtils.convertQuery(theQuery, "", ""));
    }
}
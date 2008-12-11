package com.clarkparsia.sesame.utils;

import org.openrdf.rio.StatementHandler;
import org.openrdf.sesame.sail.RdfRepository;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Nov 1, 2006 12:53:21 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class BuildRepositoryStatementHandler implements StatementHandler {
    private RdfRepository mRepository;

    public BuildRepositoryStatementHandler(RdfRepository theRepository) {
        mRepository = theRepository;
        //mRepository.startTransaction();
    }

    public void handleStatement(Resource theSubject, URI thePredicate, Value theObject) {
        try {
            mRepository.addStatement(theSubject, thePredicate, theObject);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
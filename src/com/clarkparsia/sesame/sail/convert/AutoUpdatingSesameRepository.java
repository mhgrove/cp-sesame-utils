package com.clarkparsia.sesame.sail.convert;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.TableQueryResultListener;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.GraphQueryResultListener;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.InputStream;
import java.net.URL;

import com.clarkparsia.sesame.utils.query.SesameQueryUtils;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 23, 2008 9:37:58 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class AutoUpdatingSesameRepository implements SesameRepository {

    private SesameRepository mRepo;

    public AutoUpdatingSesameRepository(SesameRepository theRepo) {
        mRepo = theRepo;
    }

    public boolean hasUpdates() throws AccessDeniedException, IOException, MalformedQueryException, QueryEvaluationException {
        String aQuery = "select aValue from {s} <" + ConversionBaseSail.UPDATE_PREDICATE.getURI() + "> {aValue}";

        QueryResultsTable aResults = performTableQuery(QueryLanguage.SERQL, aQuery);

        return aResults.getRowCount() != 0 && Boolean.valueOf(((Literal) aResults.getValue(0, 0)).getLabel());
    }

    public Graph performConstructQuery(String theQuery) throws AccessDeniedException, IOException, MalformedQueryException, QueryEvaluationException {
        return performGraphQuery(QueryLanguage.SERQL, SesameQueryUtils.convertQuery(theQuery, "", ""));
    }

    public void performTableQuery(QueryLanguage theQueryLanguage, String theQuery, TableQueryResultListener theTableQueryResultListener) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        mRepo.performTableQuery(theQueryLanguage, theQuery, theTableQueryResultListener);
    }

    public QueryResultsTable performTableQuery(QueryLanguage theQueryLanguage, String theQuery) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        return mRepo.performTableQuery(theQueryLanguage, theQuery);
    }

    public void performGraphQuery(QueryLanguage theQueryLanguage, String theQuery, GraphQueryResultListener theGraphQueryResultListener) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        mRepo.performGraphQuery(theQueryLanguage, theQuery, theGraphQueryResultListener);
    }

    public Graph performGraphQuery(QueryLanguage theQueryLanguage, String theQuery) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        return mRepo.performGraphQuery(theQueryLanguage,  theQuery);
    }

    public void addData(URL theURL, String s, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theURL, s, theRDFFormat, theVerify, theAdminListener);
    }

    public void addData(File theFile, String s, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws FileNotFoundException, IOException, AccessDeniedException {
        mRepo.addData(theFile, s, theRDFFormat, theVerify, theAdminListener);
    }

    public void addData(String s, String s1, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(s, s1, theRDFFormat, theVerify, theAdminListener);
    }

    public void addData(SesameRepository theSesameRepository, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theSesameRepository, theAdminListener);
    }

    public void addData(Reader theReader, String s, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theReader, s, theRDFFormat, theVerify, theAdminListener);
    }

    public void addData(InputStream theInputStream, String s, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theInputStream, s, theRDFFormat, theVerify, theAdminListener);
    }

    public void addGraph(Graph theGraph) throws IOException, AccessDeniedException {
        mRepo.addGraph(theGraph);
    }

    public void addGraph(Graph theGraph, boolean b) throws IOException, AccessDeniedException {
        mRepo.addGraph(theGraph, b);
    }

    public void addGraph(QueryLanguage theQueryLanguage, String s) throws IOException, AccessDeniedException {
        mRepo.addGraph(theQueryLanguage, s);
    }

    public void addGraph(QueryLanguage theQueryLanguage, String s, boolean b) throws IOException, AccessDeniedException {
        mRepo.addGraph(theQueryLanguage, s, b);
    }

    public void removeGraph(Graph theGraph) throws IOException, AccessDeniedException {
        mRepo.removeGraph(theGraph);
    }

    public void removeGraph(QueryLanguage theQueryLanguage, String s) throws IOException, AccessDeniedException {
        mRepo.removeGraph(theQueryLanguage, s);
    }

    public InputStream extractRDF(RDFFormat theRDFFormat, boolean b, boolean b1, boolean b2, boolean b3) throws IOException, AccessDeniedException {
        return mRepo.extractRDF(theRDFFormat, b, b1, b2, b3);
    }

    public void removeStatements(Resource theResource, URI theURI, Value theValue, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.removeStatements(theResource, theURI, theValue, theAdminListener);
    }

    public void clear(AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.clear(theAdminListener);
    }

    public String getRepositoryId() {
        return mRepo.getRepositoryId();
    }
}
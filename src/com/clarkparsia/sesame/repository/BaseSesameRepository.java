package com.clarkparsia.sesame.repository;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.query.TableQueryResultListener;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.GraphQueryResultListener;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.InputStream;
import java.net.URL;

import com.clarkparsia.sesame.utils.query.SesameQueryUtils;

/**
 * Title: BaseSesameRepository<br/>
 * Description: Default decoratable implementation of a SesameRepository<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 22, 2008 7:14:56 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class BaseSesameRepository implements SesameRepository {

	/**
	 * The base repository
	 */
    private SesameRepository mRepo;

	/**
	 * Create a new BaseSesameRepository
	 * @param theRepo the repo to decorate
	 */
    public BaseSesameRepository(SesameRepository theRepo) {
        mRepo = theRepo;
    }

	/**
	 * Returns the base sesame repository
	 * @return the base repository
	 */
	protected SesameRepository getBaseRepository() {
		return mRepo;
	}


    ///////////////////////////////////////////////////////////////
    /////
    /////  SesameRepository Interface implementation
    /////
    ///////////////////////////////////////////////////////////////

    /**
     * @inheritDoc
     */
    public void performTableQuery(QueryLanguage theQueryLanguage, String theQuery, TableQueryResultListener theTableQueryResultListener) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        mRepo.performTableQuery(theQueryLanguage, theQuery, theTableQueryResultListener);
    }

    /**
     * @inheritDoc
     */
    public QueryResultsTable performTableQuery(QueryLanguage theQueryLanguage, String theQuery) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        return mRepo.performTableQuery(theQueryLanguage, theQuery);
    }

    /**
     * @inheritDoc
     */
    public void performGraphQuery(QueryLanguage theQueryLanguage, String theQuery, GraphQueryResultListener theGraphQueryResultListener) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        mRepo.performGraphQuery(theQueryLanguage, theQuery, theGraphQueryResultListener);
    }

    /**
     * @inheritDoc
     */
    public Graph performGraphQuery(QueryLanguage theQueryLanguage, String theQuery) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        return mRepo.performGraphQuery(theQueryLanguage, theQuery);
    }

    /**
     * @inheritDoc
     */
    public void addData(URL theURL, String theBase, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theURL, theBase, theRDFFormat, theVerify, theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public void addData(File theFile, String theBase, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws FileNotFoundException, IOException, AccessDeniedException {
        mRepo.addData(theFile, theBase, theRDFFormat, theVerify, theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public void addData(String theData, String theBase, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theData, theBase, theRDFFormat, theVerify, theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public void addData(SesameRepository theSesameRepository, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theSesameRepository, theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public void addData(Reader theReader, String theBase, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theReader, theBase, theRDFFormat, theVerify, theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public void addData(InputStream theInputStream, String theBase, RDFFormat theRDFFormat, boolean theVerify, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.addData(theInputStream, theBase, theRDFFormat, theVerify, theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public void addGraph(Graph theGraph) throws IOException, AccessDeniedException {
        mRepo.addGraph(theGraph);
    }

    /**
     * @inheritDoc
     */
    public void addGraph(Graph theGraph, boolean theJoinBlank) throws IOException, AccessDeniedException {
        mRepo.addGraph(theGraph, theJoinBlank);
    }

    /**
     * @inheritDoc
     */
    public void addGraph(QueryLanguage theQueryLanguage, String theQuery) throws IOException, AccessDeniedException {
        mRepo.addGraph(theQueryLanguage, theQuery);
    }

    /**
     * @inheritDoc
     */
    public void addGraph(QueryLanguage theQueryLanguage, String theQuery, boolean theJoinBlank) throws IOException, AccessDeniedException {
        mRepo.addGraph(theQueryLanguage, theQuery, theJoinBlank);
    }

    /**
     * @inheritDoc
     */
    public void removeGraph(Graph theGraph) throws IOException, AccessDeniedException {
        mRepo.removeGraph(theGraph);
    }

    /**
     * @inheritDoc
     */
    public void removeGraph(QueryLanguage theQueryLanguage, String theQuery) throws IOException, AccessDeniedException {
        mRepo.removeGraph(theQueryLanguage, theQuery);
    }

    /**
     * @inheritDoc
     */
    public InputStream extractRDF(RDFFormat theRDFFormat, boolean theOntology, boolean theInstances, boolean theExplicitOnly, boolean thePrettyOut) throws IOException, AccessDeniedException {
        return mRepo.extractRDF(theRDFFormat, theOntology, theInstances, theExplicitOnly, thePrettyOut);
    }

    /**
     * @inheritDoc
     */
    public void removeStatements(Resource theResource, URI theURI, Value theValue, AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.removeStatements(theResource, theURI, theValue, theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public void clear(AdminListener theAdminListener) throws IOException, AccessDeniedException {
        mRepo.clear(theAdminListener);
    }

    /**
     * @inheritDoc
     */
    public String getRepositoryId() {
        return mRepo.getRepositoryId();
    }}

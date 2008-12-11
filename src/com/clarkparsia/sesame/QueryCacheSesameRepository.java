package com.clarkparsia.sesame;

import org.openrdf.sesame.repository.SesameRepository;

import org.openrdf.sesame.admin.AdminListener;

import org.openrdf.sesame.config.AccessDeniedException;

import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.constants.RDFFormat;

import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.GraphQueryResultListener;
import org.openrdf.sesame.query.TableQueryResultListener;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import org.openrdf.model.Value;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Graph;

import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;

import java.net.URL;

import java.util.ArrayList;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 *
 * @author Michael Grove
 * @version 1.0
 */
public class QueryCacheSesameRepository implements SesameRepository
{
    private SesameRepository mRepository;
    private ArrayList mQueryList;
    private int mListSize;

    public QueryCacheSesameRepository(SesameRepository theRepo) {
        this(theRepo, 100);
    }

    public QueryCacheSesameRepository(SesameRepository theRepo, int theSize) {
        mRepository = theRepo;
        mQueryList = new ArrayList();
        mListSize = theSize;
    }

    public String getRepositoryId() {
        return mRepository.getRepositoryId();
    }

    public void clear(AdminListener theListener) throws IOException, AccessDeniedException {
        mRepository.clear(theListener);
    }

    public void removeStatements(Resource theSubj, URI thePred, Value theObj, AdminListener theListener) throws IOException, AccessDeniedException {
        mRepository.removeStatements(theSubj, thePred, theObj, theListener);
    }

    public InputStream extractRDF(RDFFormat theFormat, boolean theOntology, boolean theInstances, boolean theExplicitOnly, boolean theNiceOutput) throws IOException, AccessDeniedException {
        return mRepository.extractRDF(theFormat, theOntology, theInstances, theExplicitOnly, theNiceOutput);
    }

    public void removeGraph(Graph theGraph) throws IOException, AccessDeniedException {
        mRepository.removeGraph(theGraph);
    }

    public void removeGraph(QueryLanguage theLang, String theQuery) throws IOException, AccessDeniedException {
        mRepository.removeGraph(theLang, theQuery);
    }

    public Graph performGraphQuery(QueryLanguage theLang, String theQuery) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        logQuery(theQuery);

        return mRepository.performGraphQuery(theLang, theQuery);
    }

    public void performGraphQuery(QueryLanguage theLang, String theQuery, GraphQueryResultListener theListener) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        logQuery(theQuery);

        mRepository.performGraphQuery(theLang, theQuery, theListener);
    }

    public QueryResultsTable performTableQuery(QueryLanguage theLang, String theQuery) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        logQuery(theQuery);

        return mRepository.performTableQuery(theLang, theQuery);
    }

    public void performTableQuery(QueryLanguage theLang, String theQuery, TableQueryResultListener theListener) throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException {
        logQuery(theQuery);

        mRepository.performTableQuery(theLang, theQuery, theListener);
    }

    public void addData(File theDataFile, String theBaseURI, RDFFormat theFormat, boolean theVerify, AdminListener theListener) throws FileNotFoundException, IOException, AccessDeniedException {
        mRepository.addData(theDataFile, theBaseURI, theFormat, theVerify, theListener);
    }

    public void addData(InputStream theData, String theBaseURI, RDFFormat theFormat, boolean theVerify, AdminListener theListener) throws IOException, AccessDeniedException{
        mRepository.addData(theData, theBaseURI, theFormat, theVerify, theListener);
    }

    public void addData(Reader theData, String theBaseURI, RDFFormat theFormat, boolean theVerify, AdminListener theListener) throws IOException, AccessDeniedException {
        mRepository.addData(theData, theBaseURI, theFormat, theVerify, theListener);
    }

    public void addData(SesameRepository theRepo, AdminListener theListener) throws IOException, AccessDeniedException {
        mRepository.addData(theRepo, theListener);
    }

    public void addData(String theData, String theBaseURI, RDFFormat theFormat, boolean theVerifyData, AdminListener theListener) throws IOException, AccessDeniedException {
        mRepository.addData(theData, theBaseURI, theFormat, theVerifyData, theListener);
    }

    public void addData(URL theDataURL, String theBaseURI, RDFFormat theFormat, boolean theVerify, AdminListener theListener) throws IOException, AccessDeniedException {
        mRepository.addData(theDataURL, theBaseURI, theFormat, theVerify, theListener);
    }

    public void addGraph(Graph theGraph) throws IOException, AccessDeniedException {
        mRepository.addGraph(theGraph);
    }

    public void addGraph(Graph theGraph, boolean theJoin) throws IOException, AccessDeniedException {
        mRepository.addGraph(theGraph, theJoin);
    }

    public void addGraph(QueryLanguage theLang, String theQuery) throws IOException, AccessDeniedException {
        mRepository.addGraph(theLang, theQuery);
    }

    public void addGraph(QueryLanguage theLang, String theQuery, boolean theJoin) throws IOException, AccessDeniedException {
        mRepository.addGraph(theLang, theQuery, theJoin);
    }

    private void logQuery(String theQuery) {
        mQueryList.add(0, theQuery);

        if (mQueryList.size() > mListSize)
            mQueryList.remove(mQueryList.size() - 1);
    }

    public ArrayList getQueries() {
        return new ArrayList(mQueryList);
    }
}
package com.clarkparsia.sesame.utils;

import org.openrdf.rio.StatementHandler;

import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;

import org.openrdf.model.impl.GraphImpl;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 * @version 1.0
 */
public class BuildGraphStatementHandler implements StatementHandler
{
    private Graph mGraph;

    public BuildGraphStatementHandler() {
        mGraph = new GraphImpl();
    }

    public BuildGraphStatementHandler(Graph theGraph) {
        mGraph = theGraph;
    }

    public void handleStatement(Resource theSubject, URI thePredicate, Value theObject) {
        try {
            mGraph.add(theSubject, thePredicate, theObject);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Graph getGraph() {
        return mGraph;
    }
}
package com.clarkparsia.sesame.utils;

import org.openrdf.rio.StatementHandler;

import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;

import org.openrdf.model.impl.GraphImpl;

/**
 * <p>Implementation of the Sesame StatementHandler interface which will add the statements to a Graph</p>
 *
 * @author Michael Grove
 * @since 1.0
 */

public class BuildGraphStatementHandler implements StatementHandler {
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
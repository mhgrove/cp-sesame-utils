package com.clarkparsia.sesame.utils.query.optimize;

import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.vocabulary.RDFS;
import org.openrdf.vocabulary.OWL;
import org.openrdf.vocabulary.RDF;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Jan 29, 2008 12:22:41 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public abstract class AbstractPatternOptimizer implements GraphPatternOptimizer {
    private GraphPatternOptimizer mOptimizer;

    public AbstractPatternOptimizer() {
        this(null);
    }

    public AbstractPatternOptimizer(GraphPatternOptimizer theOpt) {
        mOptimizer = theOpt;
    }

    public GraphPattern optimize(GraphPattern thePattern) throws Exception {
        GraphPattern aPattern = thePattern;

        if (mOptimizer != null) {
            aPattern = mOptimizer.optimize(aPattern);
        }

        return performOptimization(aPattern);
    }

    protected abstract GraphPattern performOptimization(GraphPattern thePattern) throws Exception;

    protected boolean isSchemaTriple(TriplePattern thePattern) {
        return thePattern.getPredicateVar().getValue() != null &&
               (thePattern.getPredicateVar().getValue().toString().startsWith(RDF.NAMESPACE) ||
                thePattern.getPredicateVar().getValue().toString().startsWith(RDFS.NAMESPACE) ||
                thePattern.getPredicateVar().getValue().toString().startsWith(OWL.NAMESPACE));
    }


    protected boolean isTypeTriple(TriplePattern thePattern) {
        return thePattern.getPredicateVar().getValue() != null &&
               thePattern.getPredicateVar().getValue().equals(URIImpl.RDF_TYPE);
    }
}

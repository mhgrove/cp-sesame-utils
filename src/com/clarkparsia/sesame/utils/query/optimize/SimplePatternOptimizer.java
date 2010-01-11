package com.clarkparsia.sesame.utils.query.optimize;

import java.util.List;
import java.util.ArrayList;

import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.TriplePattern;

/**
 * <p>Very simple query optimizer that just pushes optionals and type triples to the end of the set of patterns.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class SimplePatternOptimizer implements GraphPatternOptimizer {

    public GraphPattern optimize(GraphPattern thePattern) throws Exception {
        GraphPattern aOptimizedPattern = new GraphPattern();

        List aTypeAtoms = new ArrayList();
        List aAtomList = new ArrayList();

        List aExpressions = thePattern.getPathExpressions();
        for (int aExprIndex = 0; aExprIndex < aExpressions.size(); aExprIndex++) {
            PathExpression aPathExpr = (PathExpression) aExpressions.get(aExprIndex);

            if (aPathExpr instanceof GraphPattern) {
                // i believe sesame uses graph patterns for all atoms that include an rdf/rdfs and probably owl construct
                // in the predicate position, we're only using rdf:type triples, so each graph pattern we see *should*
                // be a type triple
                aTypeAtoms.add(aPathExpr);
            }
            else if (aPathExpr instanceof TriplePattern) {
                TriplePattern aTP = (TriplePattern) aPathExpr;

                boolean aInserted = false;
                for (int aAtomListIndex = 0; aAtomListIndex < aAtomList.size(); aAtomListIndex++) {
                    PathExpression aPE = (PathExpression) aAtomList.get(aAtomListIndex);
                    if (aPE instanceof TriplePattern) {
                        TriplePattern aAtom = (TriplePattern) aPE;
                        if ((aAtom.getSubjectVar().getValue() == null && aAtom.getSubjectVar().equals(aTP.getSubjectVar())) ||
                            (aAtom.getSubjectVar().getValue() == null  && aAtom.getSubjectVar().equals(aTP.getObjectVar())) ||
                            (aAtom.getObjectVar().getValue() == null  && aAtom.getObjectVar().equals(aTP.getSubjectVar())) ||
                            (aAtom.getObjectVar().getValue() == null  && aAtom.getObjectVar().equals(aTP.getObjectVar()))) {
                            // we want to group the patterns together by shared variables in the atoms
                            aAtomList.add(aAtomListIndex, aTP);
                            aInserted = true;
                            break;
                        }
                    }
                }

                if (!aInserted) {
                    aAtomList.add(aTP);
                }
            }
        }

        aAtomList.addAll(aTypeAtoms);

        aOptimizedPattern.setPathExpressions(aAtomList);

        aOptimizedPattern.setOptionals(thePattern.getOptionals());

        aOptimizedPattern.setConstraints(thePattern.getRootConstraint());

        return aOptimizedPattern;
    }
}

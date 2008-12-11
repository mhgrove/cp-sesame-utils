package com.clarkparsia.sesame.utils.query.optimize;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;

import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.BooleanExpr;
import org.openrdf.sesame.sail.query.ValueCompare;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;
import org.openrdf.vocabulary.OWL;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Jan 29, 2008 12:21:37 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class WeightedPatternOptimizer extends AbstractPatternOptimizer {
    public WeightedPatternOptimizer() {
        super();
    }

    public WeightedPatternOptimizer(GraphPatternOptimizer theOpt) {
        super(theOpt);
    }

    protected GraphPattern performOptimization(GraphPattern thePattern) throws Exception {
        GraphPattern aOptimizedPattern = new GraphPattern();

        List aTypeAtoms = new ArrayList();
        List aAtomList = new ArrayList();
        List aGraphAtoms = new ArrayList();

        List aBoundVars = new ArrayList();

        List aConstraintList = thePattern.getConjunctiveConstraints();
        for (int aConstraintIndex = 0; aConstraintIndex < aConstraintList.size(); aConstraintIndex++) {
            BooleanExpr aExpr = (BooleanExpr) aConstraintList.get(aConstraintIndex);

            if (aExpr instanceof ValueCompare) {
                ValueCompare aValCompare = (ValueCompare) aExpr;

                if (aValCompare.getOperator() == ValueCompare.EQ) {
                    // ie. 5 == ?a, a is being bound to something at some point, so that makes any atom using
                    // that variable attractive because its going to be easier to calcuate since there's some
                    // bound on the values it could have

                    if (aValCompare.getLeftArg().getValue() != null && aValCompare.getRightArg().getValue() == null) {
                        aBoundVars.add(aValCompare.getRightArg());
                    }
                    else if (aValCompare.getLeftArg().getValue() == null && aValCompare.getRightArg().getValue() != null) {
                        aBoundVars.add(aValCompare.getLeftArg());
                    }
                }
            }
        }

        Collection aVisitedSubjVars = new HashSet();
        Collection aVisitedObjVars = new HashSet();

        List aExpressions = new ArrayList(thePattern.getPathExpressions());

        // i think here we only care about collecting the vars we're using in non tbox atoms
//        for (int i = 0; i < aExpressions.size(); i++) {
//            PathExpression aPathExpr = (PathExpression) aExpressions.get(i);
//
//            if (aPathExpr instanceof TriplePattern) {
//                TriplePattern aTrip = (TriplePattern) aPathExpr;
//
//                if (!isSchemaTriple(aTrip) && aTrip.getSubjectVar().getValue() == null) {
//                    aVisitedSubjVars.add(aTrip.getSubjectVar());
//                }
//
//                if (!isSchemaTriple(aTrip) && aTrip.getObjectVar().getValue() == null) {
//                    aVisitedObjVars.add(aTrip.getObjectVar());
//                }
//            }
//        }

        while (!aExpressions.isEmpty()) {
            Iterator aExprIter = aExpressions.iterator();

            TriplePattern aTrip = null;
            int aBestScore = -1;

            while (aExprIter.hasNext()) {
                PathExpression aPathExpr = (PathExpression) aExprIter.next();
                if (aPathExpr instanceof GraphPattern) {
                    aGraphAtoms.add(aPathExpr);
                    aExprIter.remove();
                }
                else if (aPathExpr instanceof TriplePattern) {
                    int aScore = 0;

                    TriplePattern aTP = (TriplePattern) aPathExpr;

                    if (isTypeTriple(aTP)) {
                        aTypeAtoms.add(aTP);
                        aExprIter.remove();
                        continue;
                    }

                    // if there's a var in the subj position
                    if (aTP.getSubjectVar().getValue() == null) {
                        // if its bound to value, fantastic!
                        if (aBoundVars.contains(aTP.getSubjectVar())) {
                            aScore += 3;
                        }
                        else {
                            // otherwise, lets see if the var is used in atoms we've got "above" it in the query
                            // if there are some overlaps in the usage, this will be easier to eval
                            if (aVisitedObjVars.contains(aTP.getSubjectVar())) {
                                aScore += 2;
                            }
                            else if (aVisitedSubjVars.contains(aTP.getSubjectVar())) {
                                aScore += 1;
                            }
                        }
                    }
                    else {
                        // the var in the subj position is bound to something, that's awesome!
                        aScore += 4;
                    }


                    // there's a var in the object position...
                    if (aTP.getObjectVar().getValue() == null) {
                        // if its going to be bound to a value, that's great
                        if (aBoundVars.contains(aTP.getObjectVar())) {
                            aScore += 3;
                        }
                        else {
                            // its not bound to anything, so lets see if vars in the subj or obj position are being
                            // used already, in which case, it is still not bad to evaluate

                            if (aVisitedSubjVars.contains(aTP.getObjectVar())) {
                                aScore += 2;
                            }
                            else if (aVisitedObjVars.contains(aTP.getObjectVar())) {
                                aScore += 1;
                            }
                        }
                    }
                    else {
                        // the var in the obj position is bound to something, that's awesome!
                        aScore += 4;
                    }

                    // TODO: should we penalize atoms w/ unbound vars in the predicate position?

                    if (aScore > aBestScore) {
                        aBestScore = aScore;
                        aTrip = aTP;
                    }
                }
            }

            if (aTrip != null) {
                aExpressions.remove(aTrip);

                aAtomList.add(aTrip);

                if (!isSchemaTriple(aTrip) && aTrip.getSubjectVar().getValue() == null) {
                    aVisitedSubjVars.add(aTrip.getSubjectVar());
                }

                if (!isSchemaTriple(aTrip) && aTrip.getObjectVar().getValue() == null) {
                    aVisitedObjVars.add(aTrip.getObjectVar());
                }
            }
        }

        aAtomList.addAll(aTypeAtoms);
        aAtomList.addAll(aGraphAtoms);

        if (aAtomList.size() != thePattern.getPathExpressions().size()) {
            throw new Exception("Oops, we lost some atoms");
        }

        aOptimizedPattern.setPathExpressions(aAtomList);

        aOptimizedPattern.setOptionals(thePattern.getOptionals());

        if (thePattern.getRootConstraint() != null) {
            aOptimizedPattern.setConstraints(thePattern.getRootConstraint());
        }

        return aOptimizedPattern;
    }
}
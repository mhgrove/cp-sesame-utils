package com.clarkparsia.sesame.utils.query.optimize;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.BooleanExpr;
import org.openrdf.sesame.sail.query.And;
import org.openrdf.sesame.sail.query.ValueCompare;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.model.Value;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Jan 29, 2008 12:26:57 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class VariableInlinePatternOptimizer extends AbstractPatternOptimizer {

    public VariableInlinePatternOptimizer() {
        super();
    }

    public VariableInlinePatternOptimizer(GraphPatternOptimizer theOpt) {
        super(theOpt);
    }

    protected GraphPattern performOptimization(GraphPattern thePattern) throws Exception {
        Map aActuallyBoundVars = collectBoundVars(thePattern.getRootConstraint());

        List aConstraintsToRemove = new ArrayList();

        List aConstraintList = new ArrayList(thePattern.getConjunctiveConstraints());
        Iterator aConstraintIter = aConstraintList.iterator();
        while (aConstraintIter.hasNext()) {
            BooleanExpr aCons = (BooleanExpr) aConstraintIter.next();

            if (aCons instanceof ValueCompare) {
                ValueCompare aValCompare = (ValueCompare) aCons;

                // we only care about value compare's that use eq, everything else we can't inline
                if (aValCompare.getOperator() == ValueCompare.EQ) {
                    if (aValCompare.getLeftArg().getValue() != null && aValCompare.getRightArg().getValue() == null &&
                        aActuallyBoundVars.containsKey(aValCompare.getRightArg())) {
                        aConstraintsToRemove.add(aCons);
                    }
                    else if (aValCompare.getLeftArg().getValue() == null && aValCompare.getRightArg().getValue() != null &&
                             aActuallyBoundVars.containsKey(aValCompare.getLeftArg())) {
                        aConstraintsToRemove.add(aCons);
                    }
//                    else if (aValCompare.getLeftArg().getValue() == null && aValCompare.getRightArg().getValue() == null &&
//                             aActuallyBoundVars.containsKey(aValCompare.getLeftArg()) && aActuallyBoundVars.containsKey(aValCompare.getRightArg())) {
//                        // both unbound vars, but they're given values elsewhere, i think we can remove this clause and
//                        // the value should be inlined due to their use in other place
//                    }
                }
            }
            else if (aCons instanceof And) {
                Map aTemp = collectBoundVars(aCons);
                if (aTemp.size() > 0) {
                    // if there are entries in this, it should mean that this And is comprised of all And'ed ValueCompare
                    // operators so we can safely remove the constraint
                    aConstraintsToRemove.add(aCons);
                }
            }
        }

        // don't know if we need this code or not, but it actually removes vars marked as bound, but that somehow
        // dont get used in a constraint, and thus, we should subst them into the graph pattern.
//        Iterator aVarIter = aActuallyBoundVars.keySet().iterator();
//        while (aVarIter.hasNext()) {
//            Object aVar = aVarIter.next();
//
//            boolean found = false;
//            Iterator consIter = aConstraintsToRemove.iterator();
//            while (consIter.hasNext()) {
//                BooleanExpr aCons = (BooleanExpr) consIter.next();
//
//                if (isInConstraint(aVar, aCons)) {
//                    found = true;
//                    break;
//                }
//            }
//
//            if (!found) {
//                aVarIter.remove();
//            }
//        }

        GraphPattern aPattern = substVars(thePattern, aActuallyBoundVars);

        // you HAVE to remove them otherwise you get a serql parse error from sesame
        aConstraintList.removeAll(aConstraintsToRemove);

        aPattern.setConstraints(aConstraintList);
        
        return aPattern;
    }

    private boolean isInConstraint(Object theVar, BooleanExpr theExpr) {
        if (theExpr instanceof ValueCompare) {
            ValueCompare aValCompare = (ValueCompare) theExpr;

            // we only care about value compare's that use eq, everything else we can't inline
            if (aValCompare.getOperator() == ValueCompare.EQ) {
                if (aValCompare.getLeftArg().equals(theVar) || aValCompare.getRightArg().equals(theVar)) {
                    return true;
                }
            }
        }
        else if (theExpr instanceof And) {
            And aAnd = (And) theExpr;
            if (isInConstraint(theVar, aAnd.getLeftArg()) || isInConstraint(theVar, aAnd.getRightArg())) {
                return true;
            }
        }

        return false;
    }

    private GraphPattern substVars(GraphPattern thePattern, Map theBoundVars) {
        GraphPattern aPattern = thePattern;

        List aNewExpressions = new ArrayList();

        List aExpressions = aPattern.getPathExpressions();
        Iterator aExprIter = aExpressions.iterator();
        while (aExprIter.hasNext()) {
            PathExpression aExpr = (PathExpression) aExprIter.next();

            if (aExpr instanceof GraphPattern) {
                aNewExpressions.add(substVars((GraphPattern) aExpr, theBoundVars));
            }
            else if (aExpr instanceof TriplePattern) {
                TriplePattern aTriple = (TriplePattern) aExpr;

                if (theBoundVars.containsKey(aTriple.getSubjectVar())) {
                    aTriple.getSubjectVar().setValue( (Value) theBoundVars.get(aTriple.getSubjectVar()));
                }

                if (theBoundVars.containsKey(aTriple.getPredicateVar())) {
                    aTriple.getPredicateVar().setValue( (Value) theBoundVars.get(aTriple.getPredicateVar()));
                }

                if (theBoundVars.containsKey(aTriple.getObjectVar())) {
                    aTriple.getObjectVar().setValue( (Value) theBoundVars.get(aTriple.getObjectVar()));
                }

                aNewExpressions.add(aTriple);
            }
            else {
                aNewExpressions.add(aExpr);
            }
        }

        aPattern.setPathExpressions(aNewExpressions);

        return aPattern;
    }

    private Map collectBoundVars(BooleanExpr theExpr) {
        Map aVars = new HashMap();

        if (theExpr instanceof ValueCompare) {
            ValueCompare aValCompare = (ValueCompare) theExpr;

            // we only care about value compare's that use eq, everything else we can't inline
            if (aValCompare.getOperator() == ValueCompare.EQ) {
                if (aValCompare.getLeftArg().getValue() != null && aValCompare.getRightArg().getValue() == null) {
                    aVars.put(aValCompare.getRightArg(), aValCompare.getLeftArg().getValue());
                }
                else if (aValCompare.getLeftArg().getValue() == null && aValCompare.getRightArg().getValue() != null) {
                    aVars.put(aValCompare.getLeftArg(), aValCompare.getRightArg().getValue());
                }
            }
        }
        else if (theExpr instanceof And) {
            And aAnd = (And) theExpr;

            Map aLeftVars = collectBoundVars(aAnd.getLeftArg());
            Map aRightVars = collectBoundVars(aAnd.getRightArg());

            if (aLeftVars.size() > 0 && aRightVars.size() > 0) {
                aVars.putAll(aLeftVars);
                aVars.putAll(aRightVars);
            }
        }
        else {
            // for every other kind of constraint, In, Exists, IsLiteral, etc. we can't inline, so we'll ignore them
        }

        return aVars;
    }
}

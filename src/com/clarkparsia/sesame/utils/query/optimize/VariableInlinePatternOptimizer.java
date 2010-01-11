/*
 * Copyright (c) 2005-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.sesame.utils.query.optimize;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;

import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.BooleanExpr;
import org.openrdf.sesame.sail.query.And;
import org.openrdf.sesame.sail.query.ValueCompare;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.sesame.sail.query.Var;
import org.openrdf.sesame.sail.query.ValueExpr;
import org.openrdf.model.Value;

/**
 * <p>Optimizer which will inline variables where appropriate.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class VariableInlinePatternOptimizer extends AbstractPatternOptimizer {
	private Collection<?> mDoNotInline;

    public VariableInlinePatternOptimizer() {
        super();
    }

    public VariableInlinePatternOptimizer(GraphPatternOptimizer theOpt) {
        super(theOpt);
    }

	public GraphPattern optimize(GraphPattern thePattern, Collection theNoInline) throws Exception {
		// we pass in the project variables here so we don't optimize out the variables we're selecting
		// that leads to disaster if there are vars in the projection which are not in any query atom

		mDoNotInline = theNoInline;

		GraphPattern aPattern = super.optimize(thePattern);

		mDoNotInline.clear();

		return aPattern;
	}

    protected GraphPattern performOptimization(GraphPattern thePattern) throws Exception {
        Map aActuallyBoundVars = collectBoundVars(thePattern.getRootConstraint());

        List<BooleanExpr> aConstraintsToRemove = new ArrayList<BooleanExpr>();

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

        List<PathExpression> aNewExpressions = new ArrayList<PathExpression>();

        List aExpressions = aPattern.getPathExpressions();
        Iterator aExprIter = aExpressions.iterator();
        while (aExprIter.hasNext()) {
            PathExpression aExpr = (PathExpression) aExprIter.next();

            if (aExpr instanceof GraphPattern) {
                aNewExpressions.add(substVars((GraphPattern) aExpr, theBoundVars));
            }
            else if (aExpr instanceof TriplePattern) {
                TriplePattern aTriple = (TriplePattern) aExpr;

                if (theBoundVars.containsKey(aTriple.getSubjectVar()) && !mDoNotInline.contains(aTriple.getSubjectVar())) {
                    aTriple.getSubjectVar().setValue( (Value) theBoundVars.get(aTriple.getSubjectVar()));
                }

                if (theBoundVars.containsKey(aTriple.getPredicateVar()) && !mDoNotInline.contains(aTriple.getPredicateVar())) {
                    aTriple.getPredicateVar().setValue( (Value) theBoundVars.get(aTriple.getPredicateVar()));
                }

                if (theBoundVars.containsKey(aTriple.getObjectVar()) && !mDoNotInline.contains(aTriple.getObjectVar())) {
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

    private Map<ValueExpr, Value> collectBoundVars(BooleanExpr theExpr) {
        Map<ValueExpr, Value> aVars = new HashMap<ValueExpr, Value>();

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

            Map<ValueExpr, Value> aLeftVars = collectBoundVars(aAnd.getLeftArg());
            Map<ValueExpr, Value> aRightVars = collectBoundVars(aAnd.getRightArg());

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

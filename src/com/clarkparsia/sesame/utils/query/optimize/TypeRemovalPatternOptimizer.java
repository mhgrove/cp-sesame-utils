package com.clarkparsia.sesame.utils.query.optimize;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.model.Value;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Feb 8, 2008 5:18:51 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class TypeRemovalPatternOptimizer extends AbstractPatternOptimizer {
    KBStats mKbStats = new KBStats();

    public TypeRemovalPatternOptimizer(SesameRepository theRepository) {
        mKbStats.initialize(theRepository);
    }

    protected GraphPattern performOptimization(GraphPattern thePattern) throws Exception {
        List aExpressionList = new ArrayList(thePattern.getPathExpressions());

        List aVarsUsedInABoxAtoms = new ArrayList();

        Iterator aExprIter = aExpressionList.iterator();
        while (aExprIter.hasNext()) {
            PathExpression aPathExpr = (PathExpression) aExprIter.next();

            if (aPathExpr instanceof TriplePattern) {
                TriplePattern aTrip = (TriplePattern) aPathExpr;

                if (!isSchemaTriple(aTrip) &&
                    aTrip.getPredicateVar().getValue() != null &&
                    mKbStats.mDomainStats.containsKey(aTrip.getPredicateVar().getValue())) {

                    if (aTrip.getSubjectVar().getValue() == null) {
                        aVarsUsedInABoxAtoms.add(aTrip.getSubjectVar());
                    }

                    if (aTrip.getObjectVar().getValue() == null) {
                        aVarsUsedInABoxAtoms.add(aTrip.getObjectVar());
                    }
                }
            }
        }

        aExprIter = aExpressionList.iterator();
        while (aExprIter.hasNext()) {
            PathExpression aPathExpr = (PathExpression) aExprIter.next();

            if (aPathExpr instanceof TriplePattern) {
                TriplePattern aTrip = (TriplePattern) aPathExpr;


                if (isTypeTriple(aTrip) && aVarsUsedInABoxAtoms.contains(aTrip.getSubjectVar())) {
                    // this triple was used in an abox atom (s p o) where p is some property
                    // in the abox and the var is being declared as having a type, where the type
                    // is the domain of said proeprty (p), thus the instance more or less has
                    // to be of that type, so including the type triple atom is redundant, so we
                    // can remove it.  the only case where it's not ok to remove this is when
                    // there is no inferencing on the kb and the data has no verified integrity
                    // i.e. a property is used on an instance whose type is not in the domain
                    // of the property.  this would not happen in an inferencing kb, the inst
                    // with property p would be inferred to have a type that is the domain of the
                    // property if it not already explicitly stated, and if that inference clashes
                    // with other assertions, you end up with an inconsistent kb, which is no good anyway.

                    aExprIter.remove();
                }
            }
        }

        thePattern.setPathExpressions(aExpressionList);

        return thePattern;
    }

    private class KBStats {
        Map mRangeStats = new HashMap();
        Map mDomainStats = new HashMap();

        public void initialize(SesameRepository theRepo) {
            mRangeStats.clear();
            mDomainStats.clear();

            collectDomainStats(theRepo);
            collectRangeStats(theRepo);
        }

        private void collectRangeStats(SesameRepository theRepo) {
            mRangeStats.clear();

            String aQuery = "select distinct prop, range from " +
                            "{prop} rdfs:range {range}, " +
                            "{s} prop {o} " +
                            "where range IN (select aType from {o} rdf:type {aType})";

            // TODO: delete the someProperty assertion from the test pops KB to make sure props w/o instances that have
            // a type in the domain of the property don't show up


            // remember this is only useful when all the instances are typed, and props are used according to their
            // domain/range.  otherwise if there's no integrity to the data, ie using a proeprty on an instance of a type
            // where the property is not valid, then you can end up with the wrong results

            try {
                QueryResultsTable aResults = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

                for (int aRow = 0; aRow < aResults.getRowCount(); aRow++) {
                    Value aProp = aResults.getValue(aRow, 0);
                    Value aDomain = aResults.getValue(aRow, 1);

                    Set aSet;
                    if (mRangeStats.containsKey(aProp)) {
                        aSet = (Set) mRangeStats.get(aProp);
                    }
                    else {
                        aSet = new HashSet();
                    }

                    aSet.add(aDomain);
                    mRangeStats.put(aProp, aSet);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }

        private void collectDomainStats(SesameRepository theRepo) {
            mDomainStats.clear();

            String aQuery = "select distinct prop, domain from " +
                            "{prop} rdfs:domain {domain}, " +
                            "{s} prop {o} " +
                            "where domain IN (select aType from {s} rdf:type {aType})";

            //String aFilterQuery = aQuery + ", {s} rdf:type {d2} where domain != d2";

            // TODO: delete the someProperty assertion from the test pops KB to make sure props w/o instances that have
            // a type in the domain of the property don't show up
            // remember this is only useful when all the instances are typed, and props are used according to their
            // domain/range.  otherwise if there's no integrity to the data, ie using a proeprty on an instance of a type
            // where the property is not valid, then you can end up with the wrong results

            try {
                QueryResultsTable aResults = theRepo.performTableQuery(QueryLanguage.SERQL, aQuery);

                for (int aRow = 0; aRow < aResults.getRowCount(); aRow++) {
                    Value aProp = aResults.getValue(aRow, 0);
                    Value aDomain = aResults.getValue(aRow, 1);

                    Set aSet;
                    if (mDomainStats.containsKey(aProp)) {
                        aSet = (Set) mDomainStats.get(aProp);
                    }
                    else {
                        aSet = new HashSet();
                    }

                    aSet.add(aDomain);
//System.err.println("Domain: " + aProp + " -> " + aSet);
                    mDomainStats.put(aProp, aSet);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }
}

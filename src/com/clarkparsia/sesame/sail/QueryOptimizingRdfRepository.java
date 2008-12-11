package com.clarkparsia.sesame.sail;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.net.URL;

import org.openrdf.sesame.sailimpl.memory.RdfRepository;

import org.openrdf.sesame.sail.SailUpdateException;
import org.openrdf.sesame.sail.StackedSail;
import org.openrdf.sesame.sail.Sail;
import org.openrdf.sesame.sail.SailInternalException;
import org.openrdf.sesame.sail.SailInitializationException;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sail.NamespaceIterator;
import org.openrdf.sesame.sail.SailChangedListener;

import org.openrdf.sesame.sail.query.Query;
import org.openrdf.sesame.sail.query.GraphPatternQuery;
import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.BooleanExpr;
import org.openrdf.sesame.sail.query.ValueCompare;

import org.openrdf.sesame.sailimpl.memory.MemStatement;

import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.serql.SerqlEngine;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.constants.QueryLanguage;

import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.config.RepositoryConfig;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.SesameService;
import org.openrdf.sesame.repository.local.LocalService;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.util.log.ThreadLog;
import org.apache.log4j.Logger;

import com.clarkparsia.sesame.utils.query.optimize.AbstractPatternOptimizer;
import com.clarkparsia.sesame.utils.query.optimize.GraphPatternOptimizer;
import com.clarkparsia.sesame.utils.query.optimize.VariableInlinePatternOptimizer;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 11, 2008 10:53:54 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class QueryOptimizingRdfRepository extends RdfRepository {
    public QueryOptimizingRdfRepository() {
        System.err.println("creating query optimizing rdf repository");
    }

    public void addStatement(Resource theSubject, URI thePredicate, Value theObject) throws SailUpdateException {
		if (!transactionStarted()) {
			throw new SailUpdateException("No transaction started");
		}

        increaseUsage(thePredicate);

        super.addStatement(theSubject, thePredicate, theObject);
    }

    protected MemStatement _addStatement(Resource theSubject, URI thePred, Value theObj) {
        MemStatement aStmt = super._addStatement(theSubject, thePred, theObj);

        increaseUsage(thePred);

        return aStmt;
    }

    public int removeStatements(Resource theSubject, URI thePredicate, Value theObject) throws SailUpdateException {
		if (!transactionStarted()) {
			throw new SailUpdateException("No transaction started");
		}

        decreaseUsage(thePredicate);

        return super.removeStatements(theSubject, thePredicate, theObject);
    }
    
    private final GraphPatternOptimizer OPTIMIZER = new WeightedUsagePatternOptimizer(new VariableInlinePatternOptimizer());

    public static boolean USE_NATIVE_OPTIMIZATION = false;

    public Query optimizeQuery(Query theQuery) {
        if (USE_NATIVE_OPTIMIZATION || !(theQuery instanceof GraphPatternQuery)) {
            super.optimizeQuery(theQuery);
            return theQuery;
        }
        else if (theQuery instanceof GraphPatternQuery) {
            GraphPatternQuery aPatternQuery = (GraphPatternQuery) theQuery;

            try {
                aPatternQuery.setGraphPattern(OPTIMIZER.optimize(aPatternQuery.getGraphPattern()));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        return new SailQuery(aPatternQuery);
        }
        else return theQuery;
    }


//
//    public void initialize(Map theMap) throws SailInitializationException {
//		if (mRepository == null) {
//			throw new SailInitializationException("Base sail was not set");
//		}
//
//		ThreadLog.trace("QueryOptimizingRdfRepository initialized");
//
//        // TODO: do we call initialize on the base sail?  or has it already been initialized?
//    }



    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////

    private Map mPropertyUsage = new HashMap();

    private void increaseUsage(URI theURI) {
        Long aCount = new Long(0);

        if (mPropertyUsage.containsKey(theURI)) {
            aCount = (Long) mPropertyUsage.get(theURI);
        }

        aCount = new Long(aCount.intValue() + 1);

        mPropertyUsage.put(theURI, aCount);
    }

    private void decreaseUsage(URI theURI) {
        if (mPropertyUsage.containsKey(theURI)) {
            Long aCount = (Long) mPropertyUsage.get(theURI);

            mPropertyUsage.put(theURI, new Long(aCount.intValue() - 1));
        }
    }

    private long usage(URI theURI) {
        int aUsage = -1;

        if (mPropertyUsage.containsKey(theURI)) {
            aUsage = ((Long) mPropertyUsage.get(theURI)).intValue();
        }

        return aUsage;
    }

    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////

    /**
     * Simple GraphPattern optimizer that will order the atoms in ascending order according to their usage in the KB
     */
    private class UsagePatternOptimizer extends AbstractPatternOptimizer {
        public UsagePatternOptimizer(GraphPatternOptimizer theOpt) {
            super(theOpt);
        }

        protected GraphPattern performOptimization(GraphPattern thePattern) throws Exception {
            GraphPattern aOptimizedPattern = new GraphPattern();

            List aTypeAtoms = new ArrayList();
            List aOtherAtoms = new ArrayList();
            List aGraphAtoms = new ArrayList();
            List aAtomList = new ArrayList();

            List aExpressions = new ArrayList(thePattern.getPathExpressions());

            Iterator aExprIter = aExpressions.iterator();

            while (aExprIter.hasNext()) {
                PathExpression aPathExpr = (PathExpression) aExprIter.next();

                if (aPathExpr instanceof GraphPattern) {
                    aGraphAtoms.add(aPathExpr);
                }
                else if (aPathExpr instanceof TriplePattern) {

                    TriplePattern aTP = (TriplePattern) aPathExpr;

                    if (isTypeTriple(aTP)) {
                        aTypeAtoms.add(aTP);
                    }
                    else {
                        aAtomList.add(aTP);
                    }
                }
                else {
                    aOtherAtoms.add(aPathExpr);
                }
            }

            Collections.sort(aAtomList, COMPARATOR);

            aAtomList.addAll(aTypeAtoms);
            aAtomList.addAll(aOtherAtoms);
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

    private class TriplePatternUsageComparator implements Comparator {
        public int compare(Object theObject, Object theObject1) {
            TriplePattern aPattern1 = (TriplePattern) theObject;
            TriplePattern aPattern2 = (TriplePattern) theObject1;

            Long aUsage1 = new Long(aPattern1.getPredicateVar().getValue() == null ? Long.MAX_VALUE : usage( (URI) aPattern1.getPredicateVar().getValue()));
            Long aUsage2 = new Long(aPattern2.getPredicateVar().getValue() == null ? Long.MAX_VALUE : usage( (URI) aPattern2.getPredicateVar().getValue()));

            return aUsage1.compareTo(aUsage2);
        }
    }

    private final Comparator COMPARATOR = new TriplePatternUsageComparator();

    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////

    /**
     * Weighted GraphPattern optimizer.  Will filter type triples to the bottom of the list of atoms, move any atoms
     * which dont have any variables, or whose variables are all bound to values to the top of the list.  and then
     * will order the remaining atoms inbetween according to how many variables are in the triple, if they've
     * been used in a previous atom, and by usage in the KB.
     */
    private class WeightedUsagePatternOptimizer extends AbstractPatternOptimizer {

        public WeightedUsagePatternOptimizer() {
            super();
        }

        public WeightedUsagePatternOptimizer(GraphPatternOptimizer theOpt) {
            super(theOpt);
        }

        protected GraphPattern performOptimization(GraphPattern thePattern) throws Exception {
            GraphPattern aOptimizedPattern = new GraphPattern();

            List aTypeAtoms = new ArrayList();
            List aAtomList = new ArrayList();
            List aGraphAtoms = new ArrayList();
            List aOtherAtoms = new ArrayList();

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

            Iterator aIter = aExpressions.iterator();

            while (aIter.hasNext()) {
                PathExpression aPathExpr = (PathExpression) aIter.next();

                if (aPathExpr instanceof TriplePattern) {
                    TriplePattern aTrip = (TriplePattern) aPathExpr;

                    // move any atoms to the front of the list which don't contain any variables in them, or have
                    // variables which are bound to values.
                    if ((aTrip.getSubjectVar().getValue() != null || aBoundVars.contains(aTrip.getSubjectVar())) &&
                        (aTrip.getPredicateVar().getValue() != null || aBoundVars.contains(aTrip.getPredicateVar())) &&
                        (aTrip.getObjectVar().getValue() != null || aBoundVars.contains(aTrip.getObjectVar()))) {

                        aAtomList.add(aTrip);
                        aIter.remove();
                    }
                }
            }

            int lastScore = -1;
            List sameScores = new ArrayList();

            while (!aExpressions.isEmpty()) {
                Iterator aExprIter = aExpressions.iterator();

                TriplePattern aBestTrip = null;
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
                            aBestTrip = aTP;
                        }
//                        else if (aScore == aBestScore) {
//                            if (COMPARATOR.compare(aBestTrip, aTP) == 1) {
//                                aBestScore = aScore;
//                                aBestTrip = aTP;
//                            }
//                        }
                    }
                    else {
                        aOtherAtoms.add(aPathExpr);
                        aExprIter.remove();
                    }
                }

                if (aBestTrip != null) {
                    aExpressions.remove(aBestTrip);

                    if (lastScore == -1) {
                        lastScore = aBestScore;
                        aAtomList.add(aBestTrip);
                    }
                    else if (lastScore != aBestScore) {

                        Collections.sort(sameScores, COMPARATOR);
                        aAtomList.addAll(sameScores);
                        sameScores.clear();

                        sameScores.add(aBestTrip);
                        lastScore = aBestScore;
                    }
                    else if (lastScore == aBestScore) {
                        sameScores.add(aBestTrip);
                    }
                    else {
                        // don't know what to do in this case, so punt.
                        throw new RuntimeException();
                    }

                    if (!isSchemaTriple(aBestTrip) && aBestTrip.getSubjectVar().getValue() == null) {
                        aVisitedSubjVars.add(aBestTrip.getSubjectVar());
                    }

                    if (!isSchemaTriple(aBestTrip) && aBestTrip.getObjectVar().getValue() == null) {
                        aVisitedObjVars.add(aBestTrip.getObjectVar());
                    }
                }
            }

            if (!sameScores.isEmpty()) {
                aAtomList.addAll(sameScores);
            }

            aAtomList.addAll(aTypeAtoms);
            aAtomList.addAll(aOtherAtoms);
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

    public static void main(String[] theArgs) throws Exception {
        System.err.println("good main");


//        String aQuery = "select  distinct uri, aLabel from\n" +
//                        "{phantom_eoej} <http://www.clarkparsia.com/baseball/position> {var0},\n" +
//                        "{phantom_eoej} <http://www.clarkparsia.com/baseball/player> {goal_base},\n" +
//                        "{var0} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/position/Position>},\n" +
//                        "{phantom_klll} <http://www.clarkparsia.com/baseball/battingAverage> {uri},\n" +
//                        "{goal_base} <http://www.clarkparsia.com/baseball/careerBatting> {phantom_klll},\n" +
//                        "{var2} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/team/Team>},\n" +
//                        "{phantom_eoej} <http://www.clarkparsia.com/baseball/team> {var2},\n" +
//                        "{goal_base} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/Player>},\n" +
//                        "[{uri} <http://www.w3.org/2000/01/rdf-schema#label> {aLabel}]\n" +
//                        "where (var0 = <http://www.clarkparsia.com/baseball/position/FirstBase>) limit 10000";


//        String aQuery = "select  distinct uri, aLabel from " +
//                        "{uri} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/position/Position>}, " +
//                        "{var1} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/team/Team>}, " +
//                        "{goal_base} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/Player>}, " +
//                        "{goal_base} <http://www.clarkparsia.com/baseball/careerBatting> {phantom1}, " +
//                        "{phantom0} <http://www.clarkparsia.com/baseball/player> {goal_base}, " +
//                        "{phantom0} <http://www.clarkparsia.com/baseball/position> {uri}, " +
//                        "{phantom0} <http://www.clarkparsia.com/baseball/team> {var1}, " +
//                        "{phantom1} <http://www.clarkparsia.com/baseball/homeruns> {var2}, " +
//                        "[{uri} <http://www.w3.org/2000/01/rdf-schema#label> {aLabel}]  limit 10000";



        String aQuery = "select   uri from " +
                        "{uri} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/position/Position>}, " +
                        "{var1} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/team/Team>}, " +
                        "{goal_base} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://www.clarkparsia.com/baseball/Player>}, " +
                        "{goal_base} <http://www.clarkparsia.com/baseball/careerBatting> {phantom1}, " +
                        "{phantom0} <http://www.clarkparsia.com/baseball/player> {goal_base}, " +
                        "{phantom0} <http://www.clarkparsia.com/baseball/position> {uri}, " +
                        "{phantom0} <http://www.clarkparsia.com/baseball/team> {var1}, " +
                        "{phantom1} <http://www.clarkparsia.com/baseball/battingAverage> {var2} " +
                        "";

//        if (true) {
//            Query q = new SerqlEngine(new RdfRepository()).parseTableQuery(aQuery).getQuery();
//            System.err.println("UNOPTIMIZED:\n "+q);
//            QueryOptimizer.optimizeQuery(q);
//            System.err.println("OPTIMIZED:\n "+q);
//            USE_NATIVE_OPTIMIZATION = false;
//            new QueryOptimizingRdfRepository().optimizeQuery(q);
//            System.err.println("OUR OPTIMIZE:\n"+q);
//            return;
//        }

//        SesameService aRemoteService = Sesame.getService(new URL("http://www.clarkparsia.com:1234/sesame"));
//        SesameRepository aRemoteRepo = aRemoteService.getRepository("baseball-mem-rdf-db");

        
//        RepositoryConfig remoteConfig = new RepositoryConfig("remote");
//        SailConfig remotesyncSail = new SailConfig("org.openrdf.sesame.sailimpl.sync.SyncRdfRepository");
//        SailConfig remotequerySail = new SailConfig("org.openrdf.sesame.sailimpl.memory.RdfRepository");
//        remotequerySail.setParameter("compressFile", "no");
//        remotequerySail.setParameter("dataFormat", "ntriples");
//        remotequerySail.setParameter("file", "/Users/mhgrove/Desktop/query_test_case.nt.txt");
////        remotequerySail.setParameter("dataFormat", "rdfxml");
////        remotequerySail.setParameter("file", "/Users/mhgrove/work/projects/baseball/output/baseball.stats.out.rdf");
//        remoteConfig.addSail(remotesyncSail);
//        remoteConfig.addSail(remotequerySail);
//        remoteConfig.setWorldReadable(true);
//
//        SesameRepository aRemoteRepo = Sesame.getService().createRepository(remoteConfig);

        LocalService aLocalService = Sesame.getService();

        RepositoryConfig repConfig = new RepositoryConfig("repo");

        SailConfig syncSail = new SailConfig("org.openrdf.sesame.sailimpl.sync.SyncRdfRepository");
//        SailConfig dummySail = new SailConfig("com.clarkparsia.sesame.sail.CachingRdfRepository");
        SailConfig querySail = new SailConfig("com.clarkparsia.sesame.sail.QueryOptimizingRdfRepository");
//        SailConfig querySail = new SailConfig("org.openrdf.sesame.sailimpl.memory.RdfRepository");
        querySail.setParameter("compressFile", "no");
        querySail.setParameter("dataFormat", "rdfxml");
        querySail.setParameter("file", "/Users/mhgrove/work/projects/baseball/output/baseball.stats.out.rdf");
//        querySail.setParameter("dataFormat", "ntriples");
//        querySail.setParameter("file", "/Users/mhgrove/Desktop/query_test_case.nt.txt");


        repConfig.addSail(syncSail);
        repConfig.addSail(querySail);
        repConfig.setWorldReadable(true);

        long s = System.currentTimeMillis();
        SesameRepository aRepo = aLocalService.createRepository(repConfig);
        long e = System.currentTimeMillis();
System.err.println("done creating " + (e-s));

//        Map aResultsForChecking = collectResults(Arrays.asList(new String[] { aQuery }), aRemoteRepo);
//        printResults(aRemoteRepo.performTableQuery(QueryLanguage.SERQL, aQuery));
        s = System.currentTimeMillis();
        QueryResultsTable aResults = aRepo.performTableQuery(QueryLanguage.SERQL, aQuery);
        System.err.println("num results: " + aResults.getRowCount());
        printResults(aResults);
//        System.err.println("RESULTS ARE THE SAME: " + compareResultSets(aResults, (QueryResultsTable) aResultsForChecking.get(aQuery)));
        e = System.currentTimeMillis();

System.err.println("done querying " + (e-s));


        System.err.println("====================");

//        System.err.println("Running w/ default");
//        runQuery(aRemoteRepo, aQuery);

//        USE_NATIVE_OPTIMIZATION = true;
//        System.err.println("Running w/ native");
//        runQuery(aRepo, aQuery);

        USE_NATIVE_OPTIMIZATION = false;
        System.err.println("Running w/ our opt");
        runQuery(aRepo, aQuery);
    }

    private static void runQuery(SesameRepository theRepo, String theQuery) throws Exception {
        long time = 0;
        int NUM_TRIES = 10;

        for (int i = 0; i < NUM_TRIES; i++) {
            long s = System.currentTimeMillis();
            theRepo.performTableQuery(QueryLanguage.SERQL, theQuery);
            long e = System.currentTimeMillis();

            time += (e-s);
        }

        System.err.println("total time: " + time);
        System.err.println("average run time: " + (time/NUM_TRIES));
    }

    private static void printResults(QueryResultsTable theTable) {
        for (int aRow = 0; aRow < theTable.getRowCount(); aRow++) {
            for (int aCol = 0; aCol < theTable.getColumnCount(); aCol++) {
                System.err.print(theTable.getColumnName(aCol) + "=" + theTable.getValue(aRow, aCol) + ", ");
            }
            System.err.println();
        }
    }

    private static boolean compareResultSets(QueryResultsTable theTable, QueryResultsTable theOtherTable) {
        return asSet(theTable).equals(asSet(theOtherTable));
    }

    private static Set asSet(QueryResultsTable theTable) {
        Set aSet = new HashSet();

        for (int aRow = 0; aRow < theTable.getRowCount(); aRow++) {
            Map aBinding = new HashMap();

            for (int aCol = 0; aCol < theTable.getColumnCount(); aCol++) {
                String aColName = theTable.getColumnName(aCol);
                aBinding.put(aColName, theTable.getValue(aRow, aCol));
            }

            aSet.add(aBinding);
        }

        return aSet;
    }

    private static Map collectResults(Collection theQueries, SesameRepository theCheck) throws Exception {
        Map aResultsList = new HashMap();

        Iterator aIter = theQueries.iterator();
        while (aIter.hasNext()) {
            String aQuery = (String) aIter.next();
            QueryResultsTable r = theCheck.performTableQuery(QueryLanguage.SERQL, aQuery);

            aResultsList.put(aQuery, r);
        }

        return aResultsList;
    }


}
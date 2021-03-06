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

package com.clarkparsia.sesame.utils.query;

import java.util.Iterator;
import java.util.Arrays;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import org.openrdf.sesame.sail.query.ConstructQuery;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.Union;
import org.openrdf.sesame.sail.query.SelectQuery;
import org.openrdf.sesame.sail.query.Var;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.BooleanExpr;
import org.openrdf.sesame.sail.query.ValueCompare;
import org.openrdf.sesame.sail.query.Null;
import org.openrdf.sesame.sail.query.ValueExpr;
import org.openrdf.sesame.sail.query.IsBNode;
import org.openrdf.sesame.sail.query.IsResource;
import org.openrdf.sesame.sail.query.IsLiteral;
import org.openrdf.sesame.sail.query.IsURI;
import org.openrdf.sesame.sail.query.And;
import org.openrdf.sesame.sail.query.Or;
import org.openrdf.sesame.sail.query.Not;
import org.openrdf.sesame.sail.query.ResourceExpr;
import org.openrdf.sesame.sail.query.LiteralExpr;
import org.openrdf.sesame.sail.query.Lang;
import org.openrdf.sesame.sail.query.Datatype;
import org.openrdf.sesame.sail.query.Query;

import org.openrdf.model.Value;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;

import com.clarkparsia.utils.io.IOUtil;

/**
 * <p>Renderer implementation to write a Sesame Query object out as sparql</p>
 *
 * @author Michael Grove
 * @since 1.0
 */

public class SPARQLQueryRenderer implements QueryRenderer {
    // sparql constants
    private static final String SPARQL_NULL = "NULL";
    private static final String SPARQL_VARIABLE = "?";

    private static final String SPARQL_OP_BOUND = "bound";
    private static final String SPARQL_OP_ISBLANK = "isBlank";
    private static final String SPARQL_OP_ISURI = "isURI";
    private static final String SPARQL_OP_ISLITERAL = "isLiteral";
    private static final String SPARQL_OP_LANG = "lang";
    private static final String SPARQL_OP_DATATYPE = "datatype";
    private static final String SPARQL_AND = "&&";
    private static final String SPARQL_OR = "||";
    private static final String SPARQL_EQ = "=";
    private static final String SPARQL_GE = ">=";
    private static final String SPARQL_GT = ">";
    private static final String SPARQL_LE = "<=";
    private static final String SPARQL_LT = "<";
    private static final String SPARQL_NE = "!=";
    private static final String SPARQL_OPTIONAL = "OPTIONAL";
    private static final String SPARQL_SELECT = "SELECT";
    private static final String SPARQL_DISTINCT = "DISTINCT";
    private static final String SPARQL_WHERE = "WHERE";
    private static final String SPARQL_FILTER = "FILTER";
    private static final String SPARQL_LIMIT = "LIMIT";
    private static final String SPARQL_OFFSET = "OFFSET";
    private static final String SPARQL_PATH_SEP = ".";
    private static final String SPARQL_UNION = "UNION";
    private static final String SPARQL_NOT = "!";
    private static final String SPARQL_CONSTRUCT = "CONSTRUCT";

    public String render(Query theQuery) {
        if (theQuery instanceof SelectQuery) {
            return serializeTableQuery( (SelectQuery) theQuery);
        }
        else if (theQuery instanceof ConstructQuery) {
            return serializeGraphQuery( (ConstructQuery) theQuery);
        }
        else if (theQuery instanceof Union) {
            return union( (Union) theQuery);
        }
        else {
            throw new IllegalArgumentException("NYI: can't render this query type");
        }
    }

    private String serializeGraphQuery(ConstructQuery theGraphQuery) {
        StringBuffer aNewQuery = new StringBuffer(SPARQL_CONSTRUCT + " ");

        // i dont think this means anything here, but we'll include it just in case.
        if (theGraphQuery.isDistinct()) {
            aNewQuery.append(SPARQL_DISTINCT).append(" ");
        }

        aNewQuery.append(" { ");

        Iterator aConstructGraphIter = Arrays.asList(theGraphQuery.getProjection()).iterator();
        while (aConstructGraphIter.hasNext()) {
            TriplePattern aTriplePattern = (TriplePattern) aConstructGraphIter.next();

            aNewQuery.append(serializeTriplePattern(aTriplePattern)).append(" ");

            if (aConstructGraphIter.hasNext()) {
                aNewQuery.append(". ");
            }
        }

        aNewQuery.append(" } ").append(IOUtil.ENDL);

        aNewQuery.append(SPARQL_WHERE);
        aNewQuery.append(IOUtil.ENDL);

        GraphPattern aGraphPattern = theGraphQuery.getGraphPattern();

        aNewQuery.append(serializeGraphPattern(aGraphPattern));

        aNewQuery.append(IOUtil.ENDL);

        if (theGraphQuery.hasOffset()) {
            aNewQuery.append(IOUtil.ENDL).append(SPARQL_OFFSET).append(" ").append(theGraphQuery.getOffset());
        }

        if (theGraphQuery.hasLimit()) {
            aNewQuery.append(IOUtil.ENDL).append(SPARQL_LIMIT).append(" ").append(theGraphQuery.getLimit());
        }

        return aNewQuery.toString();
    }

    private String serializeTableQuery(SelectQuery theQuery) {
        StringBuffer aNewQuery = new StringBuffer(SPARQL_SELECT + " ");

        if (theQuery.isDistinct()) {
            aNewQuery.append(SPARQL_DISTINCT).append(" ");
        }

        Set<Var> aVarList = new LinkedHashSet<Var>();
        theQuery.getProjectionVariables(aVarList);

        for (Var aVariable : aVarList) {
            aNewQuery.append(SPARQL_VARIABLE).append(aVariable.getName()).append(" ");
        }

        aNewQuery.append(IOUtil.ENDL);
        aNewQuery.append(SPARQL_WHERE).append(" ");
        aNewQuery.append(IOUtil.ENDL);

        GraphPattern aGraphPattern = theQuery.getGraphPattern();

        aNewQuery.append(serializeGraphPattern(aGraphPattern));

        aNewQuery.append(IOUtil.ENDL);

        if (theQuery.hasOffset()) {
            aNewQuery.append(IOUtil.ENDL).append(SPARQL_OFFSET).append(" ").append(theQuery.getOffset());
        }

        if (theQuery.hasLimit()) {
            aNewQuery.append(IOUtil.ENDL).append(SPARQL_LIMIT).append(" ").append(theQuery.getLimit());
        }

        return aNewQuery.toString();
    }

    private String union(Union theUnionQuery) {
        StringBuffer aNewQuery = new StringBuffer(SPARQL_SELECT + " ");

        SelectQuery aLeftArg = (SelectQuery) theUnionQuery.getLeftArg();
        SelectQuery aRightArg = (SelectQuery) theUnionQuery.getRightArg();

        if (aLeftArg.isDistinct() || aRightArg.isDistinct()) {
            aNewQuery.append(SPARQL_DISTINCT).append(" ");
        }

        // both left and right *should* select the same set of variables, serql doesnt require that, but i think the semantics
        // of sparql are different from serql (sparql one has one set of vars) so we'll assume for sanity's sake, and ease
        // or programming that we both sets of variables are the same
        Set<Var> aVarList = new LinkedHashSet<Var>();
        aLeftArg.getProjectionVariables(aVarList);

        for (Var aVariable : aVarList) {
            aNewQuery.append(SPARQL_VARIABLE).append(aVariable.getName()).append(" ");
        }

        aNewQuery.append(IOUtil.ENDL);
        aNewQuery.append(SPARQL_WHERE).append(" {");
        aNewQuery.append(IOUtil.ENDL);

        aNewQuery.append(serializeGraphPattern(aLeftArg.getGraphPattern()));
        aNewQuery.append(IOUtil.ENDL).append(SPARQL_UNION).append(IOUtil.ENDL);
        aNewQuery.append(serializeGraphPattern(aRightArg.getGraphPattern()));

        aNewQuery.append("}").append(IOUtil.ENDL);

        // again, we're going to assume that the left and right args of the serql union clause have the same offset/limit
        // as sparql only specifies one for the whole query
        if (aLeftArg.hasOffset()) {
            aNewQuery.append(IOUtil.ENDL).append(SPARQL_OFFSET).append(" ").append(aLeftArg.getOffset());
        }

        if (aLeftArg.hasLimit()) {
            aNewQuery.append(IOUtil.ENDL).append(SPARQL_LIMIT).append(" ").append(aLeftArg.getLimit());
        }

        return aNewQuery.toString();
    }

    private String serializeGraphPattern(GraphPattern theGraphPattern) {
        StringBuffer aBuffer = new StringBuffer("{");

        Iterator aIter = theGraphPattern.getPathExpressions().iterator();
        while (aIter.hasNext()) {
            PathExpression aExpression = (PathExpression) aIter.next();

            aBuffer.append(serializePathExpression(aExpression));

            aBuffer.append(SPARQL_PATH_SEP);
            aBuffer.append(IOUtil.ENDL);
        }

        aIter = theGraphPattern.getOptionals().iterator();
        while (aIter.hasNext()) {
            PathExpression aExpression = (PathExpression) aIter.next();

            aBuffer.append(SPARQL_OPTIONAL + " ").append(serializePathExpression(aExpression)).append(" ");

            aBuffer.append(SPARQL_PATH_SEP);
            aBuffer.append(IOUtil.ENDL);
        }

        aIter = theGraphPattern.getConjunctiveConstraints().iterator();
        while (aIter.hasNext()) {
            BooleanExpr aConstraint = (BooleanExpr) aIter.next();

            aBuffer.append(SPARQL_FILTER + "(").append(serializeConstraint(aConstraint)).append(")");
            aBuffer.append(SPARQL_PATH_SEP);
            aBuffer.append(IOUtil.ENDL);
        }

        aBuffer.append("}");
        aBuffer.append(IOUtil.ENDL);

        return aBuffer.toString();
    }

    private String serializePathExpression(PathExpression theExpression) {
        StringBuffer aBuffer = new StringBuffer();

        if (theExpression instanceof GraphPattern) {
            GraphPattern aGraphPattern = (GraphPattern) theExpression;

            aBuffer.append(serializeGraphPattern(aGraphPattern));
        }
        else if (theExpression instanceof TriplePattern) {
            TriplePattern aTriplePattern = (TriplePattern) theExpression;

            aBuffer.append(serializeTriplePattern(aTriplePattern));
        }

        return aBuffer.toString();
    }

    private String serializeTriplePattern(TriplePattern theTriplePattern) {
        StringBuffer aBuffer = new StringBuffer();

        aBuffer.append(serializeVar(theTriplePattern.getSubjectVar())).append(" ");
        aBuffer.append(serializeVar(theTriplePattern.getPredicateVar())).append(" ");
        aBuffer.append(serializeVar(theTriplePattern.getObjectVar()));

        return aBuffer.toString();
    }

    private String serializeConstraint(BooleanExpr theConstraint) {
        StringBuffer aBuffer = new StringBuffer();

        // TODO: complete this mapping for other boolean expr types....like, any, all, StringCompare
        if (theConstraint instanceof ValueCompare) {
            ValueCompare aCompare = (ValueCompare) theConstraint;

            if (aCompare.getLeftArg() instanceof Null || aCompare.getRightArg() instanceof Null) {
                ValueExpr aExpr = aCompare.getLeftArg() instanceof Null ? aCompare.getRightArg() : aCompare.getLeftArg();

				if (aExpr instanceof Lang) {
					// lang(foo) = null is a special case.  lang(foo) that doesnt have a lang in serql is a null
					// in sparql, its an empty string.
					aBuffer.append(serializeValueExpr(aExpr)).append(op2String(aCompare)).append(" \"\"");
				}
				else {
					if (aCompare.getOperator() == ValueCompare.EQ) {
						aBuffer.append(SPARQL_NOT);
					}

                	aBuffer.append(SPARQL_OP_BOUND).append("(").append(serializeValueExpr(aExpr)).append(")");
				}
            }
            else {
                aBuffer.append(serializeValueExpr(aCompare.getLeftArg()));

				aBuffer.append(op2String(aCompare));

				aBuffer.append(serializeValueExpr(aCompare.getRightArg()));
            }
        }
        else if (theConstraint instanceof IsBNode) {
            IsBNode aBNodeCompare = (IsBNode) theConstraint;

            List<Var> aList = new ArrayList<Var>();
            aBNodeCompare.getVariables(aList);

            for (Var aVar : aList) {
                aBuffer.append(SPARQL_OP_ISBLANK + "(" + SPARQL_VARIABLE).append(aVar.getName()).append(") ");
            }
        }
        else if (theConstraint instanceof IsResource) {
            IsResource aIsResourceCompare = (IsResource) theConstraint;

            List<Var> aList = new ArrayList<Var>();
            aIsResourceCompare.getVariables(aList);

            for (Var aVar : aList) {
                aBuffer.append("(" + SPARQL_OP_ISBLANK + "(" + SPARQL_VARIABLE).append(aVar.getName()).append(")  || " + SPARQL_OP_ISURI + "(" + SPARQL_VARIABLE).append(aVar.getName()).append("))");
            }
        }
        else if (theConstraint instanceof IsLiteral) {
            IsLiteral aIsLiteralCompare = (IsLiteral) theConstraint;

            List<Var> aList = new ArrayList<Var>();
            aIsLiteralCompare.getVariables(aList);

            for (Var aVar : aList) {
                aBuffer.append(SPARQL_OP_ISLITERAL + "(" + SPARQL_VARIABLE).append(aVar.getName()).append(") ");
            }
        }
        else if (theConstraint instanceof IsURI) {
            IsURI aIsURICompare = (IsURI) theConstraint;

            List<Var> aList = new ArrayList<Var>();
            aIsURICompare.getVariables(aList);

            for (Var aVar : aList) {
                aBuffer.append(SPARQL_OP_ISURI + "(" + SPARQL_VARIABLE).append(aVar.getName()).append(") ");
            }
        }
        else if (theConstraint instanceof And) {
            And aAnd = (And) theConstraint;

            aBuffer.append(serializeConstraint(aAnd.getLeftArg())).append(" " + SPARQL_AND + " ").append(serializeConstraint(aAnd.getRightArg()));
        }
        else if (theConstraint instanceof Or) {
            Or aOr = (Or) theConstraint;

            aBuffer.append(serializeConstraint(aOr.getLeftArg())).append(" " + SPARQL_OR + " ").append(serializeConstraint(aOr.getRightArg()));
        }
        else if (theConstraint instanceof Not) {
            Not aNot = (Not) theConstraint;

            aBuffer.append(SPARQL_NOT).append(serializeConstraint(aNot.getArg()));
        }
        else {
            throw new UnsupportedOperationException("NYI: " + theConstraint.getClass());
        }

        return aBuffer.toString();
    }

	private String op2String(final ValueCompare theCompare) {
		final StringBuffer aBuffer = new StringBuffer();
		switch (theCompare.getOperator()) {
			case ValueCompare.EQ:
				aBuffer.append(" " + SPARQL_EQ + "  ");
				break;
			case ValueCompare.GE:
				aBuffer.append(" " + SPARQL_GE + " ");
				break;
			case ValueCompare.GT:
				aBuffer.append(" " + SPARQL_GT + " ");
				break;
			case ValueCompare.LE:
				aBuffer.append(" " + SPARQL_LE + " ");
				break;
			case ValueCompare.LT:
				aBuffer.append(" " + SPARQL_LT + " ");
				break;
			case ValueCompare.NE:
				aBuffer.append(" " + SPARQL_NE + " ");
				break;
		}

		return aBuffer.toString();
	}

	private String serializeValueExpr(ValueExpr theValueExpr) {
        // TODO: cover all subclasses of value expression

        if (theValueExpr instanceof Var) {
            return serializeVar( (Var) theValueExpr);
        }
        else if (theValueExpr instanceof ResourceExpr || theValueExpr instanceof LiteralExpr) {
            return getSPARQLQueryString(theValueExpr.getValue());
        }
//        else if (theValueExpr instanceof Null) {
//            return SPARQL_NOT + SPARQL_BOUND + "(" + theValueExpr.get + ")";
//        }
        else if (theValueExpr instanceof Lang) {
            return SPARQL_OP_LANG + "(" + SPARQL_VARIABLE + ((Lang)theValueExpr).getVar().getName() + ")";
        }
        else if (theValueExpr instanceof Datatype) {
            return SPARQL_OP_DATATYPE + "(" + SPARQL_VARIABLE + ((Datatype)theValueExpr).getVar().getName() + ")";
        }

        throw new UnsupportedOperationException("NYI: " + theValueExpr.getClass());
    }

    private String serializeVar(Var theVariable) {
        StringBuffer aBuffer = new StringBuffer();

        if (theVariable.getValue() != null) {
            aBuffer.append(getSPARQLQueryString(theVariable.getValue()));
        }
        else {
            aBuffer.append(SPARQL_VARIABLE).append(theVariable.getName());
        }

        return aBuffer.toString();
    }

    public static String getSPARQLQueryString(Value theValue) {
        StringBuffer aBuffer = new StringBuffer();

        if (theValue instanceof org.openrdf.model.URI) {
            org.openrdf.model.URI aURI = (org.openrdf.model.URI) theValue;
            aBuffer.append("<").append(aURI.getURI()).append(">");
        }
        else if (theValue instanceof BNode) {
            aBuffer.append("_:").append(((BNode)theValue).getID());
        }
        else if (theValue instanceof Literal) {
            Literal aLit = (Literal)theValue;
            aBuffer.append("\"").append(escape(aLit.getLabel())).append("\"").append(aLit.getLanguage() != null ? "@" + aLit.getLanguage() : "");
            if (aLit.getDatatype() != null) {
                aBuffer.append("^^<").append(aLit.getDatatype().toString()).append(">");
            }
        }


        return aBuffer.toString();
    }

    private static String escape(String theString) {
        theString = theString.replaceAll("\"", "\\\\\"");

        return theString;
    }
}

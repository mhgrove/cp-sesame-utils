// Copyright (c) 2005 - 2009, Clark & Parsia, LLC. <http://www.clarkparsia.com>

package com.clarkparsia.sesame.utils.query;

import java.util.Iterator;

import org.openrdf.sesame.sail.query.SelectQuery;
import org.openrdf.sesame.sail.query.ProjectionElem;
import org.openrdf.sesame.sail.query.PathExpression;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.Var;
import org.openrdf.sesame.sail.query.BooleanExpr;
import org.openrdf.sesame.sail.query.And;
import org.openrdf.sesame.sail.query.BooleanConstant;
import org.openrdf.sesame.sail.query.CompareAll;
import org.openrdf.sesame.sail.query.ValueCompare;
import org.openrdf.sesame.sail.query.CompareAny;
import org.openrdf.sesame.sail.query.Exists;
import org.openrdf.sesame.sail.query.In;
import org.openrdf.sesame.sail.query.IsBNode;
import org.openrdf.sesame.sail.query.IsLiteral;
import org.openrdf.sesame.sail.query.IsResource;
import org.openrdf.sesame.sail.query.IsURI;
import org.openrdf.sesame.sail.query.Like;
import org.openrdf.sesame.sail.query.Not;
import org.openrdf.sesame.sail.query.Or;
import org.openrdf.sesame.sail.query.StringCompare;
import org.openrdf.sesame.sail.query.ValueExpr;
import org.openrdf.sesame.sail.query.Datatype;
import org.openrdf.sesame.sail.query.Label;
import org.openrdf.sesame.sail.query.LiteralExpr;
import org.openrdf.sesame.sail.query.LocalName;
import org.openrdf.sesame.sail.query.MathExpr;
import org.openrdf.sesame.sail.query.Namespace;
import org.openrdf.sesame.sail.query.Null;
import org.openrdf.sesame.sail.query.ResourceExpr;
import org.openrdf.sesame.sail.query.Query;
import org.openrdf.sesame.sail.query.ConstructQuery;
import org.openrdf.sesame.sail.query.SetOperator;
import org.openrdf.sesame.sail.query.Intersect;
import org.openrdf.sesame.sail.query.Minus;
import org.openrdf.sesame.sail.query.Union;
import org.openrdf.sesame.sail.query.Lang;
import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.io.IOUtil;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 16, 2007 11:34:15 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class SERQLQueryRenderer implements QueryRenderer {

    public String render(Query theQuery) {
        if (theQuery instanceof SelectQuery) {
            return renderSelectQuery( (SelectQuery) theQuery);
        }
        else if (theQuery instanceof ConstructQuery) {
            return renderConstructQuery( (ConstructQuery) theQuery);
        }
        else if (theQuery instanceof SetOperator) {
            return renderSetOperator(theQuery);
        }
        else throw new IllegalArgumentException("NYI: can't render this query type");
    }

    private String renderSetOperator(Query theQuery) {
        SetOperator aOpQuery = (SetOperator) theQuery;

        String aLeftQuery = render(aOpQuery.getLeftArg());
        String aRightQuery = render(aOpQuery.getRightArg());

        String aOp = null;
        if (aOpQuery instanceof Union) {
            aOp = "union";
        }
        else if (aOpQuery instanceof Intersect) {
            aOp = "intersect";
        }
        else if (aOpQuery instanceof Minus) {
            aOp = "minus";
        }

        if (aOp == null) {
            throw new IllegalArgumentException("NYI: can't render this query type");
        }

        return aLeftQuery + "\n" + aOp + "\n" + aRightQuery;
    }

    private String renderConstructQuery(ConstructQuery theQuery) {
        StringBuffer aQuery = new StringBuffer();

        aQuery.append("construct ");

        if (theQuery.isDistinct()) {
            aQuery.append(" distinct ");
        }

        TriplePattern[] aTriples = theQuery.getProjection();
        for (int i = 0; i < aTriples.length; i++) {
            aQuery.append(renderTriplePattern(aTriples[i]));

            if (i < aTriples.length - 1)
                aQuery.append(", ");
        }

        aQuery.append(IOUtil.ENDL);

        aQuery.append("from").append(IOUtil.ENDL);

        aQuery.append(renderGraphPattern(theQuery.getGraphPattern()));

        aQuery.append(IOUtil.ENDL);

        if (theQuery.hasLimit()) {
            aQuery.append(" limit ").append(theQuery.getLimit());
        }

        if (theQuery.hasOffset()) {
            aQuery.append(" offset ").append(theQuery.getOffset());
        }

        return aQuery.toString();
    }

    private String renderSelectQuery(SelectQuery theQuery) {
        StringBuffer aQuery = new StringBuffer();

        aQuery.append("select ");

        if (theQuery.isDistinct()) {
            aQuery.append(" distinct ");
        }

        ProjectionElem[] aSelectVars = theQuery.getProjection();
        for (int i = 0; i < aSelectVars.length; i++) {

			if (aSelectVars[i].getAlias() != null) {
				aQuery.append(renderValueExpr(aSelectVars[i].getValueExpr())).append(" as \"").append(aSelectVars[i].getAlias()).append("\"");
			}
			else {
            	aQuery.append(aSelectVars[i].getName());
			}

            if (i < aSelectVars.length - 1)
                aQuery.append(", ");
        }

        aQuery.append(IOUtil.ENDL);

        aQuery.append("from").append(IOUtil.ENDL);

        aQuery.append(renderGraphPattern(theQuery.getGraphPattern()));

        aQuery.append(IOUtil.ENDL);

        if (theQuery.hasLimit()) {
            aQuery.append(" limit ").append(theQuery.getLimit());
        }

        if (theQuery.hasOffset()) {
            aQuery.append(" offset ").append(theQuery.getOffset());
        }

        return aQuery.toString();
    }

    private String renderPathExpression(PathExpression theExpr) {
        if (theExpr instanceof TriplePattern)
            return renderTriplePattern( (TriplePattern) theExpr);
        else if (theExpr instanceof GraphPattern)
            return renderGraphPattern( (GraphPattern) theExpr);
        else throw new IllegalArgumentException();
    }

    private String renderGraphPattern(GraphPattern thePattern) {
        StringBuffer aBuffer = new StringBuffer();

        Iterator aPathIter = thePattern.getPathExpressions().iterator();
        while (aPathIter.hasNext()) {
            PathExpression aExpr = (PathExpression) aPathIter.next();

            aBuffer.append(renderPathExpression(aExpr));

            if (aPathIter.hasNext())
                aBuffer.append(",").append(IOUtil.ENDL);
        }

        Iterator aOptionalIter = thePattern.getOptionals().iterator();

        if (aOptionalIter.hasNext())
            aBuffer.append(",").append(IOUtil.ENDL);

        while (aOptionalIter.hasNext()) {
            PathExpression aExpr = (PathExpression) aOptionalIter.next();

            aBuffer.append("[").append(renderPathExpression(aExpr)).append("]");

            if (aOptionalIter.hasNext())
                aBuffer.append(",").append(IOUtil.ENDL);
        }

        if (thePattern.getRootConstraint() != null) {
            aBuffer.append(IOUtil.ENDL).append("where").append(IOUtil.ENDL);

            aBuffer.append(renderConstraint(thePattern.getRootConstraint()));
        }

        return aBuffer.toString();
    }

    private String renderTriplePattern(TriplePattern theExpr) {
        StringBuffer aBuffer = new StringBuffer();

        aBuffer.append("{").append(renderVar(theExpr.getSubjectVar())).append("} ").
                append(renderVar(theExpr.getPredicateVar())).
                append(" {").append(renderVar(theExpr.getObjectVar())).append("}");

        return aBuffer.toString();
    }

    private String renderVar(Var theVar) {
        if (theVar.getValue() != null)
            return SesameQueryUtils.getQueryString(theVar.getValue());
        else return theVar.getName();
    }

    private String renderConstraint(BooleanExpr theExpr) {
        StringBuffer aRender = new StringBuffer("(");

        if (theExpr instanceof And) {
            And aAnd = (And) theExpr;
            aRender.append(renderConstraint(aAnd.getLeftArg())).append(" and ").append(renderConstraint(aAnd.getRightArg()));
        }
        else if (theExpr instanceof BooleanConstant) {
            if (theExpr.equals(BooleanConstant.TRUE))
                aRender.append("true");
            else aRender.append("false");
        }
        else if (theExpr instanceof CompareAll) {
            CompareAll aAll = (CompareAll) theExpr;
            aRender.append(" all ").append(aAll.getLeftArg()).append(operator2string(aAll.getOperator())).append(render(aAll.getRightArg())).append(")");
        }
        else if (theExpr instanceof CompareAny) {
            CompareAny aAny = (CompareAny) theExpr;
            aRender.append(" any ").append(aAny.getLeftArg()).append(operator2string(aAny.getOperator())).append(render(aAny.getRightArg())).append(")");
        }
        else if (theExpr instanceof Exists) {
            Exists aExists = (Exists) theExpr;
            aRender.append(" exists (").append(render(aExists.getArg())).append(")");
        }
        else if (theExpr instanceof In) {
            In aIn = (In) theExpr;
            aRender.append(aIn.getLeftArg()).append(" in (").append(render(aIn.getRightArg())).append(")");
        }
        else if (theExpr instanceof IsBNode) {
            IsBNode aIsBNode = (IsBNode) theExpr;

            aRender.append(aIsBNode.toString());
        }
        else if (theExpr instanceof IsLiteral) {
            IsLiteral aIsLiteral = (IsLiteral) theExpr;

            aRender.append(aIsLiteral.toString());
        }
        else if (theExpr instanceof IsResource) {
            IsResource aIsResource = (IsResource) theExpr;

            aRender.append(aIsResource.toString());
        }
        else if (theExpr instanceof IsURI) {
            IsURI aIsURI = (IsURI) theExpr;

            aRender.append(aIsURI.toString());
        }
        else if (theExpr instanceof Like) {
            Like aLike = (Like) theExpr;
            aRender.append(aLike.toString());
        }
        else if (theExpr instanceof Not) {
            Not aNot = (Not) theExpr;
            aRender.append(" not ").append(renderConstraint(aNot.getArg()));
        }
        else if (theExpr instanceof Or) {
            Or aOr = (Or) theExpr;
            aRender.append(renderConstraint(aOr.getLeftArg())).append(" or ").append(renderConstraint(aOr.getRightArg()));
        }
        else if (theExpr instanceof StringCompare) {
            aRender.append(theExpr.toString());
        }
        else if (theExpr instanceof ValueCompare) {
            ValueCompare aValCompare = (ValueCompare) theExpr;
            aRender.append(renderValueExpr(aValCompare.getLeftArg())).
                    append(" ").
                    append(operator2string(aValCompare.getOperator())).
                    append(" ").
                    append(renderValueExpr(aValCompare.getRightArg()));
        }
        else aRender.append(theExpr.toString());

        aRender.append(")");

        return aRender.toString();
    }

    private String renderValueExpr(ValueExpr theValueExpr) {
        StringBuffer aBuffer = new StringBuffer();

        if (theValueExpr instanceof Datatype) {
            Datatype aDt = (Datatype) theValueExpr;
            aBuffer.append("datatype(").append(renderVar(aDt.getVar())).append(")");
        }
        else if (theValueExpr instanceof Label) {
            Label aLabel = (Label) theValueExpr;
            aBuffer.append("label(").append(renderVar(aLabel.getVar())).append(")");
        }
        else if (theValueExpr instanceof LiteralExpr) {
            aBuffer.append(SesameQueryUtils.getQueryString(theValueExpr.getValue()));
        }
        else if (theValueExpr instanceof LocalName) {
            LocalName aLocalName = (LocalName) theValueExpr;
            aBuffer.append("localName(").append(renderVar(aLocalName.getVar())).append(")");
        }
        else if (theValueExpr instanceof MathExpr) {
            MathExpr aMath = (MathExpr) theValueExpr;
            aBuffer.append(renderValueExpr(aMath.getLeftArg()));

            switch (aMath.getOperator()) {
                case MathExpr.DIVIDE:
                    aBuffer.append(" / ");
                    break;
                case MathExpr.MULTIPLY:
                    aBuffer.append(" * ");
                    break;
                case MathExpr.PLUS:
                    aBuffer.append(" + ");
                    break;
                case MathExpr.REMAINDER:
                    aBuffer.append(" % ");
                    break;
                case MathExpr.SUBTRACT:
                    aBuffer.append(" - ");
                    break;
            }

            aBuffer.append(renderValueExpr(aMath.getRightArg()));
        }
        else if (theValueExpr instanceof Namespace) {
            Namespace aNamespace = (Namespace) theValueExpr;
            aBuffer.append("namespace(").append(renderVar(aNamespace.getVar())).append(")");
        }
        else if (theValueExpr instanceof Null) {
            //Null aNull = (Null) theValueExpr;
            aBuffer.append("null");
        }
        else if (theValueExpr instanceof ResourceExpr) {
            aBuffer.append(SesameQueryUtils.getQueryString(theValueExpr.getValue()));
        }
        else if (theValueExpr instanceof Var) {
            aBuffer.append(renderVar( (Var) theValueExpr));
        }
		else if (theValueExpr instanceof Lang) {
			Lang aLang = (Lang) theValueExpr;
			aBuffer.append("lang(").append(renderVar(aLang.getVar())).append(")");
		}

        return aBuffer.toString();
    }

	public static String operator2string(int op) {
		switch (op) {
			case ValueCompare.EQ: return "=";
			case ValueCompare.NE: return "!=";
			case ValueCompare.LT: return "<";
			case ValueCompare.LE: return "<=";
			case ValueCompare.GE: return ">=";
			case ValueCompare.GT: return ">";
			default: throw new IllegalArgumentException("Illegal operator value: " + op);
		}
	}
}

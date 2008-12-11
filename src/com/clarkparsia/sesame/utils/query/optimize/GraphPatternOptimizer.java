package com.clarkparsia.sesame.utils.query.optimize;

import org.openrdf.sesame.sail.query.GraphPattern;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Jan 29, 2008 12:18:51 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public interface GraphPatternOptimizer {
    public GraphPattern optimize(GraphPattern thePattern) throws Exception;
}

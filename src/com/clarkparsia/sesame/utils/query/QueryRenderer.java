package com.clarkparsia.sesame.utils.query;

import org.openrdf.sesame.sail.query.Query;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 16, 2007 11:33:43 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public interface QueryRenderer {
    public String render(Query theQuery);
}

package com.clarkparsia.sesame.sail.convert;

import org.openrdf.rio.StatementHandler;

import java.net.URL;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 23, 2008 9:21:53 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public interface Converter {
    public void convert(URL theFile, StatementHandler theHandler) throws Exception;
}

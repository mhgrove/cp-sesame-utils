package com.clarkparsia.sesame.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2009 3:09:50 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfClass {
	public String value();
}

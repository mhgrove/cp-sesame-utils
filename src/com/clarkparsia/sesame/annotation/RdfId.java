package com.clarkparsia.sesame.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2009 3:10:13 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfId {
	String namespace();
}

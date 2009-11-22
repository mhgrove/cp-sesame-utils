package com.clarkparsia.sesame.annotation;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2009 4:23:53 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class InvalidRdfException extends Exception {

	public InvalidRdfException(final String theMessage) {
		super(theMessage);
	}

	public InvalidRdfException(final Throwable theCause) {
		super(theCause);
	}
}

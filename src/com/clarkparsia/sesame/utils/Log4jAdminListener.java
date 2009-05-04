package com.clarkparsia.sesame.utils;

import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.model.Statement;
import org.apache.log4j.Logger;

/**
 * Title: Log4jAdminListener<br/>
 * Description: Implementation of a Sesame {@link AdminListener} which routes all the admin messages to a Log4j logger instance.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: May 1, 2009 12:32:36 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class Log4jAdminListener implements AdminListener {
	private Logger mLogger;

	public Log4jAdminListener(Logger theLogger) {
		mLogger = theLogger;
	}

	public void transactionStart() {
		mLogger.info("Transaction Started");
	}

	public void transactionEnd() {
		mLogger.info("Transaction Ended");
	}

	public void status(String theMsg, int theLine, int theCol) {
		mLogger.info("Status [" + theLine + ":" + theCol + "]: " + theMsg); 
	}

	public void notification(String theMsg, int theLine, int theCol, Statement theStatement) {
		mLogger.info("Notification [" + theLine + ":" + theCol + "]: " + theMsg + " -- " + theStatement);
	}

	public void warning(String theMsg, int theLine, int theCol, Statement theStatement) {
		mLogger.warn("Warning [" + theLine + ":" + theCol + "]: " + theMsg + " -- " + theStatement);
	}

	public void error(String theMsg, int theLine, int theCol, Statement theStatement) {
		mLogger.error("Error [" + theLine + ":" + theCol + "]: " + theMsg + " -- " + theStatement);
	}
}

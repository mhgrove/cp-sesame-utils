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

package com.clarkparsia.sesame.utils;

import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.model.Statement;
import org.apache.log4j.Logger;

/**
 * <p>Implementation of a Sesame {@link AdminListener} which routes all the admin messages to a Log4j logger instance.</p>
 *
 * @author Michael Grove
 * @since 1.0
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

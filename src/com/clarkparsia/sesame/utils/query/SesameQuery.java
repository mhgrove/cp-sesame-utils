package com.clarkparsia.sesame.utils.query;

import org.openrdf.sesame.constants.QueryLanguage;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Apr 25, 2009 5:38:36 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class SesameQuery {
	private QueryLanguage mLang;
	private String mQuery;

	public SesameQuery(QueryLanguage theLang, String theQuery) {
		mLang = theLang;
		mQuery = theQuery;
	}

	public QueryLanguage getLanguage() {
		return mLang;
	}

	public void setLanguage(QueryLanguage theLang) {
		mLang = theLang;
	}

	public String getQueryString() {
		return mQuery;
	}

	public void setQueryString(String theQuery) {
		mQuery = theQuery;
	}
}

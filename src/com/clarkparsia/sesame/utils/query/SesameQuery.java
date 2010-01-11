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

package com.clarkparsia.sesame.utils.query;

import org.openrdf.sesame.constants.QueryLanguage;

/**
 * <p>Utility query object which encapsulates both the query string and the query language.</p>
 *
 * @author Michael Grove
 * @since 1.0
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

	public static SesameQuery serql(String theQuery) {
		return new SesameQuery(QueryLanguage.SERQL, theQuery);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return getQueryString();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return 37 * (getQueryString().hashCode() + getLanguage().hashCode());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof SesameQuery && ((SesameQuery) theObj).getQueryString().equals(getQueryString()) &&
			   ((SesameQuery) theObj).getLanguage().equals(getLanguage());
	}
}

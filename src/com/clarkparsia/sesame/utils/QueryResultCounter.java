
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

import java.io.IOException;

import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryErrorType;
import org.openrdf.sesame.query.TableQueryResultListener;
/**
 * <p>Implementation of a TableQueryResultListener which counts the total number of query results.</p>
 *
 * @since 1.0
 */
public class QueryResultCounter implements TableQueryResultListener {
		private int count;
		private String message = "";
		public QueryResultCounter() {
			count = 0;
			
			System.out.print( "Counting..." );
		}

		public int getCount() {
			return count;
		}

		public void startTableQueryResult() throws IOException {
			count = 0;			
		}

		public void startTableQueryResult(String[] columnHeaders)
				throws IOException {
			startTableQueryResult();
		}

		public void endTableQueryResult() throws IOException {
			System.out.println( "done" );
		}

		public void startTuple() throws IOException {
			count++;
			
		}

		public void endTuple() throws IOException {
		}

		public void tupleValue(Value value) throws IOException {
		}

		public void error(QueryErrorType errType, String msg)
				throws IOException {
			System.err.println(msg);
			throw new RuntimeException(msg);
		}
	}
/**
 * 
 */
package com.clarkparsia.sesame.utils;

import java.io.IOException;

import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryErrorType;
import org.openrdf.sesame.query.TableQueryResultListener;

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
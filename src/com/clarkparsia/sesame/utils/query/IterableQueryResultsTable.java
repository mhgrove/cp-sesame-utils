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

import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * <p>Adapter class to allow a SesameQueryResults table to be used as an Iterable set of {@link Binding}s</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class IterableQueryResultsTable implements Iterable<Binding> {

    private QueryResultsTable mTable;

    IterableQueryResultsTable(QueryResultsTable theTable) {
        mTable = theTable;
    }

	public int size() {
		return mTable.getRowCount();
	}

    public static IterableQueryResultsTable iterable(QueryResultsTable theTable) {
        return new IterableQueryResultsTable(theTable);
    }

    public Iterator<Binding> iterator() {
        return new TableIter();
    }

    private class TableIter implements Iterator<Binding> {
        private int mIndex = 0;

        public boolean hasNext() {
            return mIndex < mTable.getRowCount();
        }

        public Binding next() {
            if (!hasNext()) {
                throw new ArrayIndexOutOfBoundsException();
            }

            Binding aMap = new Binding();

            for (String aName : mTable.getColumnNames()) {
                aMap.put(aName,
                         mTable.getValue(mIndex, SesameQueryUtils.indexOfVariable(mTable, aName)));
            }

            mIndex++;

            return aMap;
        }

        public void remove() {
            // no-op
        }
    }
}

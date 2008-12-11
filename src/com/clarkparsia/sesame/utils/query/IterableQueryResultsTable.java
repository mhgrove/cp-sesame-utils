package com.clarkparsia.sesame.utils.query;

import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2008 7:59:09 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class IterableQueryResultsTable implements Iterable<Binding> {

    private QueryResultsTable mTable;

    IterableQueryResultsTable(QueryResultsTable theTable) {
        mTable = theTable;
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

package com.clarkparsia.sesame.repository.convert;

import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sail.NamespaceIterator;
import org.openrdf.sesame.sail.SailInitializationException;
import org.openrdf.sesame.sail.RdfRepository;
import org.openrdf.sesame.sail.SailUpdateException;
import org.openrdf.sesame.sail.SailChangedListener;
import org.openrdf.sesame.sail.util.EmptyStatementIterator;
import org.openrdf.sesame.sail.util.SingleStatementIterator;
import org.openrdf.sesame.sail.util.SailChangedEventImpl;
import org.openrdf.sesame.sail.query.Query;
import org.openrdf.sesame.sailimpl.memory.MemNamespaceIterator;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.vocabulary.XmlSchema;

import java.util.Map;
import java.util.Timer;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.io.IOException;

import com.clarkparsia.sesame.utils.BuildRepositoryStatementHandler;


/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 22, 2008 7:25:36 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ConversionBaseSail implements RdfRepository {
    private static final int DEFAULT_UPDATE_TIME = 1000;

    public static final URI UPDATE_PREDICATE = new ValueFactoryImpl().createURI("http://www.clarkparsia.com/sesame/hasUpdates");

    public static final String KEY_UPDATE_TIME = "update.time";
    public static final String KEY_UPDATE_FILE = "update.file";
    public static final String KEY_CONVERTER_CLASS = "converter.class";

    private ValueFactory mValueFactory = new ValueFactoryImpl();
    private RdfRepository mSource;
    private Timer mUpdateTimer;

    private Lock mLock = new ReentrantLock();

    private URL mFileURL;
    private long mLastUpdated = -1;
    private Converter mConverter;
    private boolean mHasUpdates = false;

    private final Set<SailChangedListener> mListeners = new HashSet<SailChangedListener>();
    private SailChangedEventImpl mCurrentEvent;
    private boolean mInTransaction = false;

    public ConversionBaseSail() {
        mUpdateTimer = new Timer(true /* isDaemon */);

        mSource = new org.openrdf.sesame.sailimpl.memory.RdfRepository();
    }

    public ValueFactory getValueFactory() {
        return mValueFactory;
    }

    public StatementIterator getStatements(Resource theResource, URI theURI, Value theValue) {
        if (mSource == null) {
            return new EmptyStatementIterator();
        }
        else {
            if (theURI != null && theURI.equals(UPDATE_PREDICATE)) {
                try {
                    return new SingleStatementIterator(getValueFactory().createURI("http://www.clarkparsia.com/sesame/sail/ConversionBaseSail"),
                                                       UPDATE_PREDICATE,
                                                       getValueFactory().createLiteral(""+ mHasUpdates,
                                                                                       getValueFactory().createURI(XmlSchema.BOOLEAN)));
                }
                finally {
                    mHasUpdates = false;
                }
            }

            try {
                mLock.lock();

                return mSource.getStatements(theResource, theURI, theValue);
            }
            finally {
                mLock.unlock();
            }
        }
    }

    public boolean hasStatement(Resource theResource, URI theURI, Value theValue) {
        if (mSource == null) {
            return false;
        }
        else {
            try {
                mLock.lock();

                return mSource.hasStatement(theResource, theURI, theValue);
            }
            finally {
                mLock.unlock();
            }
        }
    }

    public Query optimizeQuery(Query theQuery) {
        if (mSource == null) {
            return theQuery;
        }
        else {
            return mSource.optimizeQuery(theQuery);
        }
    }

    public NamespaceIterator getNamespaces() {
        if (mSource == null) {
            return new MemNamespaceIterator(new ArrayList());
        }
        else {
            return mSource.getNamespaces();
        }
    }

    public void initialize(Map theMap) throws SailInitializationException {
        validateInitializationMap(theMap, Arrays.asList(KEY_UPDATE_FILE, KEY_CONVERTER_CLASS));

        int aUpdateTime = DEFAULT_UPDATE_TIME;

        if (theMap.containsKey(KEY_UPDATE_TIME)) {
            aUpdateTime = Integer.parseInt(theMap.get(KEY_UPDATE_TIME).toString());
        }


        try {
            mFileURL = new URL(theMap.get(KEY_UPDATE_FILE).toString());
        }
        catch (MalformedURLException e) {
            throw new SailInitializationException(e);
        }

        try {
            mConverter = (Converter) Class.forName(theMap.get(KEY_CONVERTER_CLASS).toString()).newInstance();
        }
        catch (Exception ex) {
            throw new SailInitializationException(ex);
        }

        performUpdate();

        // the initial load should not count as an update.
        mHasUpdates = false;

        mUpdateTimer.schedule(new UpdateTimerTask(), 0, aUpdateTime);
    }

    private void validateInitializationMap(Map theMap, List<String> theKeys) throws SailInitializationException {
        for (String aKey : theKeys) {
            if (!theMap.containsKey(aKey)) {
                throw new SailInitializationException("Missing required configuration paramter: " + aKey);
            }
        }
    }

    public void shutDown() {
        mUpdateTimer.cancel();

        if (mSource != null) {
            mSource.shutDown();
        }
    }

    private void performUpdate() {
System.err.println("Updating");

        mLastUpdated = System.currentTimeMillis();

        RdfRepository aNewRepo = new org.openrdf.sesame.sailimpl.memory.RdfRepository();
        aNewRepo.startTransaction();

        try {
            mConverter.convert(mFileURL, new BuildRepositoryStatementHandler(aNewRepo));

            aNewRepo.commitTransaction();
        }
        catch (Exception ex) {
            // TODO: do some logging!
            System.err.println("Update failed");
            ex.printStackTrace();
            return;
        }

        try {
            mLock.lock();

            mSource = aNewRepo;

            mHasUpdates = true;
        }
        finally {
            mLock.unlock();
        }
    }

    protected boolean isDirty() {
        URLConnection aConn = null;

        try {
            aConn = mFileURL.openConnection();

            // TODO: what about time zone normalization?  i think this will always be wrong if we're hitting a resource
            // in a different time zone, unless java converts the modified time to the local time, or if HTTP does.
            return mLastUpdated < aConn.getLastModified();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            // release the connection?
            aConn = null;
        }
    }

    public void startTransaction() {
        mInTransaction = true;
        mCurrentEvent = new SailChangedEventImpl();
    }

    public void commitTransaction() {
        mInTransaction = false;

        notifySailChanged();

        mCurrentEvent = null;
    }

    public boolean transactionStarted() {
        return mInTransaction;
    }

    public void addStatement(Resource theResource, URI theURI, Value theValue) throws SailUpdateException {
		if (!transactionStarted()) {
			throw new SailUpdateException("no transaction started.");
		}

        // todo: log this
        System.err.println("Ignoring ADD Statement, cannot add to a conversion sail");
    }

    public int removeStatements(Resource theResource, URI theURI, Value theValue) throws SailUpdateException {
		if (!transactionStarted()) {
			throw new SailUpdateException("no transaction started.");
		}

        // todo: log this
        System.err.println("Ignoring REMOVE Statement, cannot remove from a conversion sail");

        return 0;
    }

    public void clearRepository() throws SailUpdateException {
		if (!transactionStarted()) {
			throw new SailUpdateException("no transaction started.");
		}

        mInTransaction = false;
        mSource = null;
        mLastUpdated = -1;
        mHasUpdates = false;
    }

    public void changeNamespacePrefix(String theNamespace, String thePrefix) throws SailUpdateException {
        if (mSource != null) {
            mSource.changeNamespacePrefix(theNamespace, thePrefix);
        }
    }

    public void addListener(SailChangedListener theSailChangedListener) {
        synchronized (mListeners) {
            mListeners.add(theSailChangedListener);
        }
    }

    public void removeListener(SailChangedListener theSailChangedListener) {
        synchronized (mListeners) {
            mListeners.remove(theSailChangedListener);
        }
    }

    private void notifySailChanged() {
        if (!mCurrentEvent.sailChanged()) {
            return;
        }

        synchronized (mListeners) {
            for (SailChangedListener aListener : mListeners) {
                aListener.sailChanged(mCurrentEvent);
            }
        }
    }

    private class UpdateTimerTask extends TimerTask {

        public void run() {
            if (isDirty()) {
                performUpdate();
            }
        }
    }
} 

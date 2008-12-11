package com.clarkparsia.sesame.sail;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sesame.sail.NamespaceIterator;
import org.openrdf.sesame.sail.RdfRepository;
import org.openrdf.sesame.sail.Sail;
import org.openrdf.sesame.sail.SailChangedListener;
import org.openrdf.sesame.sail.SailInitializationException;
import org.openrdf.sesame.sail.SailInternalException;
import org.openrdf.sesame.sail.SailUpdateException;
import org.openrdf.sesame.sail.StackedSail;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sail.query.Query;
import org.openrdf.util.log.ThreadLog;

/**
 * @author Evren Sirin
 */
public class CachingRdfRepository implements RdfRepository, StackedSail {
    public static Logger log = Logger.getLogger( CachingRdfRepository.class );

	private RdfRepository _rdfRepository;

    private ICache _cache;

	private boolean _transactionStarted = false;

	public CachingRdfRepository() {
		ThreadLog.trace("new CachingRdfRepository created");
	}

	public void setBaseSail(Sail sail) {
		if (sail instanceof RdfRepository) {
			_rdfRepository = (RdfRepository)sail;
			ThreadLog.trace("CachingRdfRepository: base sail was set");
		}
		else {
			throw new SailInternalException("base Sail should be an RdfRepository");
		}
	}

	public Sail getBaseSail() {
		return _rdfRepository;
	}

	public void initialize(Map configParams)
		throws SailInitializationException
	{
		if (_rdfRepository == null) {
			throw new SailInitializationException("Base sail was not set");
		}

        _cache = new DynamicCache();
//        _cache = new ICache() {
//            public StatementIterator getCached(Resource theSubj, URI thePred, Value theObj) {
//                return null;
//            }
//
//            public StatementIterator cache(Resource theSubj, URI thePred, Value theObj, StatementIterator theIter) {
//                return theIter;
//            }
//
//            public Boolean contains(Resource theSubj, URI thePred, Value theObj) {
//                return null;
//            }
//
//            public void clear() {
//            }
//        };

        ThreadLog.trace("CachingRdfRepository initialized");
	}

	public void shutDown() {
        _cache.clear();
		_rdfRepository.shutDown();
	}

	public void startTransaction() {
		_transactionStarted = true;
        
        _rdfRepository.startTransaction();
	}

	public void commitTransaction() {
		if (!_transactionStarted) {
			throw new SailInternalException("No transaction started");
		}

		_rdfRepository.commitTransaction();
	}

	public boolean transactionStarted() {
		return _transactionStarted;
	}

	public void addStatement(Resource subj, URI pred, Value obj)
		throws SailUpdateException
	{
		if (!_transactionStarted) {
			throw new SailUpdateException("No transaction started");
		}

		_rdfRepository.addStatement(subj, pred, obj);
	}

	public int removeStatements(Resource subj, URI pred, Value obj) 
		throws SailUpdateException
	{
		if (!_transactionStarted) {
			throw new SailUpdateException("No transaction started");
		}
		return _rdfRepository.removeStatements(subj, pred, obj);
	}

	public void clearRepository() 
		throws SailUpdateException
	{
		if (!_transactionStarted) {
			throw new SailUpdateException("No transaction started");
		}
		_rdfRepository.clearRepository();
	}

	public void changeNamespacePrefix(String namespace, String prefix)
		throws SailUpdateException
	{
		if (!_transactionStarted) {
			throw new SailUpdateException("No transaction started");
		}
		_rdfRepository.changeNamespacePrefix(namespace, prefix);
	}

	public ValueFactory getValueFactory() {
		return _rdfRepository.getValueFactory();
	}

	public StatementIterator getStatements(Resource subj, URI pred, Value obj) {
		StatementIterator result = _cache.getCached( subj, pred, obj );

		if( result == null ) {
            if( log.isDebugEnabled() )
                log.debug( "Cache miss " + subj + " " + pred + " " + obj );            
		    result = _rdfRepository.getStatements( subj, pred, obj );
            // TODO create an iterator that caches results as items are iterated
            result = _cache.cache( subj, pred, obj, result );
        }
        else if( log.isDebugEnabled() )
            log.debug( "Cache HIT " + subj + " " + pred + " " + obj );

        
		return result;
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj) {
		boolean result = false;

		Boolean cachedResult = _cache.contains( subj, pred, obj );
        if( cachedResult != null )
            result = cachedResult.booleanValue();
        else
            result = _rdfRepository.hasStatement( subj, pred, obj );

		return result;
	}

	public Query optimizeQuery(Query qc) {		
		return _rdfRepository.optimizeQuery(qc);
	}

	public NamespaceIterator getNamespaces() {
		return _rdfRepository.getNamespaces();
	}
	
	/* (non-Javadoc)
	 * @see org.openrdf.sesame.sail.RdfRepository#addListener(org.openrdf.sesame.sail.SailChangedListener)
	 */
	public void addListener(SailChangedListener listener) {
		_rdfRepository.addListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.openrdf.sesame.sail.RdfRepository#removeListener(org.openrdf.sesame.sail.SailChangedListener)
	 */
	public void removeListener(SailChangedListener listener) {
		_rdfRepository.removeListener(listener);
	}
}

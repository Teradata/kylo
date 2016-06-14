/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;

import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;


/**
 *
 * @author Sean Felten
 */
public class JcrMetadataAccess implements MetadataAccess {
    
    private static final Logger log = LoggerFactory.getLogger(JcrMetadataAccess.class);
    
    public static final String TBA_PREFIX = "tba";
    
    private static final ThreadLocal<Session> activeSession = new ThreadLocal<Session>() {
        protected Session initialValue() {
            return null;
        }
    };

    private static final ThreadLocal<Set<Node>> checkedOutNodes = new ThreadLocal<Set<Node>>() {

    };

    @Inject
    @Named("metadataJcrRepository")
    private Repository repository;
    
    @Inject
    private TransactionManagerLookup txnLookup;

    public static Session getActiveSession() {
        return activeSession.get();
    }


    /**
     * Return all nodes that have been checked out
     */
    public static Set<Node> getCheckedoutNodes() {
        return checkedOutNodes.get();
    }


    /**
     * Check out the node and add it to the Set of checked out nodes
     * //ensureCheckout
     *
     */
    public static void checkoutNode(Node n) throws RepositoryException {
        if (JcrUtil.isVersionable(n) && (!n.isCheckedOut() || (n.isNew() && !checkedOutNodes.get().contains(n)))) {
            JcrVersionUtil.checkout(n);
            checkedOutNodes.get().add(n);
        }
    }

    /**
     * A set of Nodes that have been Checked Out. Nodes that have the mix:versionable need to be Checked Out and then Checked In when updating.
     *
     * @see com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil.setProperty() which checks out the node before applying the update
     */
    public static void checkinNodes() throws RepositoryException {
        Set<Node> checkedOutNodes = getCheckedoutNodes();
        if (checkedOutNodes != null && !checkedOutNodes.isEmpty()) {
            for (Iterator<Node> itr = checkedOutNodes.iterator(); itr.hasNext(); ) {
                Node element = itr.next();
                JcrVersionUtil.checkin(element);
                itr.remove();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.Command)
     */
    @Override
    public <R> R commit(Command<R> cmd) {
        Session session = activeSession.get();
        Set<Node> checkedOutNodes = getCheckedoutNodes();
        
        if (session == null) {
            try {

                if (checkedOutNodes == null) {
                    this.checkedOutNodes.set(new HashSet<>());
                }
                activeSession.set(this.repository.login());

                TransactionManager txnMgr = this.txnLookup.getTransactionManager();
                
                try {
                    txnMgr.begin();
                    
                    R result = cmd.execute();
                    
                    activeSession.get().save();

                    checkinNodes();

                    txnMgr.commit();
                    return result;
                } catch (RepositoryException | NotSupportedException | SystemException | HeuristicMixedException e) {
                    log.warn("Exception while execution a transactional operation - rollng back", e);
                    
                    try {
                        txnMgr.rollback();
                    } catch (SystemException se) {
                        log.error("Failed to rollback tranaction as a result of other transactional errors", se);
                    }
                    
                    activeSession.get().refresh(false);
                    // TODO Use a better exception
                    throw new RuntimeException(e);
                } catch (RollbackException | HeuristicRollbackException e) {
                    log.warn("Failed to commmit the transational operation due to a rollback", e);
                    
                    activeSession.get().refresh(false);
                    // TODO Use a better exception
                    throw new RuntimeException(e);
                } finally {
                    activeSession.get().logout();
                    activeSession.remove();
                    if(this.checkedOutNodes.get() != null) {
                        this.checkedOutNodes.get().clear();
                    }
                    this.checkedOutNodes.remove();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (RepositoryException e) {
                // TODO Use a better exception
                throw new RuntimeException(e);
            }
        } else {
            return cmd.execute();
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#read(com.thinkbiganalytics.metadata.api.Command)
     */
    @Override
    public <R> R read(Command<R> cmd) {
        Session session = activeSession.get();
        
        if (session == null) {
            try {
                activeSession.set(this.repository.login());
                
                TransactionManager txnMgr = this.txnLookup.getTransactionManager();
                
                try {
                    txnMgr.begin();
                    
                    return cmd.execute();
                } catch (SystemException | NotSupportedException e) {
                    // TODO Use a better exception
                    throw new RuntimeException(e);
                } finally {
                    try {
                        txnMgr.rollback();
                    } catch (SystemException e) {
                        log.error("Failed to rollback transaction", e);
                    }
                    
                    activeSession.get().refresh(false);
                    activeSession.get().logout();
                    activeSession.remove();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (RepositoryException e) {
                // TODO Use a better exception
                throw new RuntimeException(e);
            }
        } else {
            return cmd.execute();
        }
    }
}

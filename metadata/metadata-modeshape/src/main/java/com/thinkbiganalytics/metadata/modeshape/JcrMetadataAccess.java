/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.security.SpringAuthenticationCredentials;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;


/**
 *
 * @author Sean Felten
 */
public class JcrMetadataAccess implements MetadataAccess {

    private static final Logger log = LoggerFactory.getLogger(JcrMetadataAccess.class);

    public static final String TBA_PREFIX = "tba";

    /** Namespace for user-defined items */
    public static final String USR_PREFIX = "usr";

    private static final ThreadLocal<Session> activeSession = new ThreadLocal<Session>() {
        protected Session initialValue() {
            return null;
        }
    };

    private static final ThreadLocal<Set<Node>> checkedOutNodes = new ThreadLocal<Set<Node>>() {
        protected java.util.Set<Node> initialValue() {
            return new HashSet<>();
        };
    };

    @Inject
    @Named("metadataJcrRepository")
    private Repository repository;

    @Inject
    private TransactionManagerLookup txnLookup;

    public static Session getActiveSession() {
        Session active = activeSession.get();
        
        if (active != null) {
            return active;
        } else {
            throw new NoActiveSessionException();
        }
    }


    /**
     * Return all nodes that have been checked out
     */
    public static Set<Node> getCheckedoutNodes() {
        return checkedOutNodes.get();
    }


    /**
     * Check out the node and add it to the Set of checked out nodes
     *
     *
     */
    public static void ensureCheckoutNode(Node n) throws RepositoryException {
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
        for (Iterator<Node> itr = checkedOutNodes.iterator(); itr.hasNext(); ) {
            Node element = itr.next();
            JcrVersionUtil.checkin(element);
            itr.remove();
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.Command)
     */
    @Override
    public <R> R commit(Command<R> cmd) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SpringAuthenticationCredentials creds = new SpringAuthenticationCredentials(auth);
        
        return commit(creds, cmd);
    }

    public <R> R commit(Credentials creds, Command<R> cmd) {
        Session session = activeSession.get();
        if (session == null) {
            try {
                activeSession.set(this.repository.login(creds));

                TransactionManager txnMgr = this.txnLookup.getTransactionManager();

                try {
                    txnMgr.begin();

                    R result = cmd.execute();

                    activeSession.get().save();

                    checkinNodes();

                    txnMgr.commit();
                    return result;
                } catch (Exception e) {
                    log.warn("Exception while execution a transactional operation - rollng back", e);

                    try {
                        txnMgr.rollback();
                    } catch (SystemException se) {
                        log.error("Failed to rollback tranaction as a result of other transactional errors", se);
                    }

                    activeSession.get().refresh(false);
                    // TODO Use a better exception
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                } finally {
                    activeSession.get().logout();
                    activeSession.remove();
                    checkedOutNodes.remove();
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SpringAuthenticationCredentials creds = new SpringAuthenticationCredentials(auth);
        
        return read(creds, cmd);
    }

    public <R> R read(Credentials creds, Command<R> cmd) {
        Session session = activeSession.get();
        
        if (session == null) {
            try {
                activeSession.set(this.repository.login(creds));
                
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

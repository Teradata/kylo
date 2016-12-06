/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAccessException;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataExecutionException;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeReadOnlyPrincipal;
import com.thinkbiganalytics.metadata.modeshape.security.OverrideCredentials;
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
    
    private static final ThreadLocal<Set<Consumer<Boolean>>> postTransactionActions = new ThreadLocal<Set<Consumer<Boolean>>>() {
        protected Set<Consumer<Boolean>> initialValue() {
            return new HashSet<Consumer<Boolean>>();
        }
    };

    private static final ThreadLocal<Set<Node>> checkedOutNodes = new ThreadLocal<Set<Node>>() {
        protected java.util.Set<Node> initialValue() {
            return new HashSet<>();
        };
    };

    private static MetadataRollbackAction nullRollbackAction  = (e) -> {

    };


    private static MetadataRollbackCommand nullRollbackCommand = (e) -> {
        nullRollbackAction.execute(e);
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
        if (n.getSession().getRootNode().equals(n.getParent())) {
            return;
        } else if (JcrUtil.isVersionable(n) && (!n.isCheckedOut() || (n.isNew() && !checkedOutNodes.get().contains(n)))) {
            JcrVersionUtil.checkout(n);
            checkedOutNodes.get().add(n);
        } else {
            ensureCheckoutNode(n.getParent());
        }
    }

    /**
     * A set of Nodes that have been Checked Out. Nodes that have the mix:versionable need to be Checked Out and then Checked In when updating.
     *
     * @see com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil#setProperty(Node, String, Object) which checks out the node before applying the update
     */
    public static void checkinNodes() throws RepositoryException {
        Set<Node> checkedOutNodes = getCheckedoutNodes();
        for (Iterator<Node> itr = checkedOutNodes.iterator(); itr.hasNext(); ) {
            Node element = itr.next();
            JcrVersionUtil.checkin(element);
            itr.remove();
        }
    }
    
    public static void addPostTransactionAction(Consumer<Boolean> action) {
        postTransactionActions.get().add(action);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.MetadataCommand, java.security.Principal[])
     */
    @Override
    public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
        return commit(createCredentials(principals), cmd);
    }

    public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {

        return commit(createCredentials(principals), cmd, rollbackCmd);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(java.lang.Runnable, java.security.Principal[])
     */
    public void commit(MetadataAction action, Principal... principals) {
        commit(() -> {
            action.execute();
            return null;
        }, principals);
    }

    public void commit(MetadataAction action,  MetadataRollbackAction rollbackAction, Principal... principals) {
        commit(() -> {
            action.execute();
            return null;
        }, (e) -> {
            rollbackAction.execute(e);
        },principals);
    }
    
    public void commit(Credentials creds, MetadataAction action) {
        commit(creds, () -> {
            action.execute();
            return null;
        });
    }

    public <R> R commit(Credentials creds, MetadataCommand<R> cmd) {
        return commit(creds,cmd,nullRollbackCommand);
    }


    public <R> R commit(Credentials creds, MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd) {
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
                    performPostTransactionActions(true);
                    return result;
                } catch (Exception e) {
                    log.warn("Exception while execution a transactional operation - rolling back", e);

                    try {
                        txnMgr.rollback();
                    } catch (SystemException se) {
                        log.error("Failed to rollback tranaction as a result of other transactional errors", se);
                    }
                    
                    if (rollbackCmd != null){
                        try {
                            rollbackCmd.execute(e);
                        } catch (Exception rbe) {
                            log.error("Failed to execution rollback command", rbe);
                        }
                    }

                    activeSession.get().refresh(false);
                    performPostTransactionActions(false);

                    throw e;
                } finally {
                    activeSession.get().logout();
                    activeSession.remove();
                    postTransactionActions.remove();
                    checkedOutNodes.remove();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (RepositoryException e) {
                throw new MetadataAccessException("Failure accessing the metadata store", e);
            } catch (Exception e) {
                throw new MetadataExecutionException(e);
            }
        } else {
            try {
                return cmd.execute();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new MetadataExecutionException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#read(com.thinkbiganalytics.metadata.api.MetadataCommand, java.security.Principal[])
     */
    @Override
    public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
        return read(createCredentials(principals), cmd);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#read(java.lang.Runnable, java.security.Principal[])
     */
    public void read(MetadataAction action, Principal... principals) {
        read(() -> {
            action.execute();
            return null;
        }, principals);
    }
    
    public void read(Credentials creds, MetadataAction action) {
        read(creds, () -> {
            action.execute();
            return null;
        });
    }

    public <R> R read(Credentials creds, MetadataCommand<R> cmd) {
        Session session = activeSession.get();
        
        if (session == null) {
            try {
                activeSession.set(this.repository.login(creds));
                
                TransactionManager txnMgr = this.txnLookup.getTransactionManager();
                
                try {
                    txnMgr.begin();
                    
                    return cmd.execute();
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
            } catch (SystemException | NotSupportedException | RepositoryException e) {
                throw new MetadataAccessException("Failure accessing the metadata store", e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new MetadataExecutionException(e);
            }
        } else {
            try {
                return cmd.execute();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new MetadataExecutionException(e);
            }
        }
    }


    /**
     * Invokes all of the post-commit consumers; passing the transaction success flag to each.
     * @param success true if the transaction committed successfully, otherwise false
     */
    private void performPostTransactionActions(boolean success) {
        postTransactionActions.get().stream().forEach(action -> action.accept(success));
    }


    private Credentials createCredentials(Principal... principals) {
        Credentials creds = null;
        
        if (principals.length == 0) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            creds = new SpringAuthenticationCredentials(auth, ModeShapeReadOnlyPrincipal.INSTANCE);
        } else {
            creds = OverrideCredentials.create(Stream.concat(Stream.of(ModeShapeReadOnlyPrincipal.INSTANCE), 
                                                             Arrays.stream(principals))
                                               .collect(Collectors.toSet()));
        }
        
        return creds;
    }
}

/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAccessException;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataExecutionException;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapePrincipal;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeReadOnlyPrincipal;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeReadWritePrincipal;
import com.thinkbiganalytics.metadata.modeshape.security.OverrideCredentials;
import com.thinkbiganalytics.metadata.modeshape.security.SpringAuthenticationCredentials;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Arrays;
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


/**
 *
 */
public class JcrMetadataAccess implements MetadataAccess {

    public static final String TBA_PREFIX = "tba";
    /**
     * Namespace for user-defined items
     */
    public static final String USR_PREFIX = "usr";
    private static final Logger log = LoggerFactory.getLogger(JcrMetadataAccess.class);
    
    private static final ThreadLocal<ActiveSession> activeSession = new ThreadLocal<ActiveSession>() {
        protected ActiveSession initialValue() {
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
        }
    };

    private static MetadataRollbackAction nullRollbackAction = (e) -> {

    };


    private static MetadataRollbackCommand nullRollbackCommand = (e) -> {
        nullRollbackAction.execute(e);
    };


    @Inject
    @Named("metadataJcrRepository")
    private Repository repository;

    @Inject
    private TransactionManagerLookup txnLookup;


    public static boolean hasActiveSession() {
        return activeSession.get() != null;
    }

    public static Session getActiveSession() {
        ActiveSession active = activeSession.get();

        if (active != null) {
            return active.session;
        } else {
            throw new NoActiveSessionException();
        }
    }
    
    public static UsernamePrincipal getActiveUser() {
        ActiveSession active = activeSession.get();

        if (active != null) {
            return active.userPrincipal;
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
     */
    public static void ensureCheckoutNode(Node n) {
        try {
            if (!n.isCheckedOut() || (n.isNew() && !checkedOutNodes.get().contains(n))) {
                log.debug("***** checking out node: {}", n);
            }
            
            // Checking out an already checked-out node is a no-op.
            JcrVersionUtil.checkout(n);
            checkedOutNodes.get().add(n);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to checkout node: " + n, e);
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
        return commit(createCredentials(false, principals), cmd);
    }

    public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {

        return commit(createCredentials(false, principals), cmd, rollbackCmd);
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

    public void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals) {
        commit(() -> {
            action.execute();
            return null;
        }, (e) -> {
            rollbackAction.execute(e);
        }, principals);
    }

    public void commit(Credentials creds, MetadataAction action) {
        commit(creds, () -> {
            action.execute();
            return null;
        });
    }

    public <R> R commit(Credentials creds, MetadataCommand<R> cmd) {
        return commit(creds, cmd, nullRollbackCommand);
    }


    public <R> R commit(Credentials creds, MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd) {
        ActiveSession active = activeSession.get();

        if (active == null) {
            try {
                activeSession.set(new ActiveSession(this.repository.login(creds)));

                TransactionManager txnMgr = this.txnLookup.getTransactionManager();

                try {
                    txnMgr.begin();

                    R result = execute(creds, cmd);

                    activeSession.get().session.save();
                    checkinNodes();
                    txnMgr.commit();
                    performPostTransactionActions(true);
                    return result;
                } catch (Exception e) {
                    log.warn("Exception while execution a transactional operation - rolling back", e);

                    try {
                        txnMgr.rollback();
                    } catch (SystemException se) {
                        log.error("Failed to rollback transaction as a result of other transactional errors", se);
                    }

                    if (rollbackCmd != null) {
                        try {
                            rollbackCmd.execute(e);
                        } catch (Exception rbe) {
                            log.error("Failed to execution rollback command", rbe);
                        }
                    }

                    activeSession.get().session.refresh(false);
                    performPostTransactionActions(false);

                    throw e;
                } finally {
                    activeSession.get().session.logout();
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
        return read(createCredentials(true, principals), cmd);
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
        ActiveSession session = activeSession.get();

        if (session == null) {
            try {
                activeSession.set(new ActiveSession(this.repository.login(creds)));

                TransactionManager txnMgr = this.txnLookup.getTransactionManager();

                try {
                    txnMgr.begin();

                    return execute(creds, cmd);
                } finally {
                    try {
                        txnMgr.rollback();
                    } catch (SystemException e) {
                        log.error("Failed to rollback transaction", e);
                    }

                    activeSession.get().session.refresh(false);
                    activeSession.get().session.logout();
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
     * Execute the command in the context of the given credentials.
     */
    private <R> R execute(Credentials creds, MetadataCommand<R> cmd) throws Exception {
        if (creds instanceof OverrideCredentials) {
            // If using override credentials first replace any existing Authentication (might be null) with
            // the Authentication built from the overriding principals.
            OverrideCredentials overrideCreds = (OverrideCredentials) creds;
            Authentication initialAuth = SecurityContextHolder.getContext().getAuthentication();
            
            SecurityContextHolder.getContext().setAuthentication(overrideCreds.getAuthentication());
            try {
                return cmd.execute();
            } finally {
                // Set the current Authentication back to what it was originally.
                SecurityContextHolder.getContext().setAuthentication(initialAuth);
            }
        } else {
            return cmd.execute();
        }
    }

    /**
     * Invokes all of the post-commit consumers; passing the transaction success flag to each.
     *
     * @param success true if the transaction committed successfully, otherwise false
     */
    private void performPostTransactionActions(boolean success) {
        postTransactionActions.get().stream().forEach(action -> action.accept(success));
    }


    private Credentials createCredentials(boolean readOnly, Principal... principals) {
        Credentials creds = null;
        // Using a default principal that is read-only or read-write since we will use ACLs when we implement entity-level access control.
        ModeShapePrincipal defaultPrincipal = readOnly ? ModeShapeReadOnlyPrincipal.INSTANCE : ModeShapeReadWritePrincipal.INSTANCE;

        if (principals.length == 0) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            creds = new SpringAuthenticationCredentials(auth, defaultPrincipal);
        } else {
            creds = OverrideCredentials.create(Stream.concat(Stream.of(defaultPrincipal),
                                                             Arrays.stream(principals))
                                                   .collect(Collectors.toSet()));
        }

        return creds;
    }
    
    
    private static class ActiveSession {
        private final Session session;
        private final UsernamePrincipal userPrincipal;
        
        public ActiveSession(Session sess) {
            this.session = sess;
            this.userPrincipal = new UsernamePrincipal(sess.getUserID());
        }
    }

}

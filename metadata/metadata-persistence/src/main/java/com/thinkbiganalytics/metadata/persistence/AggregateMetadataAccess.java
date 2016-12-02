/**
 * 
 */
package com.thinkbiganalytics.metadata.persistence;

import java.security.Principal;

import javax.inject.Inject;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.config.OperationalMetadataTransactionTemplateMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;

/**
 *
 * @author Sean Felten
 */
public class AggregateMetadataAccess implements MetadataAccess {
    
    @Inject
    private OperationalMetadataTransactionTemplateMetadataAccess jpaMetadataAccess;
    
    @Inject
    private JcrMetadataAccess jcrMetadataAccess;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.MetadataCommand, java.security.Principal[])
     */
    @Override
    public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
        return jcrMetadataAccess.commit(wrap(cmd, false), principals);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.MetadataCommand, com.thinkbiganalytics.metadata.api.MetadataRollbackCommand, java.security.Principal[])
     */
    @Override
    public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {
        return jcrMetadataAccess.commit(wrap(cmd, false), rollbackCmd, principals);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.MetadataAction, java.security.Principal[])
     */
    @Override
    public void commit(MetadataAction action, Principal... principals) {
        jcrMetadataAccess.commit(wrap(action, false), principals);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.MetadataAction, com.thinkbiganalytics.metadata.api.MetadataRollbackAction, java.security.Principal[])
     */
    @Override
    public void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals) {
        jcrMetadataAccess.commit(wrap(action, false), principals);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#read(com.thinkbiganalytics.metadata.api.MetadataCommand, java.security.Principal[])
     */
    @Override
    public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
        return jcrMetadataAccess.read(wrap(cmd, true), principals);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#read(com.thinkbiganalytics.metadata.api.MetadataAction, java.security.Principal[])
     */
    @Override
    public void read(MetadataAction cmd, Principal... principals) {
        jcrMetadataAccess.read(wrap(cmd, true), principals);
    }

    
    private MetadataAction wrap(final MetadataAction action, final boolean readOnly) {
        return () -> {
            if (readOnly) {
                jpaMetadataAccess.read(() -> {
                    action.execute();
                    return null;
                });
            } else {
                jpaMetadataAccess.commit(() -> {
                    action.execute();
                    return null;
                });
            }
        };
    }
    
    private <R> MetadataCommand<R> wrap(final MetadataCommand<R> cmd, final boolean readOnly) {
        return () -> {
            if (readOnly) {
                return jpaMetadataAccess.read(() -> cmd.execute());
            } else {
                return jpaMetadataAccess.commit(() -> cmd.execute());
            }
        };
    }
}

/**
 *
 */
package com.thinkbiganalytics.metadata.persistence;

/*-
 * #%L
 * thinkbig-metadata-persistence
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
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.config.OperationalMetadataTransactionTemplateMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;

import java.security.Principal;

import javax.inject.Inject;

/**
 *
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
        if (JcrMetadataAccess.hasActiveSession()) {
            return action;
        } else {
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
    }

    private <R> MetadataCommand<R> wrap(final MetadataCommand<R> cmd, final boolean readOnly) {
        if (JcrMetadataAccess.hasActiveSession()) {
            return cmd;
        } else {
            return () -> {
                if (readOnly) {
                    return jpaMetadataAccess.read(() -> cmd.execute());
                } else {
                    return jpaMetadataAccess.commit(() -> cmd.execute());
                }
            };
        }
    }
}

/**
 *
 */
package com.thinkbiganalytics.metadata.config;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
import com.thinkbiganalytics.metadata.api.MetadataExecutionException;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.Principal;

import javax.inject.Inject;


//public class OperationalMetadataTransactionTemplateMetadataAccess implements OperationalMetadataAccess {
public class OperationalMetadataTransactionTemplateMetadataAccess implements MetadataAccess {

    private TransactionTemplate template;

    @Inject
    public void setTransactionManager(@Qualifier("operationalMetadataTransactionManager") PlatformTransactionManager transactionMgr) {
        this.template = new TransactionTemplate(transactionMgr);
    }
    
    

    @Override
    public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
        return commit(cmd);
    }

    @Override
    public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {
        // TODO Rollback command currently not supported
        return commit(cmd);
    }

    @Override
    public void commit(MetadataAction action, Principal... principals) {
        commit(action);
    }

    @Override
    public void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals) {
        // TODO Rollback command currently not supported
        commit(action);
    }



    @Override
    public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
        return read(cmd);
    }



    @Override
    public void read(MetadataAction cmd, Principal... principals) {
        read(cmd);
    }

    protected <R> R commit(MetadataCommand<R> cmd) {
        return template.execute(createCallback(cmd, false));
    }

    protected <R> R read(MetadataCommand<R> cmd) {
        return template.execute(createCallback(cmd, true));
    }
    
    protected void commit(MetadataAction action) {
        template.execute(createCallback(action, false));
    }
    
    protected void read(MetadataAction action) {
        template.execute(createCallback(action, true));
    }
    
    private TransactionCallback<Object> createCallback(final MetadataAction action, final boolean readOnly) {
        return createCallback(new MetadataCommand<Object>() {
                                  @Override
                                  public Object execute() throws Exception {
                                      action.execute();
                                      return null;
                                  }
                              }, 
                              readOnly);
    }

    private <R> TransactionCallback<R> createCallback(final MetadataCommand<R> cmd, final boolean readOnly) {
        return new TransactionCallback<R>() {
            @Override
            public R doInTransaction(TransactionStatus status) {
                if (status.isNewTransaction() && readOnly) {
                    status.setRollbackOnly();
                }

                try {
                    return cmd.execute();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }
        };
    }
}

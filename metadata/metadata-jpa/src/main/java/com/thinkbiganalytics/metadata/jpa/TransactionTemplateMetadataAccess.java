/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;

/**
 *
 * @author Sean Felten
 */
public class TransactionTemplateMetadataAccess implements MetadataAccess {
    
    private TransactionTemplate template;
    
    @Inject
    public void setTransactionManager(@Qualifier("metadataTransactionManager")PlatformTransactionManager transactionMgr) {
        this.template = new TransactionTemplate(transactionMgr);
    }

    @Override
    public <R> R commit(Command<R> cmd) {
        return template.execute(createCallback(cmd, false));
    }

    @Override
    public <R> R read(Command<R> cmd) {
        return template.execute(createCallback(cmd, true));
    }
    
    private <R> TransactionCallback<R> createCallback(final Command<R> cmd, final boolean readOnly) {
        return new TransactionCallback<R>() {
            @Override
            public R doInTransaction(TransactionStatus status) {
                if (readOnly) {
                    status.setRollbackOnly();
                }
                
                return cmd.execute();
            }
        };
    }
}

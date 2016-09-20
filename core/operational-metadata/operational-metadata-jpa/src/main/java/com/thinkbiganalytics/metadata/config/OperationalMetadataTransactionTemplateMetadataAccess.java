/**
 *
 */
package com.thinkbiganalytics.metadata.config;


import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

import javax.inject.Inject;


public class OperationalMetadataTransactionTemplateMetadataAccess implements OperationalMetadataAccess {

    private TransactionTemplate template;

    @Inject
    public void setTransactionManager(@Qualifier("operationalMetadataTransactionManager") PlatformTransactionManager transactionMgr) {
        this.template = new TransactionTemplate(transactionMgr);
    }

    @Override
    public <R> R commit(Supplier<R> cmd) {
        return template.execute(createCallback(cmd, false));
    }

    @Override
    public <R> R read(Supplier<R> cmd) {
        return template.execute(createCallback(cmd, true));
    }

    private <R> TransactionCallback<R> createCallback(final Supplier<R> cmd, final boolean readOnly) {
        return new TransactionCallback<R>() {
            @Override
            public R doInTransaction(TransactionStatus status) {
                if (readOnly) {
                    status.setRollbackOnly();
                }

                return cmd.get();
            }
        };
    }
}

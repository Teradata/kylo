package com.thinkbiganalytics.metadata.sla.spi.core;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;

import java.security.Principal;

public class MockMetadataAccess implements MetadataAccess {

    public MockMetadataAccess() {

    }

    @Override
    public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
        try {
            return cmd.execute();
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {
        try {
            return cmd.execute();
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void commit(MetadataAction cmd, Principal... principals) {
        try {
            cmd.execute();
        } catch (Exception e) {

        }
    }

    @Override
    public void commit(MetadataAction cmd, MetadataRollbackAction rollbackAction, Principal... principals) {
        try {
            cmd.execute();
        } catch (Exception e) {

        }
    }

    @Override
    public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
        try {
            return cmd.execute();
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void read(MetadataAction cmd, Principal... principals) {
        try {
            cmd.execute();
        } catch (Exception e) {

        }
    }
}
package com.thinkbiganalytics.metadata.sla.spi.core;
/*-
 * #%L
 * thinkbig-sla-metrics-default
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
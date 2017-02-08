package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;

/**
 * Provides a standard implementation of {@link NiFiProcessGroupsRestClient} that can be extended for different NiFi versions.
 */
public abstract class AbstractNiFiProcessGroupsRestClient implements NiFiProcessGroupsRestClient {

    @Nonnull
    @Override
    public Optional<ProcessGroupDTO> delete(@Nonnull final ProcessGroupDTO processGroup) {
        return deleteWithRetries(processGroup, 3, 300, TimeUnit.MILLISECONDS);
    }

    @Nonnull
    @Override
    public Optional<ProcessGroupDTO> findByName(@Nonnull final String parentGroupId, @Nonnull final String groupName, final boolean recursive, final boolean verbose) {
        final Set<ProcessGroupDTO> children = findAll(parentGroupId);
        final ProcessGroupDTO group = NifiProcessUtil.findFirstProcessGroupByName(children, groupName);
        if (group != null && verbose) {
            return findById(group.getId(), recursive, true);
        } else {
            return Optional.ofNullable(group);
        }
    }

    @Nonnull
    @Override
    public ProcessGroupDTO findRoot() {
        return findById("root", true, true).orElseThrow(IllegalStateException::new);
    }

    /**
     * Stops and deletes the specified process group after a configurable timeout.
     *
     * @param processGroup the process group to delete
     * @param retries      number of retries, at least 0; will try {@code retries} + 1 times
     * @param timeout      duration to wait between retries
     * @param timeUnit     unit of time for {@code timeout}
     * @return the deleted process group, if found
     */
    @Nonnull
    protected Optional<ProcessGroupDTO> deleteWithRetries(@Nonnull final ProcessGroupDTO processGroup, final int retries, final int timeout, @Nonnull final TimeUnit timeUnit) {
        // Stop the process group
        schedule(processGroup.getId(), processGroup.getParentGroupId(), NiFiComponentState.STOPPED);

        // Try to delete the process group
        Exception lastError = null;

        for (int count = 0; count <= retries; ++count) {
            try {
                return doDelete(processGroup);
            } catch (final WebApplicationException e) {
                if (e.getResponse().getStatus() == 409) {
                    lastError = e;
                    Uninterruptibles.sleepUninterruptibly(timeout, timeUnit);
                } else {
                    throw new NifiClientRuntimeException(e);
                }
            }
        }

        // Give up
        throw new NifiClientRuntimeException("Unable to delete process group: " + processGroup.getId(), lastError);
    }

    @Nonnull
    protected abstract Optional<ProcessGroupDTO> doDelete(@Nonnull final ProcessGroupDTO processGroup);
}

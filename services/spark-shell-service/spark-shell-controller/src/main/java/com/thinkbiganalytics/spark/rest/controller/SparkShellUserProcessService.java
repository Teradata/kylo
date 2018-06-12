package com.thinkbiganalytics.spark.rest.controller;

/*-
 * #%L
 * kylo-spark-shell-controller
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

import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@Component
public class SparkShellUserProcessService {

    /**
     * Manages Spark Shell processes
     */
    @Inject
    private SparkShellProcessManager processManager;

    /**
     * Retrieves the Spark Shell process for the current user.
     *
     * @return the Spark Shell process
     */
    @Nonnull
    protected SparkShellProcess getSparkShellProcess() throws Exception {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String username = (auth.getPrincipal() instanceof User) ? ((User) auth.getPrincipal()).getUsername() : auth.getPrincipal().toString();
        return processManager.getProcessForUser(username);
    }

    protected SparkShellProcess getSparkShellProcess(String username) throws Exception {
        return processManager.getProcessForUser(username);
    }


}

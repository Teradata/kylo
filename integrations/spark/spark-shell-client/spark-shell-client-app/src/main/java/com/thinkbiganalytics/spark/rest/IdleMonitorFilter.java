package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.spark.service.IdleMonitorService;

import org.springframework.stereotype.Component;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * Resets the idle monitor when a request is received or a response is sent.
 */
@Component
@Provider
public class IdleMonitorFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Service for detecting when this app is idle
     */
    @Context
    public IdleMonitorService idleMonitorService;

    @Override
    public void filter(@Nonnull final ContainerRequestContext requestContext) {
        idleMonitorService.reset();
    }

    @Override
    public void filter(@Nonnull final ContainerRequestContext requestContext, @Nonnull final ContainerResponseContext responseContext) throws IOException {
        idleMonitorService.reset();
    }
}

package com.thinkbiganalytics.auth.rest;

/*-
 * #%L
 * REST API Authentication
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

import com.thinkbiganalytics.rest.JerseyClientConfig;

import java.net.URI;

import javax.annotation.Nonnull;

/**
 * Configuration for a Login REST client.
 */
public class LoginJerseyClientConfig extends JerseyClientConfig {

    /** Base URL path */
    private String path;

    /**
     * Constructs a {@code LoginJerseyClientConfig} by copying another.
     * @param other the configuration to copy
     */
    public LoginJerseyClientConfig(@Nonnull final LoginJerseyClientConfig other) {
        path = other.path;

        setHost(other.getHost());
        setPort(other.getPort());
    }

    /**
     * Constructs a {@code LoginJerseyClientConfig} from the specified URI.
     * @param uri the URI to the REST API
     */
    public LoginJerseyClientConfig(@Nonnull final URI uri) {
        path = uri.getPath();

        setHost(uri.getHost());
        setPort(uri.getPort());
    }

    @Override
    public String getUrl() {
        final String url = super.getUrl();
        return (path != null) ? url + path : url;
    }
}

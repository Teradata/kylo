package com.thinkbiganalytics.integration;

/*-
 * #%L
 * kylo-commons-test
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Configuration required to connect to host which runs Nifi
 */
public class SshConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SshConfig.class);

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String knownHosts;
    private String keyfile;

    @PostConstruct
    public void initIt() throws Exception {
        LOG.info(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("host", host)
            .append("port", port)
            .append("username", username)
            .append("password", StringUtils.isNotBlank(password) ? "removed" : "not provided")
            .append("keyfile", keyfile)
            .append("known-hosts", knownHosts)
            .toString());

        if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(keyfile)) {
            throw new IllegalStateException("Must not set both keyfile and password at the same time. Set one or the other.");
        }
        if (StringUtils.isBlank(password) && StringUtils.isBlank(keyfile)) {
            throw new IllegalStateException("Must provide either a keyfile or a password.");
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    public String getKeyfile() {
        return keyfile;
    }

    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }
}

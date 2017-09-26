package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-sla-email
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

import org.apache.commons.lang3.StringUtils;

/**
 * Configuration that stores email setup.
 * This object should be returned as a new instance of a spring bean.
 *
 * @see com.thinkbiganalytics.metadata.sla.config.EmailServiceLevelAgreementSpringConfiguration
 */
public class EmailConfiguration {


    private String protocol;
    private String host;
    private int port;
    private String smtpAuth;
    private String starttls;
    private String starttlsRequired;
    private String from;
    private String username;
    private String password;
    private String smptAuthNtmlDomain;
    private String sslEnable;
    private String debug;
    private String smtpConnectionTimeout;
    private String smtpTimeout;
    private String smtpWriteTimeout;

    public String getSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(String smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public String getStarttls() {
        return starttls;
    }

    public void setStarttls(String starttls) {
        this.starttls = starttls;
    }

    public String getStarttlsRequired() {
        return starttlsRequired;
    }

    public void setStarttlsRequired(String starttlsRequired) {
        this.starttlsRequired = starttlsRequired;
    }

    public String getSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(String sslEnable) {
        this.sslEnable = sslEnable;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getSmtpConnectionTimeout() {
        return smtpConnectionTimeout;
    }

    public void setSmtpConnectionTimeout(String smtpConnectionTimeout) {
        this.smtpConnectionTimeout = smtpConnectionTimeout;
    }

    public String getSmtpTimeout() {
        return smtpTimeout;
    }

    public void setSmtpTimeout(String smtpTimeout) {
        this.smtpTimeout = smtpTimeout;
    }

    public String getSmtpWriteTimeout() {
        return smtpWriteTimeout;
    }

    public void setSmtpWriteTimeout(String smtpWriteTimeout) {
        this.smtpWriteTimeout = smtpWriteTimeout;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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

    public String getSmptAuthNtmlDomain() {
        return smptAuthNtmlDomain;
    }

    public void setSmptAuthNtmlDomain(String smptAuthNtmlDomain) {
        this.smptAuthNtmlDomain = smptAuthNtmlDomain;
    }

    public boolean isConfigured() {
        return StringUtils.isNotBlank(getHost()) && (getPort() > 0);
    }

}

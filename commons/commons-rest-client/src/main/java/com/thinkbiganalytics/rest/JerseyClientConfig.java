package com.thinkbiganalytics.rest;

/*-
 * #%L
 * thinkbig-commons-rest-client
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


import com.thinkbiganalytics.security.core.encrypt.EncryptionService;

/**
 * Configuration class used by the {@link JerseyRestClient} Parameters here allow you to setup a client and optionally pass in information to connect using Https
 */
public class JerseyClientConfig {

    private String host;

    private Integer port;
    private String username;
    private String password;
    private boolean https;

    private boolean keystoreOnClasspath;

    private String keystorePath;
    private String keystorePassword;
    private String keystoreType;

    private String truststorePath;
    private String truststorePassword;
    private String trustStoreType;

    //Values are in milliseconds
    private Integer readTimeout = null;
    private Integer connectTimeout = null;

    /**
     * flag to use the PoolingHttpClientConnectionManager from Apache instead of the Jersey Manager The PoolingHttpClientConnectionManager doesnt support some JSON header which is why this is set to
     * false by default
     **/
    private boolean useConnectionPooling = false;
    private EncryptionService encryptionService;

    public JerseyClientConfig() {
        this(new DoNothingEncryptionService());
    }

    public JerseyClientConfig(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public JerseyClientConfig(String host, String username, String password) {
        this();
        this.host = host;
        this.username = username;
        this.password = password;
        this.https = false;
        this.keystoreOnClasspath = false;
        this.keystorePath = null;
        this.keystorePassword = null;
    }

    public JerseyClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword) {
        this();
        this.host = host;
        this.username = username;
        this.password = password;
        this.https = https;
        this.keystoreOnClasspath = keystoreOnClasspath;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
    }

    public JerseyClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword, Integer readTimeout,
                              Integer connectTimeout) {
        this();
        this.host = host;
        this.username = username;
        this.password = password;
        this.https = https;
        this.keystoreOnClasspath = keystoreOnClasspath;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return encryptionService.isEncrypted(keystorePassword) ? encryptionService.decrypt(keystorePassword) : keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getHost() {
        return host;

    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return encryptionService.isEncrypted(password) ? encryptionService.decrypt(password) : password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isHttps() {
        return https;
    }

    public void setHttps(boolean https) {
        this.https = https;
    }

    public String getUrl() {
        String url = host;
        if (https) {
            url = "https://" + url;
        } else {
            url = "http://" + url;
        }
        if (port != null) {
            url += ":" + port;
        }
        return url;
    }

    public boolean isKeystoreOnClasspath() {
        return keystoreOnClasspath;
    }

    public void setKeystoreOnClasspath(boolean keystoreOnClasspath) {
        this.keystoreOnClasspath = keystoreOnClasspath;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public boolean isUseConnectionPooling() {
        return useConnectionPooling;
    }

    public void setUseConnectionPooling(boolean useConnectionPooling) {
        this.useConnectionPooling = useConnectionPooling;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    public String getTruststorePassword() {
        return encryptionService.isEncrypted(truststorePassword) ? encryptionService.decrypt(truststorePassword) : truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    @Override
    public String toString() {
        return "JerseyClientConfig{" +
               "host='" + host + '\'' +
               ", port=" + port +
               ", username='" + username + '\'' +
               ", password='XXX'" +
               ", https=" + https +
               ", keystoreOnClasspath=" + keystoreOnClasspath +
               ", keystorePath='" + keystorePath + '\'' +
               ", keystorePassword='YYY'" +
               ", keystoreType='" + keystoreType + '\'' +
               ", truststorePath='" + truststorePath + '\'' +
               ", truststorePassword='ZZZ'" +
               ", trustStoreType='" + trustStoreType + '\'' +
               ", readTimeout=" + readTimeout +
               ", connectTimeout=" + connectTimeout +
               ", useConnectionPooling=" + useConnectionPooling +
               ", encryptionService=" + encryptionService +
               '}';
    }

    private static class DoNothingEncryptionService extends EncryptionService {

        @Override
        public boolean isEncrypted(String str) {
            return false;
        }
    }

}

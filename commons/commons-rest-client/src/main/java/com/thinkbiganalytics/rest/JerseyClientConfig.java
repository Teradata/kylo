package com.thinkbiganalytics.rest;


/**
 * Created by sr186054 on 10/15/15.
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

    private boolean useConnectionPooling = true;



    public JerseyClientConfig() {

    }

    public JerseyClientConfig(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.https = false;
        this.keystoreOnClasspath = false;
        this.keystorePath = null;
        this.keystorePassword = null;
    }
    public JerseyClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.https = https;
        this.keystoreOnClasspath = keystoreOnClasspath;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
    }

    public JerseyClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword, Integer readTimeout, Integer connectTimeout) {
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
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getHost() {
        return host;

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHttps() {
        return https;
    }

    public void setHttps(boolean https) {
        this.https = https;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl(){
        String url = host;
        if(https){
            url = "https://"+url;
        }
        else {
            url ="http://"+url;
        }
        if(port != null){
            url +=":"+port;
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
        return truststorePassword;
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
}
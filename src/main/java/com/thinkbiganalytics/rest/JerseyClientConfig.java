package com.thinkbiganalytics.rest;



/**
 * Created by sr186054 on 10/15/15.
 */
public class JerseyClientConfig {
    private String host;
    private String username;
    private String password;
    private boolean https;

    private boolean keystoreOnClasspath;

    private String keystorePath;
    private String keystorePassword;




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
        return url;
    }

    public boolean isKeystoreOnClasspath() {
        return keystoreOnClasspath;
    }

    public void setKeystoreOnClasspath(boolean keystoreOnClasspath) {
        this.keystoreOnClasspath = keystoreOnClasspath;
    }
}
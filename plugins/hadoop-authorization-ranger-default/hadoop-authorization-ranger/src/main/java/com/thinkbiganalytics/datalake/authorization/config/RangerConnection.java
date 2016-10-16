package com.thinkbiganalytics.datalake.authorization.config;

/**
 * Created by Jeremy Merrifield on 9/10/16.
 */
public class RangerConnection implements AuthorizationConfiguration {

    private String hostName;
    private int port;
    private String username;
    private String password;
    private String hdfsRepositoryName;
    private String hiveRepositoryName;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHdfsRepositoryName() {
        return hdfsRepositoryName;
    }

    public void setHdfsRepositoryName(String hdfsRepositoryName) {
        this.hdfsRepositoryName = hdfsRepositoryName;
    }

    public String getHiveRepositoryName() {
        return hiveRepositoryName;
    }

    public void setHiveRepositoryName(String hiveRepositoryName) {
        this.hiveRepositoryName = hiveRepositoryName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

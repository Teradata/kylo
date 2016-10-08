package com.thinkbiganalytics.datalake.authorization.config;

import com.thinkbiganalytics.datalake.authorization.AuthorizationConfiguration;

/**
 * Created by Jeremy Merrifield on 9/10/16.
 */
public class RangerConnection implements AuthorizationConfiguration {

    private String hostName;
    private int port;
    private String username;
    private String password;
    private String hdfs_repository_name;
    private String hive_repository_name;

    public String getHostName() {
        return hostName;
    }

    public String getHdfs_repository_name() {
		return hdfs_repository_name;
	}

	public void setHdfs_repository_name(String hdfs_repository_name) {
		this.hdfs_repository_name = hdfs_repository_name;
	}

	public String getHive_repository_name() {
		return hive_repository_name;
	}

	public void setHive_repository_name(String hive_repository_name) {
		this.hive_repository_name = hive_repository_name;
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

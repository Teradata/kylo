package com.thinkbiganalytics.datalake.authorization.client;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

/**
 * Sentry Client configuration class for setting sentry connection information.
 *
 * @author sv186029
 */
public class SentryClientConfig {

    private String driverName;
    private String connectionString;
    private Configuration config;
    private String username;
    private String password;
	private String keyTab;
	private String principle;
	private FileSystem fileSystem;
	
    public SentryClientConfig(String driverName, String connectionString) {
        this.driverName = driverName;
        this.connectionString = connectionString;
    }

    
    public SentryClientConfig(String driverName, String connectionString, String username, String password) {
        this.username = driverName;
        this.connectionString = connectionString;
        this.setUsername(username);
        this.setPassword(password);
    }

    public SentryClientConfig() {

    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

	public String getPrinciple() {
		return principle;
	}

	public void setPrinciple(String principle) {
		this.principle = principle;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public String getKeyTab() {
		return keyTab;
	}

	public void setKeyTab(String keyTab) {
		this.keyTab = keyTab;
	}

	public FileSystem getFileSystem() {
		return fileSystem;
	}

	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
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

}
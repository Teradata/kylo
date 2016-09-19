package com.thinkbiganalytics.datalake.authorization;

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
	private String keyTab;
	private String principle;
	private FileSystem fileSystem;
	
    public SentryClientConfig(String driverName, String connectionString) {
        this.driverName = driverName;
        this.connectionString = connectionString;
    }

    public SentryClientConfig(String driverName, String connectionString, String principal, String keytab) {
        this.driverName = driverName;
        this.connectionString = connectionString;
        this.setPrinciple(principal);
        this.setKeyTab(keytab);
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

}
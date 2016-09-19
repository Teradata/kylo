package com.thinkbiganalytics.datalake.authorization.hdfs.sentry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

/**
 * Created by Shashi Vishwakarma on 19/9/2016.
 */

public class SentryHDFSConnectionHelper {

	private Configuration config;
	private String keyTab;
	private String principle;
	private FileSystem fileSystem;
	
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
	public String getPrinciple() {
		return principle;
	}
	public void setPrinciple(String principle) {
		this.principle = principle;
	}
	public FileSystem getFileSystem() {
		return fileSystem;
	}
	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
}

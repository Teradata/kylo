package com.thinkbiganalytics.datalake.rangeradmin.domain;

import java.util.ArrayList;

public class HivePolicy {

	private String policyName;
	private ArrayList<String> databases = new ArrayList<>();
	private ArrayList<String> tables = new ArrayList<>();
	private ArrayList<String> columns = new ArrayList<>();
	private ArrayList<String> udfs = new ArrayList<>();
	private String description;
	private String repositoryName;
	private String repositoryType;
	private String tableType;
	private String columnType;
	private String isEnabled;
	private String isAuditEnabled;
		
	private ArrayList<String> userList = new ArrayList<String>();
	private ArrayList<String> groupList = new ArrayList<String>();
	private static ArrayList<String> permList = new ArrayList<String>();
	
	
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
	public ArrayList<String> getDatabases() {
		return databases;
	}
	public void setDatabases(ArrayList<String> databases) {
		this.databases = databases;
	}
	public ArrayList<String> getTables() {
		return tables;
	}
	public void setTables(ArrayList<String> tables) {
		this.tables = tables;
	}
	public ArrayList<String> getColumns() {
		return columns;
	}
	public void setColumns(ArrayList<String> columns) {
		this.columns = columns;
	}
	public ArrayList<String> getUdfs() {
		return udfs;
	}
	public void setUdfs(ArrayList<String> udfs) {
		this.udfs = udfs;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRepositoryName() {
		return repositoryName;
	}
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
	public String getRepositoryType() {
		return repositoryType;
	}
	public void setRepositoryType(String repositoryType) {
		this.repositoryType = repositoryType;
	}
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public String getIsEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(String isEnabled) {
		this.isEnabled = isEnabled;
	}
	public String getIsAuditEnabled() {
		return isAuditEnabled;
	}
	public void setIsAuditEnabled(String isAuditEnabled) {
		this.isAuditEnabled = isAuditEnabled;
	}
	public ArrayList<String> getUserList() {
		return userList;
	}
	public void setUserList(ArrayList<String> userList) {
		this.userList = userList;
	}
	public ArrayList<String> getGroupList() {
		return groupList;
	}
	public void setGroupList(ArrayList<String> groupList) {
		this.groupList = groupList;
	}
	public static ArrayList<String> getPermList() {
		return permList;
	}
	public static void setPermList(ArrayList<String> permList) {
		HivePolicy.permList = permList;
	}
	
	
}	
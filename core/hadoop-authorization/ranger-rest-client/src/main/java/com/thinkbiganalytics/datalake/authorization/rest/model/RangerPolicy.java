package com.thinkbiganalytics.datalake.authorization.rest.model;

import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationPolicy;

/**
 * Created by Shashi Vishwakarma on 10/05/16.
 */

public class RangerPolicy implements HadoopAuthorizationPolicy {

	private int id;
	private String owner;
	private String updatedBy  ;
	private String policyName  ;
	private String resourceName  ;
	private String repositoryName  ;
	private String repositoryType  ;
	private String tables  ;
	private String columns  ;
	private String databases ;
	private String udfs ;
	private String isEnabled ;
	private String isRecursive ;
	private String isAuditEnabled ;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
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
	public String getTables() {
		return tables;
	}
	public void setTables(String tables) {
		this.tables = tables;
	}
	public String getColumns() {
		return columns;
	}
	public void setColumns(String columns) {
		this.columns = columns;
	}
	public String getDatabases() {
		return databases;
	}
	public void setDatabases(String databases) {
		this.databases = databases;
	}
	public String getUdfs() {
		return udfs;
	}
	public void setUdfs(String udfs) {
		this.udfs = udfs;
	}
	public String getIsEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(String isEnabled) {
		this.isEnabled = isEnabled;
	}
	public String getIsRecursive() {
		return isRecursive;
	}
	public void setIsRecursive(String isRecursive) {
		this.isRecursive = isRecursive;
	}
	public String getIsAuditEnabled() {
		return isAuditEnabled;
	}
	public void setIsAuditEnabled(String isAuditEnabled) {
		this.isAuditEnabled = isAuditEnabled;
	}
	@Override
	public int getPolicyId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	@Override
	public int getPolicyCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}

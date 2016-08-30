package com.thinkbiganalytics.datalake.sentry;

/**
 * Sentry Client configuration class for setting sentry connection information.
 * @author sv186029
 *
 */
public class SentryClientConfig {

	private String driverName;
	private String connectionString ; 
	private String principal;
	private String keytab;

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

	public SentryClientConfig(String driverName, String connectionString)
	{
		this.driverName=driverName;
		this.connectionString=connectionString;
	}

	public SentryClientConfig(String driverName, String connectionString, String principal, String keytab)
	{
		this.driverName=driverName;
		this.connectionString=connectionString;
		this.setPrincipal(principal);
		this.setKeytab(keytab);
	}

	public SentryClientConfig()
	{

	}

	public String getPrincipal() {
		return principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public String getKeytab() {
		return keytab;
	}

	public void setKeytab(String keytab) {
		this.keytab = keytab;
	}

}
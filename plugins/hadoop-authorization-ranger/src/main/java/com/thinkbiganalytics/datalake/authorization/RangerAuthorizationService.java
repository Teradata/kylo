package com.thinkbiganalytics.datalake.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.authorization.config.RangerConnection;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationPolicy;
import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClient;
import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClientConfig;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerCreatePolicy;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerGroup;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerUpdatePolicy;

public class RangerAuthorizationService implements HadoopAuthorizationService {

	private static final Logger log = LoggerFactory.getLogger(RangerAuthorizationService.class);

	final private String NIFI = "nifi_";
	private RangerRestClient rangerRestClient;
	private static String HDFS_REPOSITORY_TYPE ="hdfs";
	private static String HIVE_REPOSITORY_TYPE ="hive";
	private static String IsEnable ="true";
	private static String IsRecursive ="true";
	private static String IsAuditable = "true";
	private static String REPOSITORY_TYPE = "repositoryType";
	private static String POLICY_NAME="policyName";
	private static String HIVE_COLUMN_PERMISSION="*";

	RangerConnection rangerConnHelper;

	/**
	 * Implement Ranger Authentication Service. Initiate RangerClient and RangerClientConfig for initializing service and invoke different methods of it.
	 */

	@Override
	public void initialize(AuthorizationConfiguration config) {
		rangerConnHelper = (RangerConnection) config;
		RangerRestClientConfig rangerClientConfiguration = new RangerRestClientConfig(rangerConnHelper.getHostName(), rangerConnHelper.getUsername(), rangerConnHelper.getPassword());
		rangerClientConfiguration.setPort(rangerConnHelper.getPort());
		rangerRestClient = new RangerRestClient(rangerClientConfiguration);

	}


	@Override
	public RangerGroup getGroupByName(String groupName) {
		return rangerRestClient.getGroupByName(groupName);
	}

	@Override
	public List<HadoopAuthorizationGroup> getAllGroups() {
		return rangerRestClient.getAllGroups();
	}


	@Override
	public void createPolicy(String policyName ,  List<String> group_List, List<String> hdfs_paths, String permission_level,
			List<String> datebaseNames, List<String> tableNames, List<String> hdfs_permission_list,
			List<String> hive_permission_List) {

		RangerCreatePolicy rangerCreatePolicy = new RangerCreatePolicy();


		/**
		 * Create HDFS Policy 
		 */
		String ranger_hdfs_policy_name = NIFI+policyName +"_"+ HDFS_REPOSITORY_TYPE;
		String description = "Ranger policy created for group list " +group_List.toString()+ " for resource " + hdfs_paths.toString() ;
		String hdfs_resource = convertListToString(hdfs_paths, ",");

		rangerCreatePolicy.setPolicyName(ranger_hdfs_policy_name);
		rangerCreatePolicy.setResourceName(hdfs_resource);
		rangerCreatePolicy.setDescription(description);
		rangerCreatePolicy.setRepositoryName(rangerConnHelper.getHdfs_repository_name());
		rangerCreatePolicy.setRepositoryType(HDFS_REPOSITORY_TYPE);
		rangerCreatePolicy.setIsEnabled(IsEnable);
		rangerCreatePolicy.setIsRecursive(IsRecursive);
		rangerCreatePolicy.setIsAuditEnabled(IsAuditable);
		rangerCreatePolicy.setPermMapList( group_List, hdfs_permission_list);
		rangerRestClient.createPolicy(rangerCreatePolicy);

		/**
		 * Creating Hive Policy
		 */

		String ranger_hive_policy_name = NIFI+policyName+"_"+HIVE_REPOSITORY_TYPE;
		String hive_description = "Ranger policy created for group list " +group_List.toString()+ " for resource " + hdfs_paths.toString() ;
		String hive_databases = convertListToString(datebaseNames, ",");
		String hive_tables = convertListToString(tableNames, ",");

		rangerCreatePolicy = new RangerCreatePolicy();
		
		rangerCreatePolicy.setPolicyName(ranger_hive_policy_name);
		rangerCreatePolicy.setDatabases(hive_databases);
		rangerCreatePolicy.setTables(hive_tables);
		rangerCreatePolicy.setColumns(HIVE_COLUMN_PERMISSION);
		rangerCreatePolicy.setUdfs("");
		rangerCreatePolicy.setDescription(hive_description);
		rangerCreatePolicy.setRepositoryName(rangerConnHelper.getHive_repository_name());
		rangerCreatePolicy.setRepositoryType(HIVE_REPOSITORY_TYPE);
		rangerCreatePolicy.setIsAuditEnabled(IsAuditable);
		rangerCreatePolicy.setIsEnabled(IsEnable);
		rangerCreatePolicy.setPermMapList(group_List, hive_permission_List);
		
		rangerRestClient.createPolicy(rangerCreatePolicy);

	}



	@Override
	public void deletePolicy(int id) {
		rangerRestClient.deletePolicy(id);
	}


	@Override
	public void updatePolicy(String policyName ,  List<String> group_List, List<String> hdfs_paths, String permission_level,
			List<String> datebaseNames, List<String> tableNames, List<String> hdfs_permission_list,
			List<String> hive_permission_List) throws Exception {

		int policyId = 0 ;
		String ranger_hdfs_policy_name = NIFI+policyName +"_"+ HDFS_REPOSITORY_TYPE;

		Map<String,Object> searchHDFSCriteria = new HashMap<>();
		searchHDFSCriteria.put(POLICY_NAME,ranger_hdfs_policy_name);
		searchHDFSCriteria.put(REPOSITORY_TYPE, HDFS_REPOSITORY_TYPE);
		List<HadoopAuthorizationPolicy>  hadoopPolicyList = this.searchPolicy(searchHDFSCriteria);

		
		if (hadoopPolicyList.size() == 0)
		{
			throw new UnsupportedOperationException("Ranger Plugin : Unable to get ID for Ranger HDFS Policy");	
		}
		else
		{
			if(hadoopPolicyList.size() > 1)
			{
				throw new Exception("Unable to find HDFS unique policy.");
			}
			else
			{

				for(HadoopAuthorizationPolicy hadoopPolicy : hadoopPolicyList)
				{
					policyId = hadoopPolicy.getPolicyId();
					log.info("Got Policy ID  - " +policyId);
				}
			}
		}
		
		RangerUpdatePolicy rangerUpdatePolicy = new RangerUpdatePolicy();

		/**
		 * Update HDFS Policy 
		 */

		String description = "Ranger policy updated for group list " +group_List.toString()+ " for resource " + hdfs_paths.toString() ;
		String hdfs_resource = convertListToString(hdfs_paths, ",");

		rangerUpdatePolicy.setPolicyName(ranger_hdfs_policy_name);
		rangerUpdatePolicy.setResourceName(hdfs_resource);
		rangerUpdatePolicy.setDescription(description);
		rangerUpdatePolicy.setRepositoryName(rangerConnHelper.getHdfs_repository_name());
		rangerUpdatePolicy.setRepositoryType(HDFS_REPOSITORY_TYPE);
		rangerUpdatePolicy.setIsEnabled(IsEnable);
		rangerUpdatePolicy.setIsRecursive(IsRecursive);
		rangerUpdatePolicy.setIsAuditEnabled(IsAuditable);
		rangerUpdatePolicy.setPermMapList( group_List, hdfs_permission_list);
		rangerRestClient.updatePolicy(rangerUpdatePolicy,policyId);

		/**
		 * Update Hive Policy
		 */

		String ranger_hive_policy_name = NIFI+policyName+"_"+HIVE_REPOSITORY_TYPE;
		Map<String,Object> searchHiveCriteria = new HashMap<>();
		searchHiveCriteria.put(POLICY_NAME,ranger_hive_policy_name);
		searchHiveCriteria.put(REPOSITORY_TYPE, HIVE_REPOSITORY_TYPE);
		hadoopPolicyList = this.searchPolicy(searchHiveCriteria);

		rangerUpdatePolicy = new RangerUpdatePolicy();
		policyId =0;
		if (hadoopPolicyList.size() == 0)
		{
			throw new UnsupportedOperationException("Ranger Plugin : Unable to get ID for Ranger Hive Policy");	
		}
		else
		{
			if(hadoopPolicyList.size() > 1)
			{
				throw new Exception("Unable to find Hive unique policy.");
			}
			else
			{

				for(HadoopAuthorizationPolicy hadoopPolicy : hadoopPolicyList)
				{
					policyId = hadoopPolicy.getPolicyId();
					log.info("Got Policy ID  - " +policyId);
				}
			}
		}

		String hive_description = "Ranger policy updated for group list " +group_List.toString()+ " for resource " + datebaseNames.toString() ;
		String hive_databases = convertListToString(datebaseNames, ",");
		String hive_tables = convertListToString(tableNames, ",");

		rangerUpdatePolicy.setPolicyName(ranger_hive_policy_name);
		rangerUpdatePolicy.setDatabases(hive_databases);
		rangerUpdatePolicy.setTables(hive_tables);
		rangerUpdatePolicy.setColumns(HIVE_COLUMN_PERMISSION);
		rangerUpdatePolicy.setUdfs("");
		rangerUpdatePolicy.setDescription(hive_description);
		rangerUpdatePolicy.setRepositoryName(rangerConnHelper.getHive_repository_name());
		rangerUpdatePolicy.setRepositoryType(HIVE_REPOSITORY_TYPE);
		rangerUpdatePolicy.setIsAuditEnabled(IsAuditable);
		rangerUpdatePolicy.setIsEnabled(IsEnable);
		rangerUpdatePolicy.setPermMapList(group_List, hive_permission_List);
		rangerRestClient.updatePolicy(rangerUpdatePolicy,policyId);

	}


	@Override
	public List<HadoopAuthorizationPolicy> searchPolicy(Map<String, Object> searchCriteria) {
		return rangerRestClient.searchPolicies(searchCriteria);
	}

	/**
	 * 
	 * @param list
	 * @param delim
	 * @return : comma separated string
	 */
	public static String convertListToString(List<String> list, String delim) {

		StringBuilder sb = new StringBuilder();

		String loopDelim = "";

		for(String input : list) {

			sb.append(loopDelim);
			sb.append(input);            

			loopDelim = delim;
		}
		return sb.toString();
	}


}

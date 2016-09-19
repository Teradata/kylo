package com.thinkbiganalytics.datalake.authorization.hdfs.sentry;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

/**
 * Created by Shashi Vishwakarma on 19/9/2016.
 */

public class ApplyHDFSAcl {

	public ApplyHDFSAcl()
	{

	}

	/**
	 * 
	 * @param HadoopConfigurationResource
	 * @param keyTab : Kerberos Keytab for Authentication
	 * @param pricinple : Kerberos Principle for Authentication
	 * @param category_name : Category Name
	 * @param feed_name : Feed Name 
	 * @param permission_level : Feed Permission Level
	 * @param groupList : Group List for Authorization
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public boolean createAcl(String HadoopConfigurationResource, String keyTab, String pricinple, String category_name , String feed_name, String permission_level , String groupList ) throws IOException {

		try {

			SentryHDFSConnectionHelper conHelper = new SentryHDFSConnectionHelper();
			conHelper.setKeyTab(keyTab);
			conHelper.setPrinciple(pricinple);

			HDFSUtil hdfsUtil = new HDFSUtil();
			Configuration conf = conHelper.getConfig(); 
			conf = hdfsUtil.getConfigurationFromResources(HadoopConfigurationResource);

			String allPathForAclCreation = hdfsUtil.constructResourceforPermissionHDFS(category_name, feed_name, permission_level);
			hdfsUtil.splitPathListAndApplyPolicy(allPathForAclCreation, conf, conHelper.getFileSystem() , groupList);

			//Return if HDFS Permission is created successfully.
			return true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException("Unable to create ACL " + e);
		}


	}

	/**
	 * 
	 * @param HadoopConfigurationResource
	 * @param category_name : Category Name
	 * @param feed_name : Feed Name 
	 * @param permission_level : Feed Permission Level
	 * @param groupList : Group List for Authorization
	 * @throws IOException 
	 */

	@SuppressWarnings("static-access")
	public boolean createAcl(String HadoopConfigurationResource, String category_name , String feed_name, String permission_level , String groupList) throws IOException {

		try {

			SentryHDFSConnectionHelper conHelper = new SentryHDFSConnectionHelper();
			HDFSUtil hdfsUtil = new HDFSUtil();

			Configuration conf = conHelper.getConfig(); 
			conf = hdfsUtil.getConfigurationFromResources(HadoopConfigurationResource);
			
			String allPathForAclCreation = hdfsUtil.constructResourceforPermissionHDFS(category_name, feed_name, permission_level);
			hdfsUtil.splitPathListAndApplyPolicy(allPathForAclCreation, conf, conHelper.getFileSystem() , groupList);
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException("Unable to create ACL " + e);

		}
	}

}

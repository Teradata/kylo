/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.nifi.processors.ranger.authorization;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.CATEGORY_NAME;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.FEED_NAME;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.GROUP_LIST;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.HDFS_PERMISSION_LIST;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.HDFS_REPOSIROTY_NAME;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.HIVE_PERMISSION_LIST;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.HIVE_REPOSIROTY_NAME;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.PERMISSION_LEVEL;
import static  com.thinkbiganalytics.nifi.processors.ranger.authorization.ComponentProperties.RANGER_SERVICE;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import com.thinkbiganalytics.datalake.authorization.model.HDFSPolicy;
import com.thinkbiganalytics.datalake.authorization.model.HivePolicy;
import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClient;
import com.thinkbiganalytics.nifi.authorization.ranger.service.RangerService;
import com.thinkbiganalytics.nifi.processors.ranger.authorization.service.util.RangerUtil;

@Tags({"ranger,thinkbig,authorization"})
@CapabilityDescription("Creating group policies using Ranger.")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})

public class CreateRangerAuthorizationPolicy extends AbstractProcessor  {

	private static final String HDFS_REPOSITORY_TYPE="hdfs";
	private static final String HIVE_REPOSITORY_TYPE="hive";

	// Standard Relationships
	public static final Relationship REL_SUCCESS = new Relationship.Builder()
			.name("success")
			.description("Successfully created policy from .")
			.build();

	public static final Relationship REL_FAILURE = new Relationship.Builder()
			.name("failure")
			.description("Failed to create policy for Group.")
			.build();


	private List<PropertyDescriptor> propDescriptors;

	private Set<Relationship> relationships;

	public CreateRangerAuthorizationPolicy() {
		final Set<Relationship> r = new HashSet<>();
		r.add(REL_SUCCESS);
		r.add(REL_FAILURE);
		relationships = Collections.unmodifiableSet(r);

		final List<PropertyDescriptor> pds = new ArrayList<>();
		pds.add(RANGER_SERVICE);
		pds.add(GROUP_LIST);
		pds.add(PERMISSION_LEVEL);
		pds.add(HIVE_PERMISSION_LIST);
		pds.add(HDFS_PERMISSION_LIST);
		pds.add(CATEGORY_NAME);
		pds.add(FEED_NAME);
		pds.add(HDFS_REPOSIROTY_NAME);
		pds.add(HIVE_REPOSIROTY_NAME);

		propDescriptors = Collections.unmodifiableList(pds);
	}

	@Override
	public Set<Relationship> getRelationships() {
		return relationships;
	}

	@Override
	protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return propDescriptors;
	}

	@Override
	public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
		final ProcessorLog logger = getLogger();
		FlowFile flowFile = session.get();
		if ( flowFile == null ) {
			return;
		}

		//Read policy information
		String  group_list=context.getProperty(GROUP_LIST).evaluateAttributeExpressions(flowFile).getValue();
		String  permission_level=context.getProperty(PERMISSION_LEVEL).getValue();
		String  hdfs_permission_list=context.getProperty(HDFS_PERMISSION_LIST).getValue();
		String  hive_permission_list=context.getProperty(HIVE_PERMISSION_LIST).getValue();
		String  category_name=context.getProperty(CATEGORY_NAME).evaluateAttributeExpressions(flowFile).getValue();
		String  feed_name=context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
		String  hdfs_reposiroty_name=context.getProperty(HDFS_REPOSIROTY_NAME).getValue();
		String  hive_reposiroty_name=context.getProperty(HIVE_REPOSIROTY_NAME).getValue(); 

		final RangerService rangerService = context.getProperty(RANGER_SERVICE).asControllerService(RangerService.class);
		try {

			RangerRestClient rangerClientObject =  rangerService.getConnection();
			RangerUtil rangerUtil = new RangerUtil();

			
			
			logger.info("Creating HDFS Policy");
			if (rangerUtil.checkIfPolicyExists(rangerClientObject, category_name,feed_name , permission_level , HDFS_REPOSITORY_TYPE))
			{
				HDFSPolicy createhdfsPolicy = rangerUtil.getHDFSCreatePolicyJson( group_list, permission_level , category_name , feed_name ,hdfs_permission_list ,hdfs_reposiroty_name);
				rangerClientObject.createPolicy(createhdfsPolicy.policyJson());
				logger.info("HDFS Policy got created for feed " +feed_name+ "." );
			}
			else
			{
				int policyId = rangerUtil.getIdForExistingPolicy(rangerClientObject, category_name, feed_name, permission_level, HDFS_REPOSITORY_TYPE);
				HDFSPolicy updatehdfsPolicy = rangerUtil.updateHDFSPolicyJson( group_list, permission_level , category_name , feed_name ,hdfs_permission_list ,hdfs_reposiroty_name);
				rangerClientObject.updatePolicy(updatehdfsPolicy.policyJson(), policyId);
				logger.warn("HDFS Policy already exists for feed " + feed_name +". Updating HDFS existing policy.");
			}

			logger.info("Creating HIVE Policy");
			if (rangerUtil.checkIfPolicyExists(rangerClientObject, category_name,feed_name ,permission_level , HIVE_REPOSITORY_TYPE))
			{
				HivePolicy createHivePolicy = rangerUtil.getHIVECreatePolicyJson( group_list, permission_level , category_name , feed_name ,hive_permission_list,hive_reposiroty_name);
				rangerClientObject.createPolicy(createHivePolicy.policyJson());
				logger.info("Hive Policy got created for feed " +feed_name+ "." );
			}
			else
			{
				int policyId = rangerUtil.getIdForExistingPolicy(rangerClientObject, category_name, feed_name, permission_level, HIVE_REPOSITORY_TYPE);
				HivePolicy updateHivePolicy = rangerUtil.updateHivePolicyJson( group_list, permission_level , category_name , feed_name ,hive_permission_list,hive_reposiroty_name);
				rangerClientObject.updatePolicy(updateHivePolicy.policyJson(), policyId);
				logger.warn("HIVE Policy already exists for feed " + feed_name +". Updating Hive existing policy.");
			}

			session.transfer(flowFile, REL_SUCCESS);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Unable to obtain Ranger connection for {} due to {}; routing to failure", new Object[]{flowFile, e});
			session.transfer(flowFile, REL_FAILURE);

		}

	}

}

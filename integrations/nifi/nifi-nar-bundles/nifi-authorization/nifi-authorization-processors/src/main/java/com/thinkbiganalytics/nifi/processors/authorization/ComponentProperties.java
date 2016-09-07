/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.nifi.processors.authorization;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;
import com.thinkbiganalytics.nifi.authorization.ranger.service.RangerService;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;

public interface ComponentProperties {

	/**
	 * Ranger Properties
	 */
	public  PropertyDescriptor RANGER_SERVICE = new PropertyDescriptor.Builder()
			.name("Ranger Connection Pooling Service")
			.description("The Ranger Controller Service that is used to obtain connection from Ranger Authorization Service.")
			.required(true)
			.identifiesControllerService(RangerService.class)
			.build();

	public static final PropertyDescriptor GROUP_LIST = new PropertyDescriptor
			.Builder().name("Group List")
			.description("Comma Seperated list of group for creating policy")
			.required(true)
			.defaultValue("${groups}")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.expressionLanguageSupported(true)
			.build();

	public static final PropertyDescriptor PERMISSION_LEVEL = new PropertyDescriptor
			.Builder().name("Permission Level")
			.description("Level at which policy should be created ex . category,feed")
			.allowableValues("Category","Feed")
			.defaultValue("Feed")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();

	public static final PropertyDescriptor HDFS_PERMISSION_LIST = new PropertyDescriptor
			.Builder().name("HDFS Permission List")
			.description("HDFS Permission List")
			.defaultValue("read,write")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();

	public static final PropertyDescriptor HIVE_PERMISSION_LIST = new PropertyDescriptor
			.Builder().name("HIVE Permission List")
			.description("HIVE Permission List")
			.defaultValue("create,select,update")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();

	public static final PropertyDescriptor CATEGORY_NAME = new PropertyDescriptor
			.Builder().name("Category Name")
			.description("Category Name")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.expressionLanguageSupported(true)
			.defaultValue("${category}")
			.build();


	public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor
			.Builder().name("Feed Name")
			.description("Feed Name")
			.required(true)
			.defaultValue("${feed}")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.expressionLanguageSupported(true)
			.build();

	public static final PropertyDescriptor HDFS_REPOSIROTY_NAME = new PropertyDescriptor
			.Builder().name("HDFS Repository Name")
			.description("HDFS Repository Name")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();


	public static final PropertyDescriptor HIVE_REPOSIROTY_NAME = new PropertyDescriptor
			.Builder().name("Hive Repository Name")
			.description("Hive Repository Name")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();

	/**
	 * Sentry Properties
	 */

	PropertyDescriptor THRIFT_SERVICE = new PropertyDescriptor.Builder()
			.name("Database Connection Pooling Service")
			.description("The Controller Service that is used to obtain connection to database")
			.required(true)
			.identifiesControllerService(ThriftService.class)
			.build();

	
	
}

package com.thinkbiganalytics.nifi.processors.sentry.authorization;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;

/**
 * Created by Shashi Vishwakarma on 9/9/16.
 */

public interface ComponentProperties {
	/**
	 * Sentry Properties
	 */	

	public static final PropertyDescriptor GROUP_LIST = new PropertyDescriptor
			.Builder().name("Group List")
			.description("Comma Seperated list of group for creating policy")
			.required(true)
			.defaultValue("${security.authorization.group")
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


	public static final PropertyDescriptor THRIFT_SERVICE = new PropertyDescriptor.Builder()
			.name("Database Connection Pooling Service")
			.description("The Controller Service that is used to obtain connection to database")
			.required(true)
			.identifiesControllerService(ThriftService.class)
			.build();



}

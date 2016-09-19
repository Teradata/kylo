/**
 * Created by Shashi Vishwakarma on 9/9/16.
 */

package com.thinkbiganalytics.nifi.processors.sentry.authorization;

import static com.thinkbiganalytics.nifi.processors.sentry.authorization.ComponentProperties.CATEGORY_NAME;
import static com.thinkbiganalytics.nifi.processors.sentry.authorization.ComponentProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.processors.sentry.authorization.ComponentProperties.GROUP_LIST;
import static com.thinkbiganalytics.nifi.processors.sentry.authorization.ComponentProperties.PERMISSION_LEVEL;
import static com.thinkbiganalytics.nifi.processors.sentry.authorization.ComponentProperties.THRIFT_SERVICE;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.NiFiProperties;

import com.thinkbiganalytics.datalake.authorization.hdfs.sentry.ApplyHDFSAcl;
import com.thinkbiganalytics.nifi.processors.sentry.authorization.service.util.SentryUtil;
import com.thinkbiganalytics.nifi.security.ApplySecurityPolicy;
import com.thinkbiganalytics.nifi.security.SecurityUtil;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;

@Tags({"sentry,authorization,thinkbig"})
@CapabilityDescription("Sentry processor for creating policies for Apache Kylo.")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class CreateSentryAuthorizationPolicy extends AbstractProcessor {

	public static NiFiProperties NIFI_PROPERTIES = null;

	public static final PropertyDescriptor HADOOP_CONFIGURATION_RESOURCES = new PropertyDescriptor.Builder()
			.name("Hadoop Configuration Resources")
			.description("A file or comma separated list of files which contains the Hadoop file system configuration. Without this, Hadoop "
					+ "will search the classpath for a 'core-site.xml' and 'hdfs-site.xml' file or will revert to a default configuration.")
			.required(false).addValidator(createMultipleFilesExistValidator())
			.build();

	public static final PropertyDescriptor KERBEROS_PRINCIPAL = new PropertyDescriptor.Builder().name("Kerberos Principal").required(false)
			.description("Kerberos principal to authenticate as. Requires nifi.kerberos.krb5.file to be set " + "in your nifi.properties").addValidator(Validator.VALID)
			.addValidator(kerberosConfigValidator()).build();

	public static final PropertyDescriptor KERBEROS_KEYTAB = new PropertyDescriptor.Builder().name("Kerberos Keytab").required(false)
			.description("Kerberos keytab associated with the principal. Requires nifi.kerberos.krb5.file to be set " + "in your nifi.properties").addValidator(Validator.VALID)
			.addValidator(StandardValidators.FILE_EXISTS_VALIDATOR)
			.addValidator(kerberosConfigValidator()).build();

	public static final Relationship Success = new Relationship.Builder()
			.name("success")
			.description("success")
			.build();

	public static final Relationship Failure = new Relationship.Builder()
			.name("failure")
			.description("failure")
			.build();

	private List<PropertyDescriptor> descriptors;

	private Set<Relationship> relationships;

	@Override
	protected void init(final ProcessorInitializationContext context) {
		final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
		descriptors.add(THRIFT_SERVICE);
		descriptors.add(HADOOP_CONFIGURATION_RESOURCES);
		descriptors.add(KERBEROS_PRINCIPAL);
		descriptors.add(KERBEROS_KEYTAB);
		descriptors.add(PERMISSION_LEVEL);
		descriptors.add(GROUP_LIST);
		descriptors.add(CATEGORY_NAME);
		descriptors.add(FEED_NAME);

		this.descriptors = Collections.unmodifiableList(descriptors);

		final Set<Relationship> relationships = new HashSet<Relationship>();
		relationships.add(Success);
		relationships.add(Failure);
		this.relationships = Collections.unmodifiableSet(relationships);
	}

	@Override
	public Set<Relationship> getRelationships() {
		return this.relationships;
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return descriptors;
	}

	@OnScheduled
	public void onScheduled(final ProcessContext context) {

	}

	@Override
	public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
		FlowFile flowFile = session.get();
		final ProcessorLog logger = getLogger();
		if ( flowFile == null ) {
			return;
		}

		final ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);

		try (final Connection conn = thriftService.getConnection()) {

			String permission = context.getProperty(PERMISSION_LEVEL).evaluateAttributeExpressions(flowFile).getValue();
			String group_list = context.getProperty(GROUP_LIST).evaluateAttributeExpressions(flowFile).getValue();
			String category = context.getProperty(CATEGORY_NAME).evaluateAttributeExpressions(flowFile).getValue();
			String feed = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();

			Statement stmt = conn.createStatement();
			SentryUtil sentryUtil = new SentryUtil();
			boolean policy_creation_status=sentryUtil.createPolicy(stmt,group_list,category,feed ,permission);

			
			String principal = context.getProperty(KERBEROS_PRINCIPAL).getValue();
			String keyTab = context.getProperty(KERBEROS_KEYTAB).getValue();
			String hadoopConfigurationResources = context.getProperty(HADOOP_CONFIGURATION_RESOURCES).getValue();

			/**
			 * Check for Kerberos Security Before Creating ACL
			 */
			boolean authenticateUser = false;
			if (!(StringUtils.isEmpty(principal) && StringUtils.isEmpty(keyTab) && StringUtils.isEmpty(hadoopConfigurationResources))) {
				authenticateUser = true;
			}

			if(authenticateUser) {
				ApplySecurityPolicy applySecurityObject = new ApplySecurityPolicy();
				Configuration configuration;

				try {

					configuration = applySecurityObject.getConfigurationFromResources(hadoopConfigurationResources);

					if (SecurityUtil.isSecurityEnabled(configuration)) {
						if (principal.equals("") && keyTab.equals("")) {
							getLogger().error("Kerberos Principal and Kerberos KeyTab information missing in Kerboeros enabled cluster.");
							session.transfer(flowFile, Failure);
							return;
						}

						try {
							getLogger().info("User anuthentication initiated");

							boolean authenticationStatus = applySecurityObject.validateUserWithKerberos(logger, hadoopConfigurationResources, principal, keyTab);
							if (authenticationStatus) {
								getLogger().info("User authenticated successfully.");
							} else {
								getLogger().info("User authentication failed.");
								session.transfer(flowFile, Failure);
								return;
							}

						} catch (Exception unknownException) {
							getLogger().error("Unknown exception occured while validating user :" + unknownException.getMessage());
							session.transfer(flowFile, Failure);
							return;
						}

					}
				} catch (IOException e1) {
					getLogger().error("Unknown exception occurred while authenticating user :" + e1.getMessage());
					session.transfer(flowFile, Failure);
					return;
				}
			}


			/**
			 * Apply HDFS ACL 
			 */
			
			ApplyHDFSAcl applyAcl = new ApplyHDFSAcl();
			boolean hdfs_acl_status = applyAcl.createAcl(hadoopConfigurationResources, category, feed, permission, group_list);

			//Based on policy creation status , route flowfile either success or failure.
			if(policy_creation_status && hdfs_acl_status)
			{
				session.transfer(flowFile, Success);
			}
			else
			{
				session.transfer(flowFile, Failure);
			}

		} catch (final ProcessException | SQLException | IOException e) {
			logger.error("Unable to obtain connection for {} due to {}; routing to failure", new Object[]{flowFile, e});
			session.transfer(flowFile, Failure);
		}

	}

	public static final Validator kerberosConfigValidator() {
		return new Validator() {

			@Override
			public ValidationResult validate(String subject, String input, ValidationContext context) {

				File nifiProperties = NiFiProperties.getInstance().getKerberosConfigurationFile();

				// Check that the Kerberos configuration is set
				if (nifiProperties == null) {
					return new ValidationResult.Builder()
							.subject(subject).input(input).valid(false)
							.explanation("you are missing the nifi.kerberos.krb5.file property which "
									+ "must be set in order to use Kerberos")
							.build();
				}

				// Check that the Kerberos configuration is readable
				if (!nifiProperties.canRead()) {
					return new ValidationResult.Builder().subject(subject).input(input).valid(false)
							.explanation(String.format("unable to read Kerberos config [%s], please make sure the path is valid "
									+ "and nifi has adequate permissions", nifiProperties.getAbsoluteFile()))
							.build();
				}

				return new ValidationResult.Builder().subject(subject).input(input).valid(true).build();
			}


		};
	}

	/*
	 * Validates that one or more files exist, as specified in a single property.
	 */

	public static final Validator createMultipleFilesExistValidator() {
		return new Validator() {
			@Override
			public ValidationResult validate(String subject, String input, ValidationContext context) {
				final String[] files = input.split(",");
				for (String filename : files) {
					try {
						final File file = new File(filename.trim());
						final boolean valid = file.exists() && file.isFile();
						if (!valid) {
							final String message = "File " + file + " does not exist or is not a file";
							return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
						}
					} catch (SecurityException e) {
						final String message = "Unable to access " + filename + " due to " + e.getMessage();
						return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
					}
				}
				return new ValidationResult.Builder().subject(subject).input(input).valid(true).build();
			}

		};
	}


}


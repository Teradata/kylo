/**
 * Copyright (c) 2016. Teradata Inc.
 * @author sv186029
 */
package com.thinkbiganalytics.nifi.processors.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import com.thinkbiganalytics.nifi.processors.service.util.SentryUtil;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import static com.thinkbiganalytics.nifi.processors.authorization.ComponentProperties.CATEGORY_NAME;
import static com.thinkbiganalytics.nifi.processors.authorization.ComponentProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.processors.authorization.ComponentProperties.GROUP_LIST;
import static com.thinkbiganalytics.nifi.processors.authorization.ComponentProperties.PERMISSION_LEVEL;
import static com.thinkbiganalytics.nifi.processors.authorization.ComponentProperties.THRIFT_SERVICE;

@Tags({"sentry,authorization,thinkbig"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class CreateSentryAuthorizationPolicy extends AbstractProcessor {

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
			sentryUtil.createPolicy(stmt,group_list,category,feed);

			session.transfer(flowFile, Success);

		} catch (final ProcessException | SQLException e) {
			logger.error("Unable to obtain connection for {} due to {}; routing to failure", new Object[]{flowFile, e});
			session.transfer(flowFile, Failure);
		}

	}


}


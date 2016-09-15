/**
 * @author sv186029
 */
package com.thinkbiganalytics.nifi.authorization.ranger.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import com.thinkbiganalytics.datalake.ranger.rest.client.RangerRestClient;
import com.thinkbiganalytics.datalake.ranger.rest.client.RangerRestClientConfig;

@Tags({ "ranger,athorization,thinkbig"})
@CapabilityDescription("Ranger authorization service for PCNG.")
public class RangerControllerService extends AbstractControllerService implements RangerService {

	private RangerRestClient rangerClientObj;

	public static final PropertyDescriptor IMPLEMENTATION = new PropertyDescriptor.Builder()
			.name("Implementation")
			.description("Specifies which implementation of the metadata providers should be used")
			.allowableValues("REST")
			.defaultValue("REST")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.required(true)
			.build();

	public static final PropertyDescriptor CLIENT_HOSTNAME = new PropertyDescriptor.Builder()
			.name("rest-client-host")
			.displayName("Ranger REST Client Hostname")
			.description("Hostname to the Ranger server for the REST API client.")
			.defaultValue("localhost")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.required(true)
			.build();

	public static final PropertyDescriptor CLIENT_PORT = new PropertyDescriptor.Builder()
			.name("rest-client-port")
			.displayName("Ranger REST Client Port")
			.description("Port to the Ranger server for the REST API client.")
			.defaultValue("6080")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.required(true)
			.build();


	public static final PropertyDescriptor CLIENT_USERNAME = new PropertyDescriptor.Builder()
			.name("client-username")
			.displayName("Ranger REST Client User Name")
			.description("Optional user name if the client requires a credential")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.defaultValue("")
			.required(false)
			.build();

	public static final PropertyDescriptor CLIENT_PASSWORD = new PropertyDescriptor.Builder()
			.name("client-password")
			.displayName("Ranger REST Client Password")
			.description("Optional password if the client requires a credential")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.defaultValue("")
			.sensitive(true)
			.required(false)
			.build();

	private static final List<PropertyDescriptor> properties;

	static {
		final List<PropertyDescriptor> props = new ArrayList<>();
		props.add(IMPLEMENTATION);
		props.add(CLIENT_HOSTNAME);
		props.add(CLIENT_PORT);
		props.add(CLIENT_USERNAME);
		props.add(CLIENT_PASSWORD);
		properties = Collections.unmodifiableList(props);
	}

	@Override
	protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return properties;
	}

	/**
	 * @param context
	 *            the configuration context
	 * @throws InitializationException
	 *             if unable to create a ranger connection
	 */
	@OnEnabled
	public void onEnabled (final ConfigurationContext context) throws InitializationException {

		PropertyValue impl = context.getProperty(IMPLEMENTATION);
		String hostname = context.getProperty(CLIENT_HOSTNAME).getValue();
		int port = Integer.parseInt(context.getProperty(CLIENT_PORT).getValue());
		String username = context.getProperty(CLIENT_USERNAME).getValue();
		String password = context.getProperty(CLIENT_PASSWORD).getValue();

		if (impl.getValue().equalsIgnoreCase("REST")) 
		{
			RangerRestClientConfig  rangerClientConfiguration = new RangerRestClientConfig(hostname,username,password);
			rangerClientConfiguration.setPort(port);
			this.rangerClientObj = new RangerRestClient(rangerClientConfiguration);


		} else {
			throw new UnsupportedOperationException("Unable to get Ranger Connection Object " + impl.getValue());
		}

	}

	@OnDisabled
	public void shutdown() {

	}


	@Override
	public RangerRestClient getConnection() throws Exception {
		// TODO Auto-generated method stub
		return this.rangerClientObj;
	}



}

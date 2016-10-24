package com.thinkbiganalytics.nifi.security;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;

import javax.annotation.Nonnull;

/**
 * Common Controller Service or Processor properties for accessing Kerberos.
 */
public interface KerberosProperties {

    /** Defines the time interval for refreshing a Kerberos ticket */
    PropertyDescriptor KERBEROS_RELOGIN_PERIOD = new PropertyDescriptor.Builder()
            .name("Kerberos Relogin Period")
            .required(false)
            .description("Period of time which should pass before attempting a kerberos relogin")
            .defaultValue("4 hours")
            .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    /**
     * Creates a new property for the Kerberos service keytab.
     *
     * @return the Kerberos service keytab property
     */
    @Nonnull
    PropertyDescriptor createKerberosKeytabProperty();

    /**
     * Creates a new property for the Kerberos service principal.
     *
     * @return the Kerberos service principal property
     */
    @Nonnull
    PropertyDescriptor createKerberosPrincipalProperty();
}

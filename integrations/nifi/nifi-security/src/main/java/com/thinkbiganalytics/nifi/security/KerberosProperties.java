/*
 * Copyright (c) 2016. Teradata Inc.
 */


package com.thinkbiganalytics.nifi.security;

import org.apache.hadoop.conf.Configuration;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.util.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * All processors and controller services that need properties for Kerberos Principal and Keytab
 * should obtain them through this class by calling:
 *
 * KerberosProperties props = KerberosProperties.create(NiFiProperties.getInstance())
 *
 * The properties can be accessed from the resulting KerberosProperties instance.
 */
public class KerberosProperties {

    private final File kerberosConfigFile;
    private final Validator kerberosConfigValidator;
    private final PropertyDescriptor kerberosPrincipal;
    private final PropertyDescriptor kerberosKeytab;


    private KerberosProperties(final File kerberosConfigFile) {
        this.kerberosConfigFile = kerberosConfigFile;

        if (this.kerberosConfigFile != null) {
            System.setProperty("java.security.krb5.conf", kerberosConfigFile.getAbsolutePath());
        }

        this.kerberosConfigValidator = new Validator() {
            @Override
            public ValidationResult validate(String subject, String input, ValidationContext context) {
                // Check that the Kerberos configuration is set
                if (kerberosConfigFile == null) {
                    return new ValidationResult.Builder()
                        .subject(subject).input(input).valid(false)
                        .explanation("you are missing the nifi.kerberos.krb5.file property which "
                                     + "must be set in order to use Kerberos")
                        .build();
                }

                // Check that the Kerberos configuration is readable
                if (!kerberosConfigFile.canRead()) {
                    return new ValidationResult.Builder().subject(subject).input(input).valid(false)
                        .explanation(String.format("unable to read Kerberos config [%s], please make sure the path is valid "
                                                   + "and nifi has adequate permissions", kerberosConfigFile.getAbsoluteFile()))
                        .build();
                }

                return new ValidationResult.Builder().subject(subject).input(input).valid(true).build();
            }
        };

        this.kerberosPrincipal = new PropertyDescriptor.Builder()
            .name("Kerberos Principal")
            .required(false)
            .description("Kerberos principal to authenticate as. Requires nifi.kerberos.krb5.file to be set in your nifi.properties")
            .addValidator(kerberosConfigValidator)
            .build();

        this.kerberosKeytab = new PropertyDescriptor.Builder()
            .name("Kerberos Keytab").required(false)
            .description("Kerberos keytab associated with the principal. Requires nifi.kerberos.krb5.file to be set in your nifi.properties")
            .addValidator(StandardValidators.FILE_EXISTS_VALIDATOR)
            .addValidator(kerberosConfigValidator)
            .build();

    }

    public static KerberosProperties create(final NiFiProperties niFiProperties) {
        if (niFiProperties == null) {
            throw new IllegalArgumentException("NiFiProperties can not be null");
        }
        return new KerberosProperties(niFiProperties.getKerberosConfigurationFile());
    }

    public File getKerberosConfigFile() {
        return kerberosConfigFile;
    }

    public Validator getKerberosConfigValidator() {
        return kerberosConfigValidator;
    }

    public PropertyDescriptor getKerberosPrincipal() {
        return kerberosPrincipal;
    }

    public PropertyDescriptor getKerberosKeytab() {
        return kerberosKeytab;
    }


    public static List<ValidationResult> validatePrincipalAndKeytab(final String subject, final Configuration config, final String principal, final String keytab, final ComponentLog logger) {
        final List<ValidationResult> results = new ArrayList<>();

        // if security is enabled then the keytab and principal are required
        final boolean isSecurityEnabled = SecurityUtil.isSecurityEnabled(config);

        if (isSecurityEnabled && StringUtils.isBlank(principal)) {
            results.add(new ValidationResult.Builder()
                            .valid(false)
                            .subject(subject)
                            .explanation("Kerberos Principal must be provided when using a secure configuration")
                            .build());
        }

        if (isSecurityEnabled && StringUtils.isBlank(keytab)) {
            results.add(new ValidationResult.Builder()
                            .valid(false)
                            .subject(subject)
                            .explanation("Kerberos Keytab must be provided when using a secure configuration")
                            .build());
        }

        if (!isSecurityEnabled && (!StringUtils.isBlank(principal) || !StringUtils.isBlank(keytab))) {
            logger.warn("Configuration does not have security enabled, Keytab and Principal will be ignored");
        }

        return results;
    }

}
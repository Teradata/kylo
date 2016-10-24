package com.thinkbiganalytics.nifi.v1.security;

import com.thinkbiganalytics.nifi.security.AbstractKerberosProperties;

import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import javax.annotation.Nullable;

/**
 * Provides Kerberos properties for NiFi v1.0.
 */
public class KerberosPropertiesV1 extends AbstractKerberosProperties {

    /** Context for the controller service using these Kerberos properties */
    @Autowired(required = false)
    @Nullable
    private ControllerServiceInitializationContext controllerServiceContext;

    /** Context for the processor using these Kerberos properties */
    @Autowired(required = false)
    @Nullable
    private ProcessorInitializationContext processorContext;

    @Nullable
    @Override
    protected File getKerberosConfigurationFile() {
        final File kerberosConfigurationFile;
        if (controllerServiceContext != null) {
            kerberosConfigurationFile = controllerServiceContext.getKerberosConfigurationFile();
        } else if (processorContext != null) {
            kerberosConfigurationFile = processorContext.getKerberosConfigurationFile();
        } else {
            kerberosConfigurationFile = null;
        }

        if (kerberosConfigurationFile != null) {
            System.setProperty("java.security.krb5.conf", kerberosConfigurationFile.getAbsolutePath());
        }

        return kerberosConfigurationFile;
    }
}

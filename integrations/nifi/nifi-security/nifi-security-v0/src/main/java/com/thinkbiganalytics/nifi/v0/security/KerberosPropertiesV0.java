package com.thinkbiganalytics.nifi.v0.security;

import com.thinkbiganalytics.nifi.security.AbstractKerberosProperties;

import org.apache.nifi.util.NiFiProperties;

import java.io.File;

import javax.annotation.Nullable;

/**
 * Provides Kerberos properties for NiFi v0.6.
 */
public class KerberosPropertiesV0 extends AbstractKerberosProperties {

    @Nullable
    @Override
    protected File getKerberosConfigurationFile() {
        final File kerberosConfigurationFile = NiFiProperties.getInstance().getKerberosConfigurationFile();
        if (kerberosConfigurationFile != null) {
            System.setProperty("java.security.krb5.conf", kerberosConfigurationFile.getAbsolutePath());
        }
        return kerberosConfigurationFile;
    }
}

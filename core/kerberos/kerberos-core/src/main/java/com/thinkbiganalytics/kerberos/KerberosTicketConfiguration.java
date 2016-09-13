package com.thinkbiganalytics.kerberos;

/**
 * Created by Jeremy Merrifield on 8/30/16.
 */
public class KerberosTicketConfiguration {

    private boolean kerberosEnabled;
    private String hadoopConfigurationResources;
    private String kerberosPrincipal;
    private String keytabLocation;

    public String getKeytabLocation() {
        return keytabLocation;
    }

    public void setKeytabLocation(String keytabLocation) {
        this.keytabLocation = keytabLocation;
    }

    public boolean isKerberosEnabled() {
        return kerberosEnabled;
    }

    public void setKerberosEnabled(boolean kerberosEnabled) {
        this.kerberosEnabled = kerberosEnabled;
    }

    public String getHadoopConfigurationResources() {
        return hadoopConfigurationResources;
    }

    public void setHadoopConfigurationResources(String hadoopConfigurationResources) {
        this.hadoopConfigurationResources = hadoopConfigurationResources;
    }

    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }

    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }

}

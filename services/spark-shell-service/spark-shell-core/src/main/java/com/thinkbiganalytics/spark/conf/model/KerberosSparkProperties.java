package com.thinkbiganalytics.spark.conf.model;

/*-
 * #%L
 * Spark Shell Core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Properties for acquiring a Kerberos ticket.
 */
public class KerberosSparkProperties {

    /**
     * Seconds to cache a Kerberos ticket
     */
    private int initInterval = 43200;

    /**
     * Seconds to wait for acquiring a ticket
     */
    private int initTimeout = 10;

    /**
     * Enables or disables Kerberos authentication
     */
    private boolean kerberosEnabled = false;

    /**
     * Name of the principal for acquiring a Kerberos ticket
     */
    private String kerberosPrincipal;

    /**
     * Local path to the keytab for acquiring a Kerberos ticket
     */
    private String keytabLocation;

    /**
     * Name of the Kerberos realm to append to usernames
     */
    private String realm;

    /**
     * Seconds to wait for acquiring a Kerberos ticket
     */
    private int retryInterval = 120;

    public int getInitInterval() {
        return initInterval;
    }

    public void setInitInterval(int initInterval) {
        this.initInterval = initInterval;
    }

    public int getInitTimeout() {
        return initTimeout;
    }

    public void setInitTimeout(int initTimeout) {
        this.initTimeout = initTimeout;
    }

    public boolean isKerberosEnabled() {
        return kerberosEnabled;
    }

    public void setKerberosEnabled(boolean kerberosEnabled) {
        this.kerberosEnabled = kerberosEnabled;
    }

    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }

    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }

    public String getKeytabLocation() {
        return keytabLocation;
    }

    public void setKeytabLocation(String keytabLocation) {
        this.keytabLocation = keytabLocation;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
}

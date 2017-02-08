package com.thinkbiganalytics.nifi.security;

/*-
 * #%L
 * thinkbig-nifi-security-api
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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;

import javax.annotation.Nonnull;

/**
 * Common Controller Service or Processor properties for accessing Kerberos.
 */
public interface KerberosProperties {

    /**
     * Defines the time interval for refreshing a Kerberos ticket
     */
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

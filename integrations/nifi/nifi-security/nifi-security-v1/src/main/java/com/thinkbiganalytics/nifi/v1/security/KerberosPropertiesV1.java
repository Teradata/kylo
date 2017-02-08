package com.thinkbiganalytics.nifi.v1.security;

/*-
 * #%L
 * thinkbig-nifi-security-v1
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

    /**
     * Context for the controller service using these Kerberos properties
     */
    @Autowired(required = false)
    @Nullable
    private ControllerServiceInitializationContext controllerServiceContext;

    /**
     * Context for the processor using these Kerberos properties
     */
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

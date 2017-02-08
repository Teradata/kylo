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
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides a standard implementation of {@link KerberosProperties}.
 */
public abstract class AbstractKerberosProperties implements KerberosProperties {

    @Nonnull
    @Override
    public PropertyDescriptor createKerberosKeytabProperty() {
        return new PropertyDescriptor.Builder()
            .name("Kerberos Keytab")
            .required(false)
            .description("Kerberos keytab associated with the principal. Requires nifi.kerberos.krb5.file to be set in your nifi.properties.")
            .addValidator(StandardValidators.FILE_EXISTS_VALIDATOR)
            .addValidator(new KerberosConfigurationValidator(getKerberosConfigurationFile()))
            .build();
    }

    @Nonnull
    @Override
    public PropertyDescriptor createKerberosPrincipalProperty() {
        return new PropertyDescriptor.Builder()
            .name("Kerberos Principal")
            .required(false)
            .description("Kerberos principal to authenticate as. Requires nifi.kerberos.krb5.file to be set in your nifi.properties.")
            .addValidator(new KerberosConfigurationValidator(getKerberosConfigurationFile()))
            .build();
    }

    /**
     * Gets the Kerberos configuration file (typically krb5.conf) that will be used by this JVM during all Kerberos operations.
     *
     * @return the Kerberos configuration file
     */
    @Nullable
    protected abstract File getKerberosConfigurationFile();

    /**
     * Validates that the Kerberos configuration has been set.
     */
    private static class KerberosConfigurationValidator implements Validator {

        /**
         * Kerberos configuration file
         */
        private final File configuration;

        /**
         * Constructs a {@code KerberosConfigurationValidator} with the specified Kerberos configuration.
         *
         * @param configuration the Kerberos configuration file
         */
        public KerberosConfigurationValidator(@Nullable final File configuration) {
            this.configuration = configuration;
        }

        @Override
        public ValidationResult validate(String subject, String input, ValidationContext context) {
            // Check that the Kerberos configuration is set
            if (configuration == null) {
                return new ValidationResult.Builder()
                    .subject(subject).input(input).valid(false)
                    .explanation("you are missing the nifi.kerberos.krb5.file property which "
                                 + "must be set in order to use Kerberos")
                    .build();
            }

            // Check that the Kerberos configuration is readable
            if (!configuration.canRead()) {
                return new ValidationResult.Builder().subject(subject).input(input).valid(false)
                    .explanation(String.format("unable to read Kerberos config [%s], please make sure the path is valid "
                                               + "and nifi has adequate permissions", configuration.getAbsoluteFile()))
                    .build();
            }

            return new ValidationResult.Builder().subject(subject).input(input).valid(true).build();
        }
    }
}

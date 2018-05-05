package com.thinkbiganalytics.kylo.nifi.teradata.tdch.api;

/*-
 * #%L
 * kylo-nifi-teradata-tdch-api
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.reporting.InitializationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Tests for interface {@link TdchConnectionService}
 */
public class TdchConnectionServiceApiTest {

    @Test
    public void testTdchConnectionServiceApi () {
        TdchConnectionService tdchConnectionService = new TdchConnectionServiceImpl();
        Assert.assertNotNull(tdchConnectionService);
        Assert.assertEquals("driver.class", tdchConnectionService.getJdbcDriverClassName());
    }

    static class TdchConnectionServiceImpl implements TdchConnectionService {

        @Override
        public String getJdbcDriverClassName() {
            return "driver.class";
        }

        @Override
        public String getJdbcConnectionUrl() {
            return null;
        }

        @Override
        public String getUserName() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getTdchJarPath() {
            return null;
        }

        @Override
        public String getTdchLibraryJarsPath() {
            return null;
        }

        @Override
        public String getTdchHadoopClassPath() {
            return null;
        }

        @Override
        public void initialize(ControllerServiceInitializationContext context) throws InitializationException {

        }

        @Override
        public Collection<ValidationResult> validate(ValidationContext context) {
            return null;
        }

        @Override
        public PropertyDescriptor getPropertyDescriptor(String name) {
            return null;
        }

        @Override
        public void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) {

        }

        @Override
        public List<PropertyDescriptor> getPropertyDescriptors() {
            return null;
        }

        @Override
        public String getIdentifier() {
            return null;
        }
    }
}

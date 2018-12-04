package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests related to {@link NifiPropertyUtil} class
 */
public class TestNifiPropertyUtil {

    private static final Logger log = LoggerFactory.getLogger(TestNifiPropertyUtil.class);


    @Test
    public void testNiFiPropertyRegistrationChangeInfo_Case_1() {
        NifiProperty latestPropertyFromNifi = new NifiProperty();
        NifiProperty registeredPropertyInKylo = new NifiProperty();

        latestPropertyFromNifi.setKey("var_a");
        latestPropertyFromNifi.setValue("two");

        registeredPropertyInKylo.setKey("var_a");
        registeredPropertyInKylo.setTemplateValue("one");
        registeredPropertyInKylo.setValue("one");

        registeredPropertyInKylo.setUserEditable(true);
        registeredPropertyInKylo.setSelected(true);
        registeredPropertyInKylo.setRenderType("text");
        registeredPropertyInKylo.setSensitive(false);
        registeredPropertyInKylo.setRequired(false);

        NifiPropertyUtil.NiFiPropertyUpdater.updateSkipInput(latestPropertyFromNifi, registeredPropertyInKylo);
        Assert.assertEquals("two", latestPropertyFromNifi.getValue());
    }

    @Test
    public void testNiFiPropertyRegistrationChangeInfo_Case_2() {
        NifiProperty latestPropertyFromNifi = new NifiProperty();
        NifiProperty registeredPropertyInKylo = new NifiProperty();

        latestPropertyFromNifi.setKey("var_a");
        latestPropertyFromNifi.setValue("two");

        registeredPropertyInKylo.setKey("var_a");
        registeredPropertyInKylo.setTemplateValue("one");
        registeredPropertyInKylo.setValue("one first");

        registeredPropertyInKylo.setUserEditable(true);
        registeredPropertyInKylo.setSelected(true);
        registeredPropertyInKylo.setRenderType("text");
        registeredPropertyInKylo.setSensitive(false);
        registeredPropertyInKylo.setRequired(false);

        NifiPropertyUtil.NiFiPropertyUpdater.updateSkipInput(latestPropertyFromNifi, registeredPropertyInKylo);
        Assert.assertEquals("one first", latestPropertyFromNifi.getValue());
    }

    @Test
    public void testNiFiPropertyRegistrationChangeInfo_Case_3() {
        NifiProperty latestPropertyFromNifi = new NifiProperty();
        NifiProperty registeredPropertyInKylo = new NifiProperty();

        latestPropertyFromNifi.setKey("var_a");
        latestPropertyFromNifi.setValue("one");

        registeredPropertyInKylo.setKey("var_a");
        registeredPropertyInKylo.setTemplateValue("one");
        registeredPropertyInKylo.setValue("one");

        registeredPropertyInKylo.setUserEditable(true);
        registeredPropertyInKylo.setSelected(true);
        registeredPropertyInKylo.setRenderType("text");
        registeredPropertyInKylo.setSensitive(false);
        registeredPropertyInKylo.setRequired(false);

        NifiPropertyUtil.NiFiPropertyUpdater.updateSkipInput(latestPropertyFromNifi, registeredPropertyInKylo);
        Assert.assertEquals("one", latestPropertyFromNifi.getValue());
    }

    @Test
    public void testUpdateCore_WithModeUpdateAllProperties() {
        final String VALUE = "value";

        NifiProperty propertyToUpdate = new NifiProperty();
        propertyToUpdate.setValue(null);

        NifiProperty property = new NifiProperty();
        property.setKey("key");
        property.setTemplateValue("templateValue");
        property.setValue(VALUE);
        property.setUserEditable(true);
        property.setSelected(true);
        property.setRenderType("text");
        property.setRequired(true);
        property.setPropertyDescriptor(null);
        property.setRenderOptions(null);

        NifiPropertyUtil.PropertyUpdateMode propertyUpdateMode = NifiPropertyUtil.PropertyUpdateMode.UPDATE_ALL_PROPERTIES;

        NifiPropertyUtil.NiFiPropertyUpdater.updateCore(propertyToUpdate, property, propertyUpdateMode);

        Assert.assertEquals(VALUE, propertyToUpdate.getValue());
    }

    @Test
    public void testUpdateCore_WithModeNotUpdateAllProperties() {
        final String VALUE = "value";

        NifiProperty propertyToUpdate = new NifiProperty();
        propertyToUpdate.setValue(null);

        NifiProperty property = new NifiProperty();
        property.setKey("key");
        property.setTemplateValue("templateValue");
        property.setValue(VALUE);
        property.setUserEditable(true);
        property.setSelected(true);
        property.setRenderType("text");
        property.setRequired(true);
        property.setPropertyDescriptor(null);
        property.setRenderOptions(null);

        NifiPropertyUtil.PropertyUpdateMode propertyUpdateMode = NifiPropertyUtil.PropertyUpdateMode.FEED_DETAILS_MATCH_TEMPLATE;

        NifiPropertyUtil.NiFiPropertyUpdater.updateCore(propertyToUpdate, property, propertyUpdateMode);

        Assert.assertEquals(null, propertyToUpdate.getValue());
    }

}

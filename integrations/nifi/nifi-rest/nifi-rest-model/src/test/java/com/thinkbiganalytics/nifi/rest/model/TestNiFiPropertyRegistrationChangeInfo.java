package com.thinkbiganalytics.nifi.rest.model;

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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link NiFiPropertyRegistrationChangeInfo} class
 */
public class TestNiFiPropertyRegistrationChangeInfo {

    @Test
    public void testNiFiPropertyRegistrationChangeInfo_1() {
        NiFiPropertyRegistrationChangeInfo niFiPropertyRegistrationChangeInfo = new NiFiPropertyRegistrationChangeInfo();
        niFiPropertyRegistrationChangeInfo.setKey("key");
        niFiPropertyRegistrationChangeInfo.setValueFromOlderNiFiTemplate("value-from-old-nifi");
        niFiPropertyRegistrationChangeInfo.setValueFromNewerNiFiTemplate("value-from-new-nifi");
        niFiPropertyRegistrationChangeInfo.setValueRegisteredInKyloTemplateFromOlderNiFiTemplate("value-from-kylo");

        Assert.assertEquals("key", niFiPropertyRegistrationChangeInfo.getKey());
        Assert.assertEquals("value-from-old-nifi", niFiPropertyRegistrationChangeInfo.getValueFromOlderNiFiTemplate());
        Assert.assertEquals("value-from-new-nifi", niFiPropertyRegistrationChangeInfo.getValueFromNewerNiFiTemplate());
        Assert.assertEquals("value-from-kylo", niFiPropertyRegistrationChangeInfo.getValueRegisteredInKyloTemplateFromOlderNiFiTemplate());
    }

    @Test
    public void testNiFiPropertyRegistrationChangeInfo_2() {
        NiFiPropertyRegistrationChangeInfo niFiPropertyRegistrationChangeInfo = new NiFiPropertyRegistrationChangeInfo("key", "value-from-old-nifi", "value-from-new-nifi", "value-from-kylo");
        Assert.assertEquals("key", niFiPropertyRegistrationChangeInfo.getKey());
        Assert.assertEquals("value-from-old-nifi", niFiPropertyRegistrationChangeInfo.getValueFromOlderNiFiTemplate());
        Assert.assertEquals("value-from-new-nifi", niFiPropertyRegistrationChangeInfo.getValueFromNewerNiFiTemplate());
        Assert.assertEquals("value-from-kylo", niFiPropertyRegistrationChangeInfo.getValueRegisteredInKyloTemplateFromOlderNiFiTemplate());
    }

}

package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-common-util
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
 * Test the naming/versioning of the process groups
 */
public class TestNifiTemplateNameUtil {


    /**
     * Test to ensure the group gets the correct version timestamp and that it is able to be parsed
     */
    @Test
    public void testProcessGroupNameVersioning() {
        String processGroupName = "my group";
        Assert.assertFalse(NifiTemplateNameUtil.isVersionedProcessGroup(processGroupName));
        String versionedName = NifiTemplateNameUtil.getVersionedProcessGroupName(processGroupName);
        String versionedName2 = NifiTemplateNameUtil.getVersionedProcessGroupName(processGroupName,"v1234");
        Assert.assertTrue(NifiTemplateNameUtil.isVersionedProcessGroup(versionedName));
        Assert.assertTrue(NifiTemplateNameUtil.isVersionedProcessGroup(versionedName2));
        Assert.assertEquals(processGroupName, NifiTemplateNameUtil.parseVersionedProcessGroupName(versionedName));
        Assert.assertEquals(processGroupName, NifiTemplateNameUtil.parseVersionedProcessGroupName(versionedName2));
        Assert.assertEquals("v1234", NifiTemplateNameUtil.getKyloVersionIdentifier(versionedName2));
    }

}

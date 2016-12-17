package com.thinkbiganalytics.nifi.rest.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by sr186054 on 12/16/16.
 */
public class TestNifiTemplateNameUtil {


    @Test
    public void testProcessGroupNameVersioning(){
        String processGroupName = "my group";
        Assert.assertFalse(NifiTemplateNameUtil.isVersionedProcessGroup(processGroupName));
        String versionedName =  NifiTemplateNameUtil.getVersionedProcessGroupName(processGroupName);
        Assert.assertTrue(NifiTemplateNameUtil.isVersionedProcessGroup(versionedName));
        Assert.assertEquals(processGroupName,NifiTemplateNameUtil.parseVersionedProcessGroupName(versionedName));
    }

}

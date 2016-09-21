package com.thinkbiganalytics.metadata.api.security;

import com.thinkbiganalytics.metadata.api.BaseProvider;

/**
 * Created by Jeremy Merrifield on 9/20/16.
 */
public interface HadoopSecurityGroupProvider extends BaseProvider<HadoopSecurityGroup, HadoopSecurityGroup.ID> {

    HadoopSecurityGroup ensureSecurityGroup(String name);
}

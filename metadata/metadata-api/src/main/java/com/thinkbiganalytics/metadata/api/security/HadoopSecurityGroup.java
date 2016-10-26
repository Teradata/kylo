package com.thinkbiganalytics.metadata.api.security;

import java.io.Serializable;

/**
 * Created by Jeremy Merrifield on 9/20/16.
 */
public interface HadoopSecurityGroup {

    interface ID extends Serializable { }

    ID getId();

    String getGroupId();

    String getName();

    String getDescription();
}

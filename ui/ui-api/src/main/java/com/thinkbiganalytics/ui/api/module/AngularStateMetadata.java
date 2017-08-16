package com.thinkbiganalytics.ui.api.module;

import java.util.Map;

/**
 * Created by sr186054 on 8/15/17.
 */
public interface AngularStateMetadata {

    String getState();

    String getUrl();

    Map<String,Object> getParams();


}

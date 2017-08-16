package com.thinkbiganalytics.ui.service;

import org.springframework.core.io.Resource;

/**
 * Created by sr186054 on 8/16/17.
 */
public interface LoadResourceCallback<R> {

        void execute(Resource resource);

}

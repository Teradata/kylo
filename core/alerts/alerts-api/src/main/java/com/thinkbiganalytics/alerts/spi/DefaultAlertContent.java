package com.thinkbiganalytics.alerts.spi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 8/24/17.
 */
public class DefaultAlertContent implements Serializable {

    Map<String,Object> content = new HashMap<>();

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }
}

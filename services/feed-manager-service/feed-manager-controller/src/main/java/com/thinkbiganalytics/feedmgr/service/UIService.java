package com.thinkbiganalytics.feedmgr.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 1/13/16.
 */
public class UIService {

    private static class LazyHolder {

        static final UIService INSTANCE = new UIService();
    }

    public static UIService getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Map<String, String> supportedCodeMirrorTypes = new HashMap<>();


    private UIService() {
        registerCodeMirrorType("text/x-pig", "Pig");
        registerCodeMirrorType("text/x-sql", "Sql");
        registerCodeMirrorType("text/x-mysql", "MySql");
        registerCodeMirrorType("text/x-hive", "Hive");
        registerCodeMirrorType("shell", "Shell");
        registerCodeMirrorType("text/x-cython", "Python");
        registerCodeMirrorType("xml", "Xml");
        registerCodeMirrorType("text/x-groovy", "Groovy");
        registerCodeMirrorType("text/x-properties", "Properties");

    }

    public Map<String, String> getCodeMirrorTypes() {
        return supportedCodeMirrorTypes;
    }


    public void registerCodeMirrorType(String type, String name) {
        supportedCodeMirrorTypes.put(type, name);
    }

}

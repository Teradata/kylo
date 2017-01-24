package com.thinkbiganalytics.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TableRegisterConfiguration {

    /*
    Note: These defaults exist for legacy support and are typically overridden in the service-app modules application.properties
     */
    private final String DEFAULT_FEED = "/model.db/";
    private final String DEFAULT_MASTER = "/app/warehouse/";
    private final String DEFAULT_PROFILE = "/model.db/";

    private String feedRoot;
    private String profileRoot;
    private String masterRoot;

    public TableRegisterConfiguration() {
        this(null, null, null);
    }

    public TableRegisterConfiguration(String feedRoot, String profileRoot, String masterRoot) {
        super();
        this.feedRoot = StringUtils.defaultIfEmpty(feedRoot, DEFAULT_FEED);
        this.profileRoot = StringUtils.defaultIfEmpty(profileRoot, DEFAULT_PROFILE);
        this.masterRoot = StringUtils.defaultIfEmpty(masterRoot, DEFAULT_MASTER);
    }

    public Path pathForTableType(TableType type) {
        switch (type) {
            case FEED:
            case INVALID:
            case VALID:
                return Paths.get(feedRoot);
            case PROFILE:
                return Paths.get(profileRoot);
            case MASTER:
                return Paths.get(masterRoot);
            default:
                throw new RuntimeException("Unsupported table type [" + type.toString() + "]. Needs to be added to class?");
        }
    }
}

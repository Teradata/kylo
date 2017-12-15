package com.thinkbiganalytics.feedmgr.service.feed.importing.model;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ImportFeedOptions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.json.ObjectMapperSerializer;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by sr186054 on 12/13/17.
 */
public class ImportFeed {

    public static final String FEED_JSON_FILE = "feed.json";
    private boolean valid;

    private boolean success;
    private String fileName;
    private String feedName;
    private ImportTemplate template;
    private NifiFeed nifiFeed;
    private String feedJson;
    private ImportFeedOptions importOptions;

    @JsonIgnore
    private FeedMetadata feedToImport;

    public ImportFeed() {
    }

    public ImportFeed(String fileName) {
        this.fileName = fileName;
        this.template = new ImportTemplate(fileName);
    }

    public String getFeedJson() {
        return feedJson;
    }

    public void setFeedJson(String feedJson) {
        this.feedJson = feedJson;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ImportTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ImportTemplate template) {
        this.template = template;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public NifiFeed getNifiFeed() {
        return nifiFeed;
    }

    public void setNifiFeed(NifiFeed nifiFeed) {
        this.nifiFeed = nifiFeed;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void addErrorMessage(FeedMetadata feedMetadata, String errorMessage) {
        if (nifiFeed == null) {
            nifiFeed = new NifiFeed(feedMetadata, null);
        }
        nifiFeed.addErrorMessage(errorMessage);
    }

    public ImportFeedOptions getImportOptions() {
        return importOptions;
    }

    public void setImportOptions(ImportFeedOptions importOptions) {
        this.importOptions = importOptions;
    }

    @JsonIgnore
    public FeedMetadata getFeedToImport() {
        if (feedToImport == null && StringUtils.isNotBlank(feedJson)) {
            feedToImport = ObjectMapperSerializer.deserialize(getFeedJson(), FeedMetadata.class);
        }
        return feedToImport;
    }

    @JsonIgnore
    public void setFeedToImport(FeedMetadata feedToImport) {
        this.feedToImport = feedToImport;
    }
}

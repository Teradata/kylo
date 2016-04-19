package com.thinkbiganalytics.feedmgr.service;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 2/23/16.
 */
@Service
public interface MetadataService {
    void registerTemplate(RegisteredTemplate registeredTemplate);

    List<NifiProperty> getTemplateProperties(String templateId);

    RegisteredTemplate getRegisteredTemplate(String templateId);
    RegisteredTemplate getRegisteredTemplateByName(String templateName);

    RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException;

    RegisteredTemplate getRegisteredTemplateForNifiProperties(final String nifiTemplateId, final String nifiTemplateName);

    List<String> getRegisteredTemplateIds();

    List<RegisteredTemplate> getRegisteredTemplates();

    void saveFeed(FeedMetadata feed);

    Collection<FeedMetadata> getFeeds();

    Collection<? extends UIFeed> getFeeds(boolean verbose);

    List<FeedSummary> getFeedSummaryData();

    List<FeedSummary> getFeedSummaryForCategory(Long categoryId);

    FeedMetadata getFeed(String feedName);

    FeedMetadata getFeed(Long feedId);

    Collection<FeedCategory> getCategories();

    void saveCategory(FeedCategory category);

    boolean deleteCategory(Long categoryId) throws InvalidOperationException;
}

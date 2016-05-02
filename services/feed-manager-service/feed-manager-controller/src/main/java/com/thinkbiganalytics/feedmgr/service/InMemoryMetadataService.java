package com.thinkbiganalytics.feedmgr.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.nifi.web.api.dto.PortDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.rest.JerseyClientException;

/**
 * Created by sr186054 on 1/13/16.
 */
public class InMemoryMetadataService implements MetadataService {

  @Inject
  FeedManagerCategoryProvider categoryProvider;

  @Inject
  FeedManagerTemplateProvider templateProvider;

  @Inject
  FeedManagerFeedProvider feedProvider;



    public InMemoryMetadataService(){


    }

  @Override
  public void registerTemplate(RegisteredTemplate registeredTemplate) {
    templateProvider.registerTemplate(registeredTemplate);
  }

  @Override
  public List<NifiProperty> getTemplateProperties(String templateId) {
    return templateProvider.getTemplateProperties(templateId);
  }

  @Override
  public RegisteredTemplate getRegisteredTemplate(String templateId) {
    return templateProvider.getRegisteredTemplate(templateId);
  }

  @Override
  public RegisteredTemplate getRegisteredTemplateByName(String templateName) {
    return templateProvider.getRegisteredTemplateByName(templateName);
  }

  @Override
  public RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException {
    return templateProvider.getRegisteredTemplateWithAllProperties(templateId);
  }

  @Override
  public RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName) {
    return templateProvider.getRegisteredTemplateForNifiProperties(nifiTemplateId,nifiTemplateName);
  }

  @Override
  public List<String> getRegisteredTemplateIds() {
    return templateProvider.getRegisteredTemplateIds();
  }

  @Override
  public List<RegisteredTemplate> getRegisteredTemplates() {
    return templateProvider.getRegisteredTemplates();
  }

  @Override
  public NifiFeed createFeed(FeedMetadata feedMetadata) throws JerseyClientException {
    return feedProvider.createFeed(feedMetadata);
  }

  @Override
  public void saveFeed(FeedMetadata feed) {
    feedProvider.saveFeed(feed);
  }

  @Override
  public Collection<FeedMetadata> getFeeds() {
    return feedProvider.getFeeds();
  }

  @Override
  public Collection<? extends UIFeed> getFeeds(boolean verbose) {
    return feedProvider.getFeeds(verbose);
  }

  @Override
  public List<FeedSummary> getFeedSummaryData() {
    return feedProvider.getFeedSummaryData();
  }

  @Override
  public List<FeedSummary> getFeedSummaryForCategory(String categoryId) {
    return feedProvider.getFeedSummaryForCategory(categoryId);
  }

  @Override
  public FeedMetadata getFeedByName(String feedName) {
    return feedProvider.getFeedByName(feedName);
  }

  @Override
  public FeedMetadata getFeedById(String feedId) {
    return feedProvider.getFeedById(feedId);
  }

  @Override
  public List<FeedMetadata> getReusableFeeds() {
    return feedProvider.getReusableFeeds();
  }

  @Override
  public Collection<FeedCategory> getCategories() {
    return categoryProvider.getCategories();
  }

  @Override
  public FeedCategory getCategoryByName(String name) {
    return categoryProvider.getCategoryByName(name);
  }

  @Override
  public FeedCategory getCategoryBySystemName(String name) {
    return categoryProvider.getCategoryBySystemName(name);
  }

  @Override
  public void saveCategory(FeedCategory category) {
categoryProvider.saveCategory(category);
  }

  @Override
  public boolean deleteCategory(String categoryId) throws InvalidOperationException {
    return categoryProvider.deleteCategory(categoryId);
  }
}

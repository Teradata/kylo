 package com.thinkbiganalytics.feedmgr.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.nifi.feedmgr.CreateFeedBuilder;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

 /**
 * Created by sr186054 on 5/1/16.
 */
public class InMemoryFeedManagerFeedProvider implements FeedManagerFeedProvider {

  @Inject
  private NifiRestClient nifiRestClient;

   @Inject
  private FeedManagerCategoryProvider categoryProvider;

   @Inject
  private FeedManagerTemplateProvider templateProvider;

  @Autowired
  @Qualifier("metadataClient")
  MetadataClient metadataClient;



  @Autowired
  PreconditionFactory preconditionFactory;

  private Map<String, FeedMetadata> feeds = new HashMap<>();



  @PostConstruct
  private void postConstruct(){
    Collection<FeedMetadata>savedFeeds = FileObjectPersistence.getInstance().getFeedsFromFile();
    if(savedFeeds != null){
      Long maxId = 0L;
      for(FeedMetadata feed: savedFeeds){

        //update the category mappings
        String categoryId = feed.getCategory().getId();
        FeedCategory category = categoryProvider.getCategoryById(categoryId);
        feed.setCategory(category);
        category.addRelatedFeed(feed);
        //add it to the map
        feeds.put(feed.getId(), feed);
      }

      loadSavedFeedsToMetaClientStore();
    }

  }



  public Collection<FeedMetadata> getFeeds() {
    return feeds.values();
  }

  public Collection<? extends UIFeed> getFeeds( boolean verbose) {
    if(verbose){
      return getFeeds();
    }
    else {
      return getFeedSummaryData();
    }

  }

  public List<FeedSummary> getFeedSummaryData() {
    List<FeedSummary> summaryList = new ArrayList<>();
    if(feeds != null && !feeds.isEmpty()) {
      for(FeedMetadata feed: feeds.values()){
        summaryList.add(new FeedSummary(feed));
      }
    }
    return summaryList;
  }


  /**
   * Get a list of all the feeds which use a Template designated as being reusable
   * @return
   */
  @Override
  public List<FeedMetadata> getReusableFeeds() {
    return Lists.newArrayList(Iterables.filter(feeds.values(), new Predicate<FeedMetadata>() {
      @Override
      public boolean apply(FeedMetadata feedMetadata) {
        return feedMetadata.isReusableFeed();
      }
    }));
  }

  public List<FeedSummary> getFeedSummaryForCategory(String categoryId){
    List<FeedSummary> summaryList = new ArrayList<>();
    FeedCategory category = categoryProvider.getCategoryById(categoryId);
    if(category != null){
      for(FeedMetadata feed: category.getFeeds()){
        summaryList.add(new FeedSummary(feed));
      }
    }
    return summaryList;
  }


  @Override
  public FeedMetadata getFeedByName(final String feedName) {

    if(feeds != null && !feeds.isEmpty()) {
      return Iterables.tryFind(feeds.values(), new Predicate<FeedMetadata>() {
        @Override
        public boolean apply(FeedMetadata metadata) {
          return metadata.getFeedName().equalsIgnoreCase(feedName);
        }
      }).orNull();
    }
    return feeds.get(feedName);
  }

  @Override
  public FeedMetadata getFeedById(String id) {

    if(feeds != null && !feeds.isEmpty()) {
      FeedMetadata feed = feeds.get(id);
      if(feed != null) {
        //get the latest category data
        FeedCategory category = categoryProvider.getCategoryById(feed.getCategory().getId());
        feed.setCategory(category);

        //set the template to the feed

        RegisteredTemplate registeredTemplate = templateProvider.getRegisteredTemplate(feed.getTemplateId());
        if(registeredTemplate == null){
          registeredTemplate = templateProvider.getRegisteredTemplateByName(feed.getTemplateName());
        }
        if(registeredTemplate != null) {
          RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
          copy.getProperties().clear();
          feed.setRegisteredTemplate(copy);
          feed.setTemplateId(copy.getNifiTemplateId());
        }

        return feed;
      }
    }

    return null;
  }




  @Override
  public List<FeedMetadata> getFeedsWithTemplate(final String registeredTemplateId) {

    return Lists.newArrayList(Iterables.filter(feeds.values(), new Predicate<FeedMetadata>() {
      @Override
      public boolean apply(FeedMetadata feed) {
        return feed.getTemplateId().equalsIgnoreCase(registeredTemplateId);
      }
    }));
  }

  /**
   * Needed to rewire feeds to ids in the server upon server start since the Feed Metadata store is in memory now
   */
  private void loadSavedFeedsToMetaClientStore(){
    for(FeedMetadata feedMetadata : feeds.values()) {
      feedMetadata.setFeedId(null);
      saveToMetadataStore(feedMetadata);
    }
  }


  @Override
  public NifiFeed createFeed(FeedMetadata feedMetadata) throws JerseyClientException {
    NifiFeed feed = null;
    //replace expressions with values
    if(feedMetadata.getTable() != null) {
      feedMetadata.getTable().updateMetadataFieldValues();
    }
    if(feedMetadata.getSchedule() != null) {
      feedMetadata.getSchedule().updateDependentFeedNamesString();
    }

    if(feedMetadata.getProperties() == null) {
      feedMetadata.setProperties(new ArrayList<NifiProperty>());
    }
    //get all the properties for the metadata
    RegisteredTemplate
        registeredTemplate = templateProvider.getRegisteredTemplateWithAllProperties(feedMetadata.getTemplateId());
    List<NifiProperty> matchedProperties =  NifiPropertyUtil
        .matchAndSetPropertyByIdKey(registeredTemplate.getProperties(), feedMetadata.getProperties());
    feedMetadata.setProperties(registeredTemplate.getProperties());
    //resolve any ${metadata.} properties
    List<NifiProperty> resolvedProperties = PropertyExpressionResolver.resolvePropertyExpressions(feedMetadata);

    //store all input related properties as well
    List<NifiProperty> inputProperties = NifiPropertyUtil
        .findInputProperties(registeredTemplate.getProperties());

    ///store only those matched and resolved in the final metadata store
    Set<NifiProperty> updatedProperties = new HashSet<>();
    updatedProperties.addAll(matchedProperties);
    updatedProperties.addAll(resolvedProperties);
    updatedProperties.addAll(inputProperties);
    feedMetadata.setProperties(new ArrayList<NifiProperty>(updatedProperties));


    CreateFeedBuilder
        feedBuilder = nifiRestClient.newFeedBuilder(registeredTemplate.getNifiTemplateId(), feedMetadata.getCategory().getSystemName(), feedMetadata.getFeedName());

    if(registeredTemplate.isReusableTemplate()){
      feedBuilder.setReusableTemplate(true);
      feedMetadata.setIsReusableFeed(true);
    }
    else {
      feedBuilder.inputProcessorType(feedMetadata.getInputProcessorType())
          .feedSchedule(feedMetadata.getSchedule()).properties( feedMetadata.getProperties());
      if(registeredTemplate.usesReusableTemplate())
      {
        ReusableTemplateConnectionInfo reusableInfo = registeredTemplate.getReusableTemplateConnections().get(0);
        //TODO change FeedBuilder to accept a List of ReusableTemplateConnectionInfo objects
        feedBuilder.reusableTemplateInputPortName(reusableInfo.getReusableTemplateInputPortName()).feedOutputPortName(reusableInfo.getFeedOutputPortName());
      }
    }
    NifiProcessGroup
        entity = feedBuilder.build();


    if (entity.isSuccess()) {
      feedMetadata.setNifiProcessGroupId(entity.getProcessGroupEntity().getProcessGroup().getId());
     // feedMetadata.setNifiProcessGroup(entity);
      Date createDate = new Date();
      feedMetadata.setCreateDate(createDate);
      feedMetadata.setUpdateDate(createDate);

      saveFeed(feedMetadata);
    }
    else {
      //rollback feed
    }
    feed = new NifiFeed(feedMetadata, entity);
    return feed;
  }


  @Override
  public void saveFeed(FeedMetadata feed) {
    if(feed.getId() == null || !feeds.containsKey(feed.getId())){
      feed.setId(UUID.randomUUID().toString());
      feed.setVersion(new Long(1));
    }
    else {
      FeedMetadata previousFeed  = feeds.get(feed.getId());
      feed.setId(previousFeed.getId());
      feed.setVersion(previousFeed.getVersion()+1L);
    }

    //match up the related category
    String categoryId = feed.getCategory().getId();
    FeedCategory category = null;
    if(categoryId != null){
      category = categoryProvider.getCategoryById(categoryId);
    }
    if(category == null){
      final String categoryName = feed.getCategory().getName();
      category = categoryProvider.getCategoryByName(categoryName);
      feed.setCategory(category);
    }
    if(category != null) {
      category.addRelatedFeed(feed);
    }

    saveToMetadataStore(feed);
    feeds.put(feed.getId(), feed);
    FileObjectPersistence.getInstance().writeFeedsToFile(feeds.values());
  }


  public void updateFeedsWithTemplate(String oldTemplateId, String newTemplateId){
    List<FeedMetadata> feedsToUpdate = getFeedsWithTemplate(oldTemplateId);
    if(feedsToUpdate != null && !feedsToUpdate.isEmpty()) {
      for (FeedMetadata feedMetadata : feedsToUpdate) {
        feedMetadata.setTemplateId(newTemplateId);
      }
      //save the feeds
      FileObjectPersistence.getInstance().writeFeedsToFile(feeds.values());
    }
  }

  /**
   * Saves the Feed and its preconditions to metadata store
   * @param feedMetadata
   */
  public void saveToMetadataStore(FeedMetadata feedMetadata){
    try {
      Feed metadataFeed = null;
      if (StringUtils.isNotBlank(feedMetadata.getFeedId())) {
        try {
          metadataFeed = metadataClient.getFeed(feedMetadata.getFeedId());
        } catch (Exception e) {

        }
      }
      if (metadataFeed == null) {
        String feedName = feedMetadata.getCategoryAndFeedName();
        metadataFeed = metadataClient.buildFeed(feedName).post();
      }

      //update preconditions

      List<GenericUIPrecondition> preconditions = feedMetadata.getSchedule().getPreconditions();
      if (preconditions != null) {
        preconditionFactory.applyFeedName(preconditions, metadataFeed.getSystemName());
        List<Metric> feedPreconditions = preconditionFactory.getPreconditions(preconditions);
        metadataFeed = metadataClient.setPrecondition(metadataFeed.getId(), feedPreconditions);
      }
      feedMetadata.setFeedId(metadataFeed.getId());
    }catch(Exception e){
      e.printStackTrace();
    }

  }


}

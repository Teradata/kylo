package com.thinkbiganalytics.feedmgr.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategoryBuilder;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 1/13/16.
 */
public class InMemoryMetadataService implements MetadataService {

    @Autowired
    private NifiRestClient nifiRestClient;

    @Autowired
    @Qualifier("metadataClient")
    MetadataClient metadataClient;

    @Autowired
    PreconditionFactory preconditionFactory;

    private static AtomicLong feedCounter = new AtomicLong(0);

    private static AtomicLong categoryCounter = new AtomicLong(0);

    private Map<String, RegisteredTemplate> registeredTemplates = new HashMap<>();

    private Map<Long, FeedMetadata> feeds = new HashMap<>();

    private Map<String, FeedMetadata> feedMap = new HashMap<>();

    private Map<Long, FeedCategory> categories = new HashMap<>();

    public InMemoryMetadataService(){


    }

    @PostConstruct
    private void postConstruct(){
        loadSavedObjects();
    }

    private void loadSavedObjects(){

        Collection<RegisteredTemplate> templates = FileObjectPersistence.getInstance().getTemplatesFromFile();
        if(templates != null){
            for(RegisteredTemplate t : templates){
                String id = t.getId() == null ? t.getTemplateId() : t.getId();
                    registeredTemplates.put(id, t);
            }
        }

        Collection<FeedCategory>savedCategories = FileObjectPersistence.getInstance().getCategoriesFromFile();
        if(savedCategories != null){
            Long maxId = 0L;
            for(FeedCategory c: savedCategories){
                categories.put(c.getId(),c);
                if(c.getId() > maxId){
                    maxId = c.getId();
                }
            }
            categoryCounter.set(maxId+1L);
        }
        if(categories.isEmpty()){
            bootstrapCategories();
        }


        Collection<FeedMetadata>savedFeeds = FileObjectPersistence.getInstance().getFeedsFromFile();
        if(savedFeeds != null){
            Long maxId = 0L;
            for(FeedMetadata feed: savedFeeds){

                    //update the category mappings
                    Long categoryId = feed.getCategory().getId();
                    FeedCategory category = categories.get(categoryId);
                    feed.setCategory(category);
                    category.addRelatedFeed(feed);
                    //add it to the map
                    feeds.put(feed.getId(), feed);
                    if (feed.getId() > maxId) {
                        maxId = feed.getId();
                    }
                }

            feedCounter.set(maxId + 1L);
            loadSavedFeedsToMetaClientStore();
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

    /**
     * Needed to rewire feeds to ids in the server upon server start since the Feed Metadata store is in memory now
     */
    private void loadSavedFeedsToMetaClientStore(){
        for(FeedMetadata feedMetadata : feeds.values()) {
            feedMetadata.setFeedId(null);
            saveToMetadataStore(feedMetadata);
        }
    }

    public RegisteredTemplate checkAndSyncTemplateId(String templateIdOrName) throws JerseyClientException {
        TemplateDTO dto = null;
        RegisteredTemplate registeredTemplate = registeredTemplates.get(templateIdOrName);
        String id = "";
        if(registeredTemplate != null){
            String templateId = registeredTemplate.getTemplateId();
            //verify it exists in nifi
            try {
                dto = nifiRestClient.getTemplateById(templateId);
            }catch(JerseyClientException e){

            }
            if(dto == null){
                try {
                    dto = nifiRestClient.getTemplateByName(registeredTemplate.getTemplateName());
                    if(dto != null){
                        //template got changed ... resync
                        registeredTemplate.setTemplateId(dto.getId());
                        saveRegisteredTemplate(registeredTemplate);
                    }
                }
                catch (JerseyClientException e){

                }
            }
            if(dto != null){
                return registeredTemplate;

            }
        }
        if(dto == null){
            throw new JerseyClientException("Unable to find a matching template for Nifi Template Id: "+registeredTemplate.getTemplateId()+" or Name: "+registeredTemplate.getTemplateName());
        }
        return null;
    }



    public String templateIdForTemplateName(String templateName) {

        TemplateDTO templateDTO = null;
        try {
            templateDTO = nifiRestClient.getTemplateByName(templateName);
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }
        if(templateDTO != null){
            return templateDTO.getId();
        }
        return null;
    }

    public void registerTemplate(RegisteredTemplate registeredTemplate) {
        Date updateDate = new Date();

        if (registeredTemplate.getId() == null || !registeredTemplates.containsKey(registeredTemplate.getId())) {
            registeredTemplate.setCreateDate(updateDate);
        }
        registeredTemplate.setUpdateDate(updateDate);
        if(registeredTemplate.getId() == null){
            registeredTemplate.setId(UUID.randomUUID().toString());

        }
        saveRegisteredTemplate(registeredTemplate);

    }

    private void saveRegisteredTemplate(RegisteredTemplate registeredTemplate){
        //ensure that the incoming template name doesnt already exist.
        //if so remove and replace with this one
        RegisteredTemplate template = getRegisteredTemplateByName(registeredTemplate.getTemplateName());
        if(template != null && !template.getId().equalsIgnoreCase(registeredTemplate.getId())){
            //remove the old one with the same name
            registeredTemplates.remove(template.getId());
            //update those feeds that were pointing to this old one, to this new one
            List<FeedMetadata> feedsToUpdate = findFeedsWithTemplate(template.getId());
            if(feedsToUpdate != null && !feedsToUpdate.isEmpty()){
                for(FeedMetadata feedMetadata : feedsToUpdate){
                    feedMetadata.setTemplateId(registeredTemplate.getId());
                }
                //save the feeds
                FileObjectPersistence.getInstance().writeFeedsToFile(feeds.values());
            }
        }

        registeredTemplates.put(registeredTemplate.getId(), registeredTemplate);
        if(registeredTemplates.containsKey(registeredTemplate.getTemplateId())){
            registeredTemplates.remove(registeredTemplate.getTemplateId());
        }

        FileObjectPersistence.getInstance().writeTemplatesToFile(registeredTemplates.values());
    }

    @Override
    public List<NifiProperty> getTemplateProperties(String templateId) {
        List<NifiProperty> list = new ArrayList<>();
        RegisteredTemplate template = getRegisteredTemplate(templateId);
        if (template != null) {
            list = template.getProperties();
        }
        return list;
    }

    @Override
    public RegisteredTemplate getRegisteredTemplate(String templateId) {
        RegisteredTemplate savedTemplate = registeredTemplates.get(templateId);
        if(savedTemplate != null) {
            return new RegisteredTemplate(savedTemplate);
        }
        return null;
    }

    public RegisteredTemplate getRegisteredTemplateByName(final String templateName) {

       return Iterables.tryFind(registeredTemplates.values(), new Predicate<RegisteredTemplate>() {
           @Override
           public boolean apply(RegisteredTemplate registeredTemplate) {
               return registeredTemplate.getTemplateName().equalsIgnoreCase(templateName);
           }
       }).orNull();
    }

    public List<FeedMetadata> findFeedsWithTemplate(final String registeredTemplateId) {

        return Lists.newArrayList(Iterables.filter(feeds.values(), new Predicate<FeedMetadata>() {
            @Override
            public boolean apply(FeedMetadata feed) {
                return feed.getTemplateId().equalsIgnoreCase(registeredTemplateId);
            }
        }));
    }

    public RegisteredTemplate getRegisteredTemplateForNifiProperties(final String nifiTemplateId, final String nifiTemplateName) {
        RegisteredTemplate match = Iterables.tryFind(registeredTemplates.values(), new Predicate<RegisteredTemplate>() {
            @Override
            public boolean apply(RegisteredTemplate registeredTemplate) {
                boolean match = nifiTemplateId.equalsIgnoreCase(registeredTemplate.getTemplateId());
                if (!match && nifiTemplateName != null) {
                    match = nifiTemplateName.equalsIgnoreCase(registeredTemplate.getTemplateName());
                }
                return match;
            }
        }).orNull();
        return match;
    }


    private void syncTemplateId(RegisteredTemplate template){
      String nifiTemplateId = templateIdForTemplateName(template.getTemplateName());
        template.setTemplateId(nifiTemplateId);

        RegisteredTemplate t = getRegisteredTemplate(template.getId());
        template.setProperties(t.getProperties());
        saveRegisteredTemplate(template);
    }


    @Override
    /**
     * Get Registered TEmplate for incoming RegisteredTemplate.id or Nifi Template Id
     * if there is no RegisteredTEmplate matching the incoming id it is assumed to be a new Tempate and it tries to fetch it from Nifi
     */
    public RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException {
        RegisteredTemplate registeredTemplate = getRegisteredTemplate(templateId);
        //if it is null check to see if the template exists in nifi and is already registered
            if(registeredTemplate == null){
                registeredTemplate = getRegisteredTemplateForNifiProperties(templateId,null);
            }
         if (registeredTemplate == null) {
            List<NifiProperty> properties = nifiRestClient.getPropertiesForTemplate(templateId);
            registeredTemplate = new RegisteredTemplate();
            registeredTemplate.setProperties(properties);
            registeredTemplate.setTemplateId(templateId);
            //get the template Name
            TemplateDTO templateDTO = nifiRestClient.getTemplateById(templateId);
            if (templateDTO != null) {
                registeredTemplate.setTemplateName(templateDTO.getName());
            }
        } else {
             List<NifiProperty> properties = null;
            int matchCount = 0;
             try {
                 properties = nifiRestClient.getPropertiesForTemplate(registeredTemplate.getTemplateId());
                 List<NifiProperty> matchedProperties = NifiPropertyUtil
                     .matchAndSetPropertyByIdKey(properties, registeredTemplate.getProperties());
                 matchCount = matchedProperties.size();
             }catch(JerseyClientException e) {

             }
              if(properties == null || matchCount == 0) {
                  //sync templateId for name


                  properties = nifiRestClient.getPropertiesForTemplateByName(registeredTemplate.getTemplateName());
                  if(properties != null) {
                    //   property = NifiPropertyUtil.findPropertyByProcessorType(properties, "com.thinkbiganalytics.nifi.GetTableData", "Archive Unit");
                      NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties());
                  //    registeredTemplate.setProperties(properties);
                  }
                  syncTemplateId(registeredTemplate);

              }
             if(properties == null){
                 //ERROR
                 properties = new ArrayList<>();
             }
            //merge with the registered properties

            RegisteredTemplate copy = new RegisteredTemplate();
             copy.setId(registeredTemplate.getId());
            copy.setTemplateId(registeredTemplate.getTemplateId());
            copy.setTemplateName(registeredTemplate.getTemplateName());
            copy.setDefineTable(registeredTemplate.isDefineTable());
            copy.setAllowPreconditions(registeredTemplate.isAllowPreconditions());
            copy.setDataTransformation(registeredTemplate.isDataTransformation());
            copy.setIcon(registeredTemplate.getIcon());
            copy.setIconColor(registeredTemplate.getIconColor());
             copy.setDescription(registeredTemplate.getDescription());
            copy.setProperties(properties);
            registeredTemplate = copy;

        }
        return registeredTemplate;
    }

    @Override
    public List<String> getRegisteredTemplateIds() {
        return new ArrayList<>(registeredTemplates.keySet());
    }

    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        return new ArrayList<>(registeredTemplates.values());
    }


    @Override
    public void saveFeed(FeedMetadata feed) {
        if(feed.getId() == null || !feeds.containsKey(feed.getId())){
            feed.setId(feedCounter.getAndIncrement());
            feed.setVersion(new Long(1));
        }
        else {
            FeedMetadata previousFeed  = feeds.get(feed.getId());
            feed.setId(previousFeed.getId());
            feed.setVersion(previousFeed.getVersion()+1L);
        }

        //match up the related category
        Long categoryId = feed.getCategory().getId();
        if(categoryId == null){
            final String categoryName = feed.getCategory().getName();
            FeedCategory category = Iterables.tryFind(categories.values(), new Predicate<FeedCategory>() {
                @Override
                public boolean apply(FeedCategory feedCategory) {
                    return feedCategory.getName().equalsIgnoreCase(categoryName);
                }
            }).orNull();
            categoryId = category.getId();
        }
        if(categoryId != null) {
            categories.get(categoryId).addRelatedFeed(feed);
        }

        saveToMetadataStore(feed);
        feeds.put(feed.getId(), feed);
        FileObjectPersistence.getInstance().writeFeedsToFile(feeds.values());
    }

    @Override
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

    @Override
    public List<FeedSummary> getFeedSummaryData() {
        List<FeedSummary> summaryList = new ArrayList<>();
        if(feeds != null && !feeds.isEmpty()) {
            for(FeedMetadata feed: feeds.values()){
                summaryList.add(new FeedSummary(feed));
            }
        }
        return summaryList;
    }

    @Override
    public List<FeedSummary> getFeedSummaryForCategory(Long categoryId){
        List<FeedSummary> summaryList = new ArrayList<>();
        FeedCategory category = categories.get(categoryId);
        if(category != null){
            for(FeedMetadata feed: category.getFeeds()){
                summaryList.add(new FeedSummary(feed));
            }
        }
        return summaryList;
    }


    @Override
    public FeedMetadata getFeed(final String feedName) {

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
    public FeedMetadata getFeed(Long feedId) {

        if(feeds != null && !feeds.isEmpty()) {
            FeedMetadata feed = feeds.get(feedId);
            if(feed != null) {
                //get the latest category data
                FeedCategory category = categories.get(feed.getCategory().getId());
                feed.setCategory(category);

                //set the template to the feed

                RegisteredTemplate registeredTemplate = getRegisteredTemplate(feed.getTemplateId());
                if(registeredTemplate == null){
                    registeredTemplate = getRegisteredTemplateByName(feed.getTemplateName());
                }
                if(registeredTemplate != null) {
                    RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
                    copy.getProperties().clear();
                    feed.setRegisteredTemplate(copy);
                    feed.setTemplateId(copy.getTemplateId());
                }

                return feed;
            }
        }

        return null;
    }

   @Override
   public Collection<FeedCategory> getCategories(){
       return categories.values();
   }


    @Override
    public void saveCategory(final FeedCategory category) {
        if(category.getId() == null) {
            category.setId(categoryCounter.getAndIncrement());
            category.generateSystemName();
        }
        else {
            FeedCategory oldCategory = categories.get(category.getId());

            if(oldCategory != null && !oldCategory.getName().equalsIgnoreCase(category.getName())) {
                ///names have changed
                //only regenerate the system name if there are no related feeds
                if(oldCategory.getRelatedFeeds() == 0){
                    category.generateSystemName();
                }
            }
          List<FeedMetadata> feeds = categories.get(category.getId()).getFeeds();

            category.setFeeds(feeds);
            if(feeds != null) {
                category.setRelatedFeeds(feeds.size());
            }
        }
        categories.put(category.getId(), category);

        FileObjectPersistence.getInstance().writeCategoriesToFile(categories.values());
    }



    public boolean deleteCategory(Long categoryId) throws InvalidOperationException {
        FeedCategory category = categories.get(categoryId);
        if(category != null){
            //dont allow if category has feeds on it
            if(category.getRelatedFeeds() >0 ){
                throw new InvalidOperationException("Unable to delete Category "+category.getName()+".  This category has "+category.getRelatedFeeds()+" feeds associated to it.");
            }
            else {
                categories.remove(categoryId);
                FileObjectPersistence.getInstance().writeCategoriesToFile(categories.values());
                return true;
            }
        }
        return false;

    }
    private void bootstrapCategories(){
        
       List<FeedCategory> feedCategoryList = new ArrayList<>();
        feedCategoryList.add(new FeedCategoryBuilder("Employees").description("Employee profile data and records").icon("people").iconColor("#F06292").build());
        feedCategoryList.add(new FeedCategoryBuilder("Sales").description("Sales data including opportunities and leads").icon("phone_android").iconColor("#90A4AE").build());
        feedCategoryList.add(new FeedCategoryBuilder("Online").description("Web traffic data and reports of online activity").icon("web").iconColor("#66BB6A").build());
        feedCategoryList.add(new FeedCategoryBuilder("Payroll").description("Payroll records for employees").icon("attach_money").iconColor("#FFCA28").build());
        feedCategoryList.add(new FeedCategoryBuilder("Travel").description("Employee travel records including all expense reports").icon("local_airport").iconColor("#FFF176").build());
        feedCategoryList.add(new FeedCategoryBuilder("Data").description("General Data ").icon("cloud").iconColor("#AB47BC").build());
        feedCategoryList.add(new FeedCategoryBuilder("Emails").description("All email traffic data archived for the last 5 years").icon("email").iconColor("#FF5252").build());
        feedCategoryList.add(new FeedCategoryBuilder("Customers").description("All customer data for various companies").icon("face").iconColor("#FF5252").build());

        for(FeedCategory category : feedCategoryList){
            category.setId(categoryCounter.getAndIncrement());
            categories.put(category.getId(),category);
        }
        
    }
}

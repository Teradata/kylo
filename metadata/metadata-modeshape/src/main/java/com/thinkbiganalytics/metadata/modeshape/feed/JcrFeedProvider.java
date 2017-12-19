package com.thinkbiganalytics.metadata.modeshape.feed;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceNotFoundException;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.event.MetadataChange.ChangeType;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChange;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.event.feed.FeedPropertyChangeEvent;
import com.thinkbiganalytics.metadata.api.event.feed.PropertyChange;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.PreconditionBuilder;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.VersionProviderMixin;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
 * A JCR provider for {@link Feed} objects.
 */
public class JcrFeedProvider extends BaseJcrProvider<Feed, Feed.ID> implements FeedProvider, VersionProviderMixin<Feed, Feed.ID> {

    private static final String SORT_FEED_NAME = "feedName";
    private static final String SORT_STATE = "state";
    private static final String SORT_CATEGORY_NAME = "category.name";
    private static final String SORT_TEMPLATE_NAME = "templateName";
    private static final String SORT_UPDATE_DATE = "updateDate";

    private static final Map<String, String> JCR_PROP_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(SORT_FEED_NAME, "fs.[tba:systemName]");
        map.put(SORT_STATE, "fdata.[tba:state]");
        map.put(SORT_CATEGORY_NAME, "c.[tba:systemName]");
//        map.put(SORT_TEMPLATE_NAME, "t.[jcr:title]");         // Not supported
        map.put(SORT_TEMPLATE_NAME, "e.[jcr:lastModified]");    // Ignore template name sorting for now and use updateDate
        map.put(SORT_UPDATE_DATE, "e.[jcr:lastModified]");
        JCR_PROP_MAP = Collections.unmodifiableMap(map);
    }

    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private ServiceLevelAgreementProvider slaProvider;

    @Inject
    private DatasourceProvider datasourceProvider;

    /**
     * JCR node type manager
     */
    @Inject
    private ExtensibleTypeProvider extensibleTypeProvider;

    /**
     * Transaction support
     */
    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private SecurityRoleProvider roleProvider;

    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;

    @Inject
    private AccessController accessController;

    @Inject
    private FeedOpsAccessControlProvider opsAccessProvider;

    @Inject
    private MetadataEventService metadataEventService;

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        return JcrFeed.NODE_TYPE;
    }

    @Override
    public Class<JcrFeed> getEntityClass() {
        return JcrFeed.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrFeed.class;
    }
    
    @Override
    public Optional<Node> findVersionableNode(ID id) {
        final JcrFeed feed = (JcrFeed) findById(id);
        if (feed != null) {
            return feed.getFeedSummary().map(s -> s.getNode());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Feed asEntity(ID id, Node versionable) {
        try {
            // The versionable node argument is the summary node.
            Node feedNode = versionable.getSession().getNodeByIdentifier(id.toString());
            return JcrUtil.getJcrObject(feedNode, JcrFeed.class, versionable, null);
        } catch (RepositoryException e) {
            throw new FeedNotFoundExcepton(id);
        }
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#create(java.lang.Object)
         */
    @Override
    public Feed create(Feed t) {
        JcrFeed feed = (JcrFeed) super.create(t);
        feed.setOpsAccessProvider(this.opsAccessProvider);
        return feed;
    }

    @Override
    public List<Feed> findAll() {
        List<Feed> feeds = super.findAll();
        return feeds.stream()
            .map(JcrFeed.class::cast)
            .map(feed -> {
                feed.setOpsAccessProvider(opsAccessProvider);
                return feed;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Feed findById(ID id) {
        final JcrFeed feed = (JcrFeed) super.findById(id);
        if (feed != null) {
            feed.setOpsAccessProvider(this.opsAccessProvider);
        }
        return feed;
    }

    @Override
    public Feed update(Feed feed) {

        //   feed.getCategory().getAllowedActions().checkPermission(CategoryAccessControl.CREATE_FEED);
        return super.update(feed);
    }

    public void removeFeedSources(Feed.ID feedId) {
        JcrFeed feed = (JcrFeed) findById(feedId);
        feed.removeFeedSources();
    }

    public void removeFeedSource(Feed.ID feedId, Datasource.ID dsId) {
        JcrFeed feed = (JcrFeed) findById(feedId);
        JcrFeedSource source = (JcrFeedSource) feed.getSource(dsId);

        if (source != null) {
            feed.removeFeedSource(source);
        }
    }

    public void removeFeedDestination(Feed.ID feedId, Datasource.ID dsId) {
        JcrFeed feed = (JcrFeed) findById(feedId);
        JcrFeedDestination dest = (JcrFeedDestination) feed.getDestination(dsId);

        if (dest != null) {
            feed.removeFeedDestination(dest);
        }
    }

    public void removeFeedDestinations(Feed.ID feedId) {
        JcrFeed feed = (JcrFeed) findById(feedId);
        feed.removeFeedDestinations();
    }

    @Override
    public FeedSource ensureFeedSource(Feed.ID feedId, Datasource.ID dsId) {
        JcrFeed feed = (JcrFeed) findById(feedId);
        FeedSource source = feed.getSource(dsId);

        if (source == null) {
            JcrDatasource datasource = (JcrDatasource) datasourceProvider.getDatasource(dsId);

            if (datasource != null) {
                JcrFeedSource jcrSrc = feed.ensureFeedSource(datasource);

                save();
                return jcrSrc;
            } else {
                throw new DatasourceNotFoundException(dsId);
            }
        } else {
            return source;
        }
    }

    @Override
    public FeedSource ensureFeedSource(Feed.ID feedId, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id, com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID slaId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeedDestination ensureFeedDestination(Feed.ID feedId, com.thinkbiganalytics.metadata.api.datasource.Datasource.ID dsId) {
        JcrFeed feed = (JcrFeed) findById(feedId);
        FeedDestination source = feed.getDestination(dsId);

        if (source == null) {
            JcrDatasource datasource = (JcrDatasource) datasourceProvider.getDatasource(dsId);

            if (datasource != null) {
                JcrFeedDestination jcrDest = feed.ensureFeedDestination(datasource);

                save();
                return jcrDest;
            } else {
                throw new DatasourceNotFoundException(dsId);
            }
        } else {
            return source;
        }
    }

    @Override
    public Feed ensureFeed(Category.ID categoryId, String feedSystemName) {
        Category category = categoryProvider.findById(categoryId);
        return ensureFeed(category.getSystemName(), feedSystemName);
    }

    /**
     * Ensure the Feed, but the Category must exist!
     */
    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName) {
        JcrCategory category = null;
        try {
            String categoryPath = EntityUtil.pathForCategory(categorySystemName);
            Node categoryNode = getSession().getNode(categoryPath);
            if (categoryNode != null) {
                category = JcrUtil.createJcrObject(categoryNode, JcrCategory.class);
            } else {
                category = (JcrCategory) categoryProvider.findBySystemName(categorySystemName);
            }
        } catch (RepositoryException e) {
            throw new CategoryNotFoundException("Unable to find Category for " + categorySystemName, null);
        }

        String feedParentPath = category.getFeedParentPath();
        boolean newFeed = !hasEntityNode(feedParentPath, feedSystemName);
        Node feedNode = findOrCreateEntityNode(feedParentPath, feedSystemName, getJcrEntityClass());
        boolean versionable = JcrVersionUtil.isVersionable(feedNode);

        JcrFeed feed = new JcrFeed(feedNode, category, this.opsAccessProvider);

        feed.setSystemName(feedSystemName);

        if (newFeed) {
            if (this.accessController.isEntityAccessControlled()) {
                List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.FEED);
                this.actionsProvider.getAvailableActions(AllowedActions.FEED)
                    .ifPresent(actions -> feed.enableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), roles));
            } else {
                this.actionsProvider.getAvailableActions(AllowedActions.FEED)
                    .ifPresent(actions -> feed.disableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser()));
            }

            addPostFeedChangeAction(feed, ChangeType.CREATE);
        }

        return feed;
    }

    /**
     * Registers an action that produces a feed change event upon a successful transaction commit.
     *
     * @param feed the feed to being created
     */
    private void addPostFeedChangeAction(Feed feed, ChangeType changeType) {
        Feed.State state = feed.getState();
        Feed.ID id = feed.getId();
        String feedName = feed.getQualifiedName();
        final Principal principal = SecurityContextHolder.getContext().getAuthentication();

        Consumer<Boolean> action = (success) -> {
            if (success) {
                FeedChange change = new FeedChange(changeType, feedName, feedName,id, state);
                FeedChangeEvent event = new FeedChangeEvent(change, DateTime.now(), principal);
                metadataEventService.notify(event);
            }
        };

        JcrMetadataAccess.addPostTransactionAction(action);
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName, String descr) {
        Feed feed = ensureFeed(categorySystemName, feedSystemName);
        feed.setDescription(descr);
        return feed;
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName, String descr, Datasource.ID destId) {
        Feed feed = ensureFeed(categorySystemName, feedSystemName, descr);
        //TODO add/find datasources
        return feed;
    }

    @Override
    public Feed ensureFeed(String categorySystemName, String feedSystemName, String descr, Datasource.ID srcId, Datasource.ID destId) {
        Feed feed = ensureFeed(categorySystemName, feedSystemName, descr);
        if (srcId != null) {
            ensureFeedSource(feed.getId(), srcId);
        }
        return feed;
    }

    @Override
    public Feed createPrecondition(Feed.ID feedId, String descr, List<Metric> metrics) {
        JcrFeed feed = (JcrFeed) findById(feedId);

        ServiceLevelAgreementBuilder slaBldr = buildPrecondition(feed)
            .name("Precondition for feed " + feedId)
            .description(descr);

        slaBldr.obligationGroupBuilder(Condition.REQUIRED)
            .obligationBuilder()
            .metric(metrics)
            .build()
            .build();

        return feed;
    }

    @Override
    public PreconditionBuilder buildPrecondition(ID feedId) {
        JcrFeed feed = (JcrFeed) findById(feedId);

        return buildPrecondition(feed);
    }

    private PreconditionBuilder buildPrecondition(JcrFeed feed) {
        try {
            if (feed != null) {
                Node slaNode = feed.createNewPrecondition();
                ServiceLevelAgreementBuilder slaBldr = ((JcrServiceLevelAgreementProvider) this.slaProvider).builder(slaNode);

                return new JcrPreconditionbuilder(slaBldr, feed);
            } else {
                throw new FeedNotFoundExcepton(feed.getId());
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create the precondition for feed " + feed.getId(), e);
        }
    }

    @Override
    public Feed addDependent(ID targetId, ID dependentId) {
        JcrFeed target = (JcrFeed) getFeed(targetId);

        if (target == null) {
            throw new FeedNotFoundExcepton("The target feed to be assigned the dependent does not exists", targetId);
        }

        JcrFeed dependent = (JcrFeed) getFeed(dependentId);

        if (dependent == null) {
            throw new FeedNotFoundExcepton("The dependent feed does not exists", dependentId);
        }

        target.addDependentFeed(dependent);
        return target;
    }

    @Override
    public Feed removeDependent(ID feedId, ID dependentId) {
        JcrFeed target = (JcrFeed) getFeed(feedId);

        if (target == null) {
            throw new FeedNotFoundExcepton("The target feed to be assigned the dependent does not exists", feedId);
        }

        JcrFeed dependent = (JcrFeed) getFeed(dependentId);

        if (dependent == null) {
            throw new FeedNotFoundExcepton("The dependent feed does not exists", dependentId);
        }

        target.removeDependentFeed(dependent);
        return target;
    }

    @Override
    public FeedCriteria feedCriteria() {
        return new Criteria();
    }

    @Override
    public Feed getFeed(Feed.ID id) {
        return findById(id);
    }

    @Override
    public List<Feed> getFeeds() {
        return findAll();
    }

    @Override
    public List<Feed> getFeeds(FeedCriteria criteria) {

        if (criteria != null) {
            Criteria criteriaImpl = (Criteria) criteria;
            return criteriaImpl.select(getSession(), JcrFeed.NODE_TYPE, Feed.class, JcrFeed.class);
        }
        return null;
    }

    @Override
    public Feed findBySystemName(String systemName) {
        String categorySystemName = FeedNameUtil.category(systemName);
        String feedSystemName = FeedNameUtil.feed(systemName);
        return findBySystemName(categorySystemName, feedSystemName);
    }

    @Override
    public Feed findBySystemName(String categorySystemName, String systemName) {
        FeedCriteria c = feedCriteria();
        if (categorySystemName != null) {
            c.category(categorySystemName);
        }
        c.name(systemName);
        List<Feed> feeds = getFeeds(c);
        if (feeds != null && !feeds.isEmpty()) {
            return feeds.get(0);
        }
        return null;
    }


    @Override
    public List<? extends Feed> findByTemplateId(FeedManagerTemplate.ID templateId) {
        String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeed.NODE_TYPE) + " as e WHERE e." + EntityUtil.asQueryProperty(FeedDetails.TEMPLATE) + " = $id";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("id", templateId.toString());
        return JcrQueryUtil.find(getSession(), query, JcrFeed.class);
    }

    @Override
    public List<? extends Feed> findByCategoryId(Category.ID categoryId) {

        String query = "SELECT e.* from " + EntityUtil.asQueryProperty(JcrFeed.NODE_TYPE) + " as e "
                       + "INNER JOIN ['tba:feedSummary'] as summary on ISCHILDNODE(summary,e)"
                       + "WHERE summary." + EntityUtil.asQueryProperty(FeedSummary.CATEGORY) + " = $id";

        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("id", categoryId.toString());

        try {
            QueryResult result = JcrQueryUtil.query(getSession(), query, bindParams);
            return JcrQueryUtil.queryResultToList(result, JcrFeed.class);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to getFeeds for Category ", e);
        }

    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.FeedProvider#findPreconditionedFeeds()
     */
    @Override
    public List<? extends Feed> findPreconditionedFeeds() {
        StringBuilder query = new StringBuilder("SELECT e.* from [tba:feed] as e ")
                        .append("JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) ")
                        .append("JOIN [tba:feedDetails] AS fdetail ON ISCHILDNODE(fdetail, fs) ")
                        .append("JOIN [tba:feedPrecondition] AS precond ON ISCHILDNODE(precond, fdetail) ");
        try {
            QueryResult result = JcrQueryUtil.query(getSession(), query.toString());
            return JcrQueryUtil.queryResultToList(result, JcrFeed.class);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to getFeeds for Category ", e);
        }
    }

//
//    @Override
//    public FeedSource getFeedSource(com.thinkbiganalytics.metadata.api.feed.FeedSource.ID id) {
//        try {
//            Node node = getSession().getNodeByIdentifier(id.toString());
//
//            if (node != null) {
//                return JcrUtil.createJcrObject(node, JcrFeedSource.class);
//            }
//        } catch (RepositoryException e) {
//            throw new MetadataRepositoryException("Unable to get Feed Destination for " + id);
//        }
//        return null;
//    }
//
//    @Override
//    public FeedDestination getFeedDestination(com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID id) {
//        try {
//            Node node = getSession().getNodeByIdentifier(id.toString());
//
//            if (node != null) {
//                return JcrUtil.createJcrObject(node, JcrFeedDestination.class);
//            }
//        } catch (RepositoryException e) {
//            throw new MetadataRepositoryException("Unable to get Feed Destination for " + id);
//        }
//        return null;
//
//    }

    @Override
    public Feed.ID resolveFeed(Serializable fid) {
        return resolveId(fid);
    }
//
//    @Override
//    public com.thinkbiganalytics.metadata.api.feed.FeedSource.ID resolveSource(Serializable sid) {
//        return new JcrFeedSource.FeedSourceId((sid));
//    }
//
//    @Override
//    public com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID resolveDestination(Serializable sid) {
//        return new JcrFeedDestination.FeedDestinationId(sid);
//    }

    @Override
    public boolean enableFeed(Feed.ID id) {
        Feed feed = getFeed(id);
        if (accessController.isEntityAccessControlled()) {
            feed.getAllowedActions().checkPermission(FeedAccessControl.ENABLE_DISABLE);
        }

        if (!feed.getState().equals(Feed.State.ENABLED)) {
            feed.setState(Feed.State.ENABLED);
            //Enable any SLAs on this feed
            List<ServiceLevelAgreement> serviceLevelAgreements = feed.getServiceLevelAgreements();
            if (serviceLevelAgreements != null) {
                for (ServiceLevelAgreement sla : serviceLevelAgreements) {
                    JcrServiceLevelAgreement jcrSla = (JcrServiceLevelAgreement) sla;
                    jcrSla.enable();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean disableFeed(Feed.ID id) {
        Feed feed = getFeed(id);
        if (accessController.isEntityAccessControlled()) {
            feed.getAllowedActions().checkPermission(FeedAccessControl.ENABLE_DISABLE);
        }

        if (!feed.getState().equals(Feed.State.DISABLED)) {
            feed.setState(Feed.State.DISABLED);
            //disable any SLAs on this feed
            List<ServiceLevelAgreement> serviceLevelAgreements = feed.getServiceLevelAgreements();
            if (serviceLevelAgreements != null) {
                for (ServiceLevelAgreement sla : serviceLevelAgreements) {
                    JcrServiceLevelAgreement jcrSla = (JcrServiceLevelAgreement) sla;
                    jcrSla.disabled();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void deleteFeed(ID feedId) {
        JcrFeed feed = (JcrFeed) getFeed(feedId);
        if (feed != null) {
            delete(feed);
        }
    }

    @Override
    public void delete(Feed feed) {
        if (accessController.isEntityAccessControlled()) {
            feed.getAllowedActions().checkPermission(FeedAccessControl.DELETE);
        }

        addPostFeedChangeAction(feed, ChangeType.DELETE);

        // Remove dependent feeds
        final Node node = ((JcrFeed) feed).getNode();
        feed.getDependentFeeds().forEach(dep -> feed.removeDependentFeed((JcrFeed) dep));
        JcrMetadataAccess.getCheckedoutNodes().removeIf(node::equals);

        // Remove destinations and sources
        ((JcrFeed) feed).removeFeedDestinations();
        ((JcrFeed) feed).removeFeedSources();

        // Delete feed
        FeedManagerTemplate template = feed.getTemplate();
        if (template != null) {
            template.removeFeed(feed);
        }

        // Remove all Ops access control entries
        this.opsAccessProvider.revokeAllAccess(feed.getId());

        super.delete(feed);
    }

    public Feed.ID resolveId(Serializable fid) {
        return new JcrFeed.FeedId(fid);
    }

    protected JcrFeed setupPrecondition(JcrFeed feed, ServiceLevelAgreement sla) {
//        this.preconditionService.watchFeed(feed);
        feed.setPrecondition((JcrServiceLevelAgreement) sla);
        return feed;
    }

    public Feed updateFeedServiceLevelAgreement(Feed.ID feedId, ServiceLevelAgreement sla) {
        JcrFeed feed = (JcrFeed) getFeed(feedId);
        feed.addServiceLevelAgreement(sla);
        return feed;
    }

    @Override
    public Map<String, Object> mergeFeedProperties(ID feedId, Map<String, Object> properties) {
        JcrFeed feed = (JcrFeed) getFeed(feedId);
        List<String> securityGroupNames = new ArrayList<>();
        for (Object o : feed.getSecurityGroups()) {
            HadoopSecurityGroup securityGroup = (HadoopSecurityGroup) o;
            securityGroupNames.add(securityGroup.getName());
        }

        Map<String, Object> merged = feed.mergeProperties(properties);

        PropertyChange change = new PropertyChange(feed.getId().getIdValue(),
                                                   feed.getCategory().getSystemName(),
                                                   feed.getSystemName(),
                                                   securityGroupNames,
                                                   feed.getProperties(),
                                                   properties);
        this.metadataEventService.notify(new FeedPropertyChangeEvent(change));

        return merged;
    }

    public Map<String, Object> replaceProperties(ID feedId, Map<String, Object> properties) {
        JcrFeed feed = (JcrFeed) getFeed(feedId);
        return feed.replaceProperties(properties);
    }

    @Nonnull
    @Override
    public Set<UserFieldDescriptor> getUserFields() {
        return JcrPropertyUtil.getUserFields(ExtensionsConstants.USER_FEED, extensibleTypeProvider);
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserFieldDescriptor> userFields) {
        // TODO service?
        metadataAccess.commit(() -> {
            JcrPropertyUtil.setUserFields(ExtensionsConstants.USER_FEED, userFields, extensibleTypeProvider);
            return userFields;
        }, MetadataAccess.SERVICE);
    }

    public void populateInverseFeedDependencies() {
        Map<Feed.ID, Feed> map = new HashMap<Feed.ID, Feed>();
        List<Feed> feeds = getFeeds();
        if (feeds != null) {
            feeds.stream().forEach(feed -> map.put(feed.getId(), feed));
        }
        feeds.stream().filter(feed -> feed.getDependentFeeds() != null && !feed.getDependentFeeds().isEmpty()).forEach(feed1 -> {
            List<Feed> dependentFeeds = feed1.getDependentFeeds();
            dependentFeeds.stream().filter(depFeed -> depFeed.getUsedByFeeds() == null || !depFeed.getUsedByFeeds().contains(feed1))
                .forEach(depFeed -> depFeed.addUsedByFeed(feed1));
        });
    }

    @Override
    protected void appendJoins(StringBuilder bldr, String filter) {
        if (!Strings.isNullOrEmpty(filter)) {
            bldr.append("JOIN [tba:categoryDetails] AS cd ON ISCHILDNODE(e, cd) ");
            bldr.append("JOIN [tba:category] AS c ON ISCHILDNODE(cd, c) ");
            bldr.append("JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) ");
            bldr.append("JOIN [tba:feedDetails] AS fdetail ON ISCHILDNODE(fdetail, fs) ");
            bldr.append("JOIN [tba:feedData] AS fdata ON ISCHILDNODE(fdata, e) ");
        }
    }

    @Override
    protected void appendJoins(StringBuilder bldr, Pageable pageable, String filter) {
        List<String> sortProps = new ArrayList<>();
        if (pageable.getSort() != null) {
            pageable.getSort().forEach(o -> sortProps.add(o.getProperty()));
        }

        // TODO: template sorting does not currently work because a way hasn't been found yet to join
        // across reference properties, so the template associated with a feed cannot be joined.
        
        // If there is no filter then just perform the minimal joins needed to sort.
        if (!Strings.isNullOrEmpty(filter)) {
            appendJoins(bldr, filter);
        } else if (sortProps.contains(SORT_FEED_NAME)) {
            bldr.append("JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) ");
        } else if (sortProps.contains(SORT_CATEGORY_NAME)) {
            bldr.append("JOIN [tba:categoryDetails] AS cd ON ISCHILDNODE(e, cd) ");
            bldr.append("JOIN [tba:category] AS c ON ISCHILDNODE(cd, c) ");
        } else if (sortProps.contains(SORT_STATE)) {
            bldr.append("JOIN [tba:feedData] AS fdata ON ISCHILDNODE(fdata, e) ");
        }
    }

    @Override
    protected void appendFilter(StringBuilder bldr, String filter) {
        String filterPattern = Strings.isNullOrEmpty(filter) ? null : "'%" + filter.toLowerCase() + "%'";
        if (filterPattern != null) {
            bldr.append("WHERE LOWER(").append(JCR_PROP_MAP.get(SORT_FEED_NAME)).append(") LIKE ").append(filterPattern);
            bldr.append(" OR LOWER(").append(JCR_PROP_MAP.get(SORT_CATEGORY_NAME)).append(") LIKE ").append(filterPattern);
            bldr.append(" OR LOWER(").append(JCR_PROP_MAP.get(SORT_STATE)).append(") LIKE ").append(filterPattern);
            // Must sub-select matching templates for reference comparison because a way to join across reference properties has not been found.
            bldr.append(" OR CAST(fdetail.[tba:feedTemplate] AS REFERENCE) IN ")
                .append("(SELECT [mode:id] from [tba:feedTemplate] AS t WHERE LOWER(t.[jcr:title]) LIKE ").append(filterPattern).append(") ");
        }
    }

    @Override
    protected String getEntityQueryStartingPath() {
        return EntityUtil.pathForCategory();
    }

    @Override
    protected String getFindAllFilter() {
       return getFindAllFilter(getEntityQueryStartingPath(),5);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider#deriveJcrPropertyName(java.lang.String)
     */
    @Override
    protected String deriveJcrPropertyName(String property) {
        String jcrProp = JCR_PROP_MAP.get(property);

        if (jcrProp == null) {
            throw new IllegalArgumentException("Unknown sort property: " + property);
        } else if (jcrProp.length() == 0) {
            return JCR_PROP_MAP.get("feedName");
        } else {
            return jcrProp;
        }
    }

    private static class Criteria extends AbstractMetadataCriteria<FeedCriteria> implements FeedCriteria, Predicate<Feed> {

        private String name;
        private Set<Datasource.ID> sourceIds = new HashSet<>();
        private Set<Datasource.ID> destIds = new HashSet<>();
        private String category;

        /**
         * Selects by navigation rather than SQL
         */
        @Override
        public <E, J extends JcrObject> List<E> select(Session session, String typeName, Class<E> type, Class<J> jcrClass) {
            try {
                // Datasources are not currently used so only name comparison is necessary
                Node feedsNode = session.getRootNode().getNode("metadata/feeds");
                NodeIterator catItr = null;

                if (this.category != null) {
                    catItr = feedsNode.getNodes(this.category);
                } else {
                    catItr = feedsNode.getNodes();
                }

                List<Feed> list = new ArrayList<Feed>();

                while (catItr.hasNext()) {
                    Node catNode = (Node) catItr.next();
                    NodeIterator feedItr = null;

                    if (catNode.hasNode(JcrCategory.DETAILS)) {
                        catNode = catNode.getNode(JcrCategory.DETAILS);
                    }

                    if (this.name != null) {
                        feedItr = catNode.getNodes(this.name);
                    } else {
                        feedItr = catNode.getNodes();
                    }

                    while (feedItr.hasNext()) {
                        Node feedNode = (Node) feedItr.next();

                        if (feedNode.getPrimaryNodeType().getName().equals("tba:feed")) {
                            list.add(JcrUtil.createJcrObject(feedNode, JcrFeed.class));
                        }
                    }
                }

                return (List<E>) list;
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to select feeds", e);
            }
        }

        @Override
        @Deprecated
        protected void applyFilter(StringBuilder queryStr, HashMap<String, Object> params) {
            StringBuilder cond = new StringBuilder();
            StringBuilder join = new StringBuilder();

            if (this.name != null) {
                cond.append(EntityUtil.asQueryProperty(JcrFeed.SYSTEM_NAME) + " = $name");
                params.put("name", this.name);
            }
            if (this.category != null) {
                //TODO FIX SQL
                join.append(
                    " join [" + JcrCategory.NODE_TYPE + "] as c on e." + EntityUtil.asQueryProperty(FeedSummary.CATEGORY) + "." + EntityUtil.asQueryProperty(JcrCategory.SYSTEM_NAME) + " = c."
                    + EntityUtil
                        .asQueryProperty(JcrCategory.SYSTEM_NAME));
                cond.append(" c." + EntityUtil.asQueryProperty(JcrCategory.SYSTEM_NAME) + " = $category ");
                params.put("category", this.category);
            }

            applyIdFilter(cond, join, this.sourceIds, "sources", params);
            applyIdFilter(cond, join, this.destIds, "destinations", params);

            if (join.length() > 0) {
                queryStr.append(join.toString());
            }

            if (cond.length() > 0) {
                queryStr.append(" where ").append(cond.toString());
            }
        }

        @Deprecated
        private void applyIdFilter(StringBuilder cond, StringBuilder join, Set<Datasource.ID> idSet, String relation, HashMap<String, Object> params) {
            if (!idSet.isEmpty()) {
                if (cond.length() > 0) {
                    cond.append("and ");
                }

                String alias = relation.substring(0, 1);
                join.append("join e.").append(relation).append(" ").append(alias).append(" ");
                cond.append(alias).append(".datasource.id in $").append(relation).append(" ");
                params.put(relation, idSet);
            }
        }

        @Override
        public boolean apply(Feed input) {
            if (this.name != null && !name.equals(input.getName())) {
                return false;
            }
            if (this.category != null && input.getCategory() != null && !this.category.equals(input.getCategory().getSystemName())) {
                return false;
            }
            if (!this.destIds.isEmpty()) {
                List<? extends FeedDestination> destinations = input.getDestinations();
                for (FeedDestination dest : destinations) {
                    if (this.destIds.contains(dest.getDatasource().getId())) {
                        return true;
                    }
                }
                return false;
            }

            if (!this.sourceIds.isEmpty()) {
                List<? extends FeedSource> sources = input.getSources();
                for (FeedSource src : sources) {
                    if (this.sourceIds.contains(src.getDatasource().getId())) {
                        return true;
                    }
                }
                return false;
            }

            return true;
        }

        @Override
        public FeedCriteria sourceDatasource(Datasource.ID id, Datasource.ID... others) {
            this.sourceIds.add(id);
            for (Datasource.ID other : others) {
                this.sourceIds.add(other);
            }
            return this;
        }

        @Override
        public FeedCriteria destinationDatasource(Datasource.ID id, Datasource.ID... others) {
            this.destIds.add(id);
            for (Datasource.ID other : others) {
                this.destIds.add(other);
            }
            return this;
        }

        @Override
        public FeedCriteria name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public FeedCriteria category(String category) {
            this.category = category;
            return this;
        }
    }

    private class JcrPreconditionbuilder implements PreconditionBuilder {

        private final ServiceLevelAgreementBuilder slaBuilder;
        private final JcrFeed feed;

        public JcrPreconditionbuilder(ServiceLevelAgreementBuilder slaBuilder, JcrFeed feed) {
            super();
            this.slaBuilder = slaBuilder;
            this.feed = feed;
        }

        public ServiceLevelAgreementBuilder name(String name) {
            return slaBuilder.name(name);
        }

        public ServiceLevelAgreementBuilder description(String description) {
            return slaBuilder.description(description);
        }

        public ServiceLevelAgreementBuilder obligation(Obligation obligation) {
            return slaBuilder.obligation(obligation);
        }

        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder() {
            return slaBuilder.obligationBuilder();
        }

        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder(Condition condition) {
            return slaBuilder.obligationBuilder(condition);
        }

        public ObligationGroupBuilder obligationGroupBuilder(Condition condition) {
            return slaBuilder.obligationGroupBuilder(condition);
        }

        public ServiceLevelAgreement build() {
            ServiceLevelAgreement sla = slaBuilder.build();

            setupPrecondition(feed, sla);
            return sla;
        }

        @Override
        public ServiceLevelAgreementBuilder actionConfigurations(List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations) {
            return null;
        }
    }
}

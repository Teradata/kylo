package com.thinkbiganalytics.nifi.rest.client;

import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitorCache;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by sr186054 on 12/27/16.
 */
public class DefaultNiFiFlowVisitorClient implements NiFiFlowVisitorClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultNiFiFlowVisitorClient.class);


    private NiFiRestClient restClient;

    public DefaultNiFiFlowVisitorClient(NiFiRestClient restClient) {
        this.restClient = restClient;
    }


    public NifiVisitableProcessGroup getFlowOrder(String processGroupId, NifiConnectionOrderVisitorCache cache) throws NifiComponentNotFoundException {
        ProcessGroupDTO processGroupEntity = restClient.processGroups().findById(processGroupId, true, true).orElse(null);
        return getFlowOrder(processGroupEntity, cache);
    }

    public NifiVisitableProcessGroup getFlowOrder(ProcessGroupDTO processGroupEntity, NifiConnectionOrderVisitorCache cache) throws NifiComponentNotFoundException {
        if (cache == null) {
            cache = new NifiConnectionOrderVisitorCache();
        }

        final NifiConnectionOrderVisitorCache finalCache = cache;
        Optional<ProcessGroupDTO> cachedProcessGroup = cache.getProcessGroup(processGroupEntity.getId());
        if (!cachedProcessGroup.isPresent() && processGroupEntity.getContents() != null) {
            NifiProcessUtil.getProcessGroups(processGroupEntity).stream().forEach(processGroupDTO -> finalCache.add(processGroupDTO));
        }

        NifiVisitableProcessGroup group = null;
        if (processGroupEntity != null) {
            group = new NifiVisitableProcessGroup(processGroupEntity);
            NifiConnectionOrderVisitor orderVisitor = new NifiConnectionOrderVisitor(restClient, group, finalCache);
            try {
                Optional<ProcessGroupDTO> parent = cache.getProcessGroup(processGroupEntity.getParentGroupId());
                if (!parent.isPresent()) {
                    parent = restClient.processGroups().findById(processGroupEntity.getParentGroupId(), false, false);
                }
                if (parent != null) {
                    group.setParentProcessGroup(parent.get());
                }
            } catch (NifiComponentNotFoundException e) {
                //cant find the parent
            }
            group.accept(orderVisitor);
            finalCache.add(orderVisitor.toCachedItem());
        }
        return group;
    }


    public NifiFlowProcessGroup getFeedFlow(String processGroupId) {
        return getFeedFlow(processGroupId, null);
    }

    public NifiFlowProcessGroup getFeedFlow(String processGroupId, NifiConnectionOrderVisitorCache cache) {
        NifiVisitableProcessGroup visitableGroup = getFlowOrder(processGroupId, cache);
        NifiFlowProcessGroup flow = new NifiFlowBuilder().build(visitableGroup);
        String categoryName = flow.getParentGroupName();
        String feedName = flow.getName();
        feedName = FeedNameUtil.fullName(categoryName, feedName);
        //if it is a versioned feed then strip the version to get the correct feed name
        feedName = TemplateCreationHelper.parseVersionedProcessGroupName(feedName);
        flow.setFeedName(feedName);
        return flow;
    }

    public Set<ProcessorDTO> getProcessorsForFlow(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup group = getFlowOrder(processGroupId, null);
        Set<ProcessorDTO> processors = new HashSet<>();
        for (NifiVisitableProcessor p : group.getStartingProcessors()) {
            processors.addAll(p.getProcessors());
        }
        return processors;
    }


    public NifiFlowProcessGroup getFeedFlowForCategoryAndFeed(String categoryAndFeedName) {
        NifiFlowProcessGroup flow = null;
        String category = FeedNameUtil.category(categoryAndFeedName);
        String feed = FeedNameUtil.feed(categoryAndFeedName);
        //1 find the ProcessGroup under "root" matching the name category
        ProcessGroupDTO processGroupEntity = restClient.processGroups().findRoot();
        ProcessGroupDTO root = processGroupEntity;
        ProcessGroupDTO categoryGroup = root.getContents().getProcessGroups().stream().filter(group -> category.equalsIgnoreCase(group.getName())).findAny().orElse(null);
        if (categoryGroup != null) {
            ProcessGroupDTO feedGroup = categoryGroup.getContents().getProcessGroups().stream().filter(group -> feed.equalsIgnoreCase(group.getName())).findAny().orElse(null);
            if (feedGroup != null) {
                flow = getFeedFlow(feedGroup.getId());
            }
        }
        return flow;
    }


    //walk entire graph
    public List<NifiFlowProcessGroup> getFeedFlows() {
        log.info("get Graph of Nifi Flows");
        long start = System.currentTimeMillis();
        NifiConnectionOrderVisitorCache cache = new NifiConnectionOrderVisitorCache();
        List<NifiFlowProcessGroup> feedFlows = new ArrayList<>();
        ProcessGroupDTO processGroupEntity = restClient.processGroups().findRoot();
        ProcessGroupDTO root = processGroupEntity;
        //first level is the category
        root.getContents().getProcessGroups().stream().sorted(new Comparator<ProcessGroupDTO>() {
            @Override
            public int compare(ProcessGroupDTO o1, ProcessGroupDTO o2) {
                if (TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME.equalsIgnoreCase(o1.getName())) {
                    return -1;
                }
                if (TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME.equalsIgnoreCase(o2.getName())) {
                    return -1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        }).forEach(category -> {
            for (ProcessGroupDTO feedProcessGroup : category.getContents().getProcessGroups()) {
                //second level is the feed
                String feedName = FeedNameUtil.fullName(category.getName(), feedProcessGroup.getName());
                //if it is a versioned feed then strip the version to get the correct feed name
                feedName = TemplateCreationHelper.parseVersionedProcessGroupName(feedName);
                NifiFlowProcessGroup feedFlow = getFeedFlow(feedProcessGroup.getId(), cache);
                feedFlow.setFeedName(feedName);
                feedFlows.add(feedFlow);
            }
        });
        long end = System.currentTimeMillis();
        log.info("finished Graph of Nifi Flows.  Returning {} flows, {} ", feedFlows.size(), (end - start) + " ms");
        return feedFlows;
    }


    /**
     * Walk the flow for a given Root Process Group and return all those Processors who are marked with a Failure Relationship
     */
    public Set<ProcessorDTO> getFailureProcessors(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup g = getFlowOrder(processGroupId, null);
        Set<ProcessorDTO> failureProcessors = new HashSet<>();
        for (NifiVisitableProcessor p : g.getStartingProcessors()) {

            failureProcessors.addAll(p.getFailureProcessors());
        }

        return failureProcessors;
    }


}

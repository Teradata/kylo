/**
 *
 */
package com.thinkbiganalytics.nifi.flow.controller;


import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.client.NifiFlowVisitorClient;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateNameUtil;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.nifi.web.api.dto.AboutDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.search.ComponentSearchResultDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Simple Client that will return a Graph of objects representing the NifiFlow
 */
public class NifiFlowClient implements NifiFlowVisitorClient {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowClient.class);

    @Inject
    private LegacyNifiRestClient client;

    public static CredentialsProvider createCredentialProvider(String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credsProvider;
    }

    public NifiFlowClient(URI base) {
        this(base, null);
    }

    public NifiFlowClient(URI base, CredentialsProvider credsProvider) {}

    ///Core methods to look up Processors and ProcessGroups for the flow
    @Override
    public boolean isConnected() {
        return isConnected(false);
    }


    public boolean isConnected(boolean logException) {
        try {
            AboutDTO aboutEntity = client.getNifiVersion();
            return aboutEntity != null;
        }catch (Exception e){
            if(logException) {
                log.error("Error assessing Nifi Connection {} ", e);
            }
        }
        return false;
    }

    public ProcessGroupDTO getProcessGroup(String processGroupId, boolean recursive, boolean verbose) throws NifiComponentNotFoundException {
        return client.getProcessGroup(processGroupId, recursive, verbose);
    }


    public ProcessGroupDTO getRootProcessGroup() throws NifiComponentNotFoundException {
        return client.getRootProcessGroup();
    }

    public ProcessorDTO getProcessor(String processGroupId, String processorId) throws NifiComponentNotFoundException {
        return client.getProcessor(processGroupId, processorId);
    }

    public ProcessorDTO findProcessorById(String processorId) {
        SearchResultsDTO results = client.search(processorId);
        //log this
        if (results != null && results.getProcessorResults() != null && !results.getProcessorResults().isEmpty()) {
            log.debug("Attempt to find processor by id {}. Processors Found: {} ", processorId, results.getProcessorResults().size());
            ComponentSearchResultDTO processorResult = results.getProcessorResults().get(0);
            String id = processorResult.getId();
            String groupId = processorResult.getGroupId();
            ProcessorDTO processorEntity = getProcessor(groupId, id);

            if (processorEntity != null) {
                return processorEntity;
            }
        } else {
            log.info("Unable to find Processor in Nifi for id: {}", processorId);
        }
        return null;
    }











    private NifiVisitableProcessGroup visitFlow(String processGroup) {
        NifiVisitableProcessGroup visitedGroup = null;
        ProcessGroupDTO processGroupEntity = getProcessGroup(processGroup, true, true);
        if (processGroupEntity != null) {
            visitedGroup = visitFlow(processGroupEntity);
        }
        return visitedGroup;
    }


    public NifiFlowProcessGroup getFeedFlow(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup visitableGroup = visitFlow(processGroupId);
        NifiFlowProcessGroup flow = new NifiFlowBuilder().build(visitableGroup);
        String categoryName = flow.getParentGroupName();
        String feedName = flow.getName();
        feedName = FeedNameUtil.fullName(categoryName, feedName);
        //if it is a versioned feed then strip the version to get the correct feed name
        feedName = NifiTemplateNameUtil.parseVersionedProcessGroupName(feedName);
        flow.setFeedName(feedName);
        return flow;
    }

    public NifiFlowProcessGroup getFeedFlow(ProcessGroupDTO categoryGroup, ProcessGroupDTO feedGroup) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup visitableGroup = visitFlow(feedGroup,categoryGroup);
        NifiFlowProcessGroup flow = new NifiFlowBuilder().build(visitableGroup);
        String categoryName = categoryGroup.getName();
        String feedName = feedGroup.getName();
        feedName = FeedNameUtil.fullName(categoryName, feedName);
        //if it is a versioned feed then strip the version to get the correct feed name
        feedName = NifiTemplateNameUtil.parseVersionedProcessGroupName(feedName);
        flow.setFeedName(feedName);
        return flow;
    }

    //walk entire graph
    public List<NifiFlowProcessGroup> getFeedFlows() {
        log.info("get Graph of Nifi Flows");
        List<NifiFlowProcessGroup> feedFlows = new ArrayList<>();
        ProcessGroupDTO processGroupEntity = getRootProcessGroup();
        ProcessGroupDTO root = processGroupEntity;
        //first level is the category
        for (ProcessGroupDTO category : root.getContents().getProcessGroups()) {
            for (ProcessGroupDTO feedProcessGroup : category.getContents().getProcessGroups()) {
                //second level is the feed
                String feedName = FeedNameUtil.fullName(category.getName(), feedProcessGroup.getName());
                //if it is a versioned feed then strip the version to get the correct feed name
                feedName = NifiTemplateNameUtil.parseVersionedProcessGroupName(feedName);
                log.debug("Get Feed flow for feed: {} ", feedName);
                NifiFlowProcessGroup feedFlow = getFeedFlow(category,feedProcessGroup);
                feedFlow.setFeedName(feedName);
                feedFlows.add(feedFlow);
            }
        }
        log.info("finished Graph of Nifi Flows.  Returning {} flows", feedFlows.size());
        return feedFlows;
    }

    private NifiVisitableProcessGroup visitFlow(ProcessGroupDTO processGroupDTO) {
       return visitFlow(processGroupDTO,null);

    }

    private NifiVisitableProcessGroup visitFlow(ProcessGroupDTO processGroupDTO, ProcessGroupDTO parentProcessGroup) {
        NifiVisitableProcessGroup visitableProcessGroup = new NifiVisitableProcessGroup(processGroupDTO);
        NifiConnectionOrderVisitor visitor = new NifiConnectionOrderVisitor(this, visitableProcessGroup);

        if(parentProcessGroup == null) {
            try {
                //find the parent just to get hte names andids
                ProcessGroupDTO parent = getProcessGroup(processGroupDTO.getParentGroupId(), false, false);
                visitableProcessGroup.setParentProcessGroup(parent);
            } catch (NifiComponentNotFoundException e) {
                //cant find the parent
            }
        }
        else {
            visitableProcessGroup.setParentProcessGroup(parentProcessGroup);
        }

        visitableProcessGroup.accept(visitor);
        return visitableProcessGroup;

    }

    public NifiFlowProcessGroup getFlowForProcessGroup(String processGroupId) {
        NifiFlowProcessGroup group = getFeedFlow(processGroupId);
        log.info("********************** getFlowForProcessGroup  ({})", group);
        NifiFlowDeserializer.constructGraph(group);
        return group;
    }

    public List<NifiFlowProcessGroup> getAllFlows() {
        log.info("********************** STARTING getAllFlows  ");
        System.out.println("********************** STARTING getAllFlows  ");
        List<NifiFlowProcessGroup> groups = getFeedFlows();
        if (groups != null) {
            System.out.println("********************** finished getAllFlows .. construct graph   " + groups.size());
            log.info("********************** getAllFlows  ({})", groups.size());
            groups.stream().forEach(group -> NifiFlowDeserializer.constructGraph(group));
        } else {
            log.info("********************** getAllFlows  (NULL!!!!)");
        }
        return groups;
    }
}

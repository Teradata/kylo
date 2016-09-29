/**
 *
 */
package com.thinkbiganalytics.nifi.flow.controller;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Function;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.client.NifiFlowVisitorClient;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateNameUtil;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.search.ComponentSearchResultDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.NotFoundException;

/**
 * Simple Client that will return a Graph of objects representing the NifiFlow
 */
public class NifiFlowClient implements NifiFlowVisitorClient {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowClient.class);

    public static final List<MediaType> ACCEPT_TYPES = Collections.unmodifiableList(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_XML));

    private String nifiApiPath = "/nifi-api/";
    private final URI base;
    private RestTemplate template;
    private AsyncRestTemplate asyncRestTemplate;

    public static final ParameterizedTypeReference<List<NifiFlowProcessGroup>> FLOW_LIST_TYPE = new ParameterizedTypeReference<List<NifiFlowProcessGroup>>() {
    };


    public static CredentialsProvider createCredentialProvider(String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credsProvider;
    }

    public NifiFlowClient(URI base) {
        this(base, null);
    }

    public NifiFlowClient(URI base, String username, String password) {
        this(base, createCredentialProvider(username, password));
    }

    public NifiFlowClient(URI base, CredentialsProvider credsProvider) {
        super();
        this.base = base;
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        ObjectMapper mapper = createObjectMapper();
        messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
        messageConverters.add(new MappingJackson2HttpMessageConverter(mapper));

        if (credsProvider != null) {
            HttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            ClientHttpRequestFactory reqFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            this.template = new RestTemplate(messageConverters);
            this.template.setRequestFactory(reqFactory);
            CloseableHttpAsyncClient asyncClient = HttpAsyncClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            HttpComponentsAsyncClientHttpRequestFactory asyncRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory(asyncClient);
            asyncRestTemplate = new AsyncRestTemplate(asyncRequestFactory);
            asyncRestTemplate.setMessageConverters(messageConverters);
            //new RestTemplate(reqFactory);
        } else {
            this.template = new RestTemplate(messageConverters);

            asyncRestTemplate = new AsyncRestTemplate();
            asyncRestTemplate.setMessageConverters(messageConverters);
        }

    }


    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    ///Core methods to look up Processors and ProcessGroups for the flow
    @Override
    public boolean isConnected() {
        return isConnected(false);
    }


    public boolean isConnected(boolean logException) {
        try {
            AboutEntity aboutEntity = getWithQueryParams(Paths.get("controller", "about"), null, AboutEntity.class);
            return aboutEntity != null;
        }catch (Exception e){
            if(logException) {
                log.error("Error assessing Nifi Connection {} ", e);
            }
        }
        return false;
    }

    public ProcessGroupEntity getProcessGroup(String processGroupId, boolean recursive, boolean verbose) throws NifiComponentNotFoundException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("recursive", recursive);
            params.put("verbose", verbose);
            return getWithQueryParams(Paths.get("controller", "process-groups", processGroupId), params, ProcessGroupEntity.class);
        } catch (Exception e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }


    public ProcessGroupEntity getRootProcessGroup() throws NifiComponentNotFoundException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("recursive", true);
            params.put("verbose", true);
            return getWithQueryParams(Paths.get("controller", "process-groups", "root"), params, ProcessGroupEntity.class);
        } catch (Exception e) {
            throw new NifiComponentNotFoundException("root", NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    public ProcessorEntity getProcessor(String processGroupId, String processorId) throws NifiComponentNotFoundException {
        try {
            return getWithQueryParams(Paths.get("controller", "process-groups", processGroupId, "processors", processorId), null, ProcessorEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processorId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESSOR, e);
        }
    }

    private SearchResultsEntity search(String query) {
        Map<String, Object> map = new HashMap<>();
        map.put("q", query);
        return getWithQueryParams(Paths.get("controller", "search-results"), map, SearchResultsEntity.class);
    }

    public ProcessorDTO findProcessorById(String processorId) {
        SearchResultsEntity results = search(processorId);
        //log this
        if (results != null && results.getSearchResultsDTO() != null && results.getSearchResultsDTO().getProcessorResults() != null && !results.getSearchResultsDTO().getProcessorResults().isEmpty()) {
            log.debug("Attempt to find processor by id {}. Processors Found: {} ", processorId, results.getSearchResultsDTO().getProcessorResults().size());
            ComponentSearchResultDTO processorResult = results.getSearchResultsDTO().getProcessorResults().get(0);
            String id = processorResult.getId();
            String groupId = processorResult.getGroupId();
            ProcessorEntity processorEntity = getProcessor(groupId, id);

            if (processorEntity != null) {
                return processorEntity.getProcessor();
            }
        } else {
            log.info("Unable to find Processor in Nifi for id: {}", processorId);
        }
        return null;
    }











    private NifiVisitableProcessGroup visitFlow(String processGroup) {
        NifiVisitableProcessGroup visitedGroup = null;
        ProcessGroupEntity processGroupEntity = getProcessGroup(processGroup, true, true);
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


    public Set<ProcessorDTO> getProcessorsForFlow(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup group = visitFlow(processGroupId);
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
        ProcessGroupEntity processGroupEntity = getRootProcessGroup();
        ProcessGroupDTO root = processGroupEntity.getProcessGroup();
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
        List<NifiFlowProcessGroup> feedFlows = new ArrayList<>();
        ProcessGroupEntity processGroupEntity = getRootProcessGroup();
        ProcessGroupDTO root = processGroupEntity.getProcessGroup();
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


    /**
     * Wallk the flow for a given Root Process Group and return all those Processors who are marked with a Failure Relationship
     */
    public Set<ProcessorDTO> getFailureProcessors(String processGroupId) throws NifiComponentNotFoundException {
        NifiVisitableProcessGroup g = visitFlow(processGroupId);
        Set<ProcessorDTO> failureProcessors = new HashSet<>();
        for (NifiVisitableProcessor p : g.getStartingProcessors()) {

            failureProcessors.addAll(p.getFailureProcessors());
        }

        return failureProcessors;
    }


    private NifiVisitableProcessGroup visitFlow(ProcessGroupEntity processGroupEntity) {
        return visitFlow(processGroupEntity.getProcessGroup());

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
                ProcessGroupEntity parent = getProcessGroup(processGroupDTO.getParentGroupId(), false, false);
                visitableProcessGroup.setParentProcessGroup(parent.getProcessGroup());
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


    public NifiFlowProcessGroup getFlowForCategoryAndFeedName(String categoryAndFeedName) {

        NifiFlowProcessGroup group = get(Paths.get("flow", categoryAndFeedName), NifiFlowProcessGroup.class);
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


    private UriComponentsBuilder base(Path path) {
        return UriComponentsBuilder.fromUri(this.base).path(nifiApiPath).path(path.toString());
    }

    private <R> R get(Path path, Class<R> resultType) {
        return get(path, null, resultType);
    }

    private <R> R get(Path path, Function<UriComponentsBuilder, UriComponentsBuilder> filterFunct, Class<R> resultType) {
        return this.template.getForObject(
            (filterFunct != null ? filterFunct.apply(base(path)) : base(path)).build().toUri(),
            resultType);
    }

    private <R> R get(Path path, Function<UriComponentsBuilder, UriComponentsBuilder> filterFunct, ParameterizedTypeReference<R> responseEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(ACCEPT_TYPES);

        ResponseEntity<R> resp = this.template.exchange(
            (filterFunct != null ? filterFunct.apply(base(path)) : base(path)).build().toUri(),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            responseEntity);

        return handle(resp);
    }

    private URI buildURI(Path path, Map<String, Object> queryParams) {
        UriComponentsBuilder builder = base(path);
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.entrySet().forEach(entry ->
                                           {
                                               builder.queryParam(entry.getKey(), entry.getValue());
                                           });

        }
        URI uri = builder.build().toUri();
        log.debug("URI: {}", uri);
        return uri;
    }


    private <R> R getWithQueryParams(Path path, Map<String, Object> queryParams, Class<R> resultType) {
        return this.template.getForObject(
            buildURI(path, queryParams),
            resultType);
    }

    private <R> R getWithQueryParams(Path path, Map<String, Object> queryParams, ParameterizedTypeReference<R> responseEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(ACCEPT_TYPES);

        ResponseEntity<R> resp = this.template.exchange(
            buildURI(path, queryParams),
            HttpMethod.GET,
            new HttpEntity<Object>(headers),
            responseEntity);

        return handle(resp);
    }


    private <R> R handle(ResponseEntity<R> resp) {
        if (resp.getStatusCode().is2xxSuccessful()) {
            return resp.getBody();
        } else {
            throw new NifiFlowClientException(ResponseEntity.status(resp.getStatusCode()).headers(resp.getHeaders()).build());
        }
    }


}

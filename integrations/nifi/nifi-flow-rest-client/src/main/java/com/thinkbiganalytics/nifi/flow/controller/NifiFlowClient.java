/**
 *
 */
package com.thinkbiganalytics.nifi.flow.controller;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Function;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple Client that will return a Graph of objects representing the NifiFlow
 */
public class NifiFlowClient {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowClient.class);

    public static final List<MediaType> ACCEPT_TYPES = Collections.unmodifiableList(Arrays.asList(MediaType.APPLICATION_JSON));


    private final URI base;
    private final RestTemplate template;

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

        if (credsProvider != null) {
            HttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            ClientHttpRequestFactory reqFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            this.template = new RestTemplate(reqFactory);
        } else {
            this.template = new RestTemplate();
        }

        ObjectMapper mapper = createObjectMapper();
        this.template.getMessageConverters().add(new MappingJackson2HttpMessageConverter(mapper));
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }

    public NifiFlowProcessGroup getFlowForProcessGroup(String processGroupId) {
        NifiFlowProcessGroup group = get(Paths.get("flow", processGroupId), NifiFlowProcessGroup.class);
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
        List<NifiFlowProcessGroup> groups = get(Paths.get("flows"), null, FLOW_LIST_TYPE);
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
        return UriComponentsBuilder.fromUri(this.base).path("/feedmgr/nifi/").path(path.toString());
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

    private <R> R handle(ResponseEntity<R> resp) {
        if (resp.getStatusCode().is2xxSuccessful()) {
            return resp.getBody();
        } else {
            throw new NifiFlowClientException(ResponseEntity.status(resp.getStatusCode()).headers(resp.getHeaders()).build());
        }
    }


}

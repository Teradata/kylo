package com.thinkbiganalytics.servicemonitor.rest.client;

/*-
 * #%L
 * thinkbig-service-monitor-ambari
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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by sr186054 on 10/1/15.
 */
public abstract class RestClient {

  private List<HttpMessageConverter> additionalMessageConverters = null;

  private RestTemplate restTemplate = new RestTemplate();

  public RestClient() {

  }

  public abstract RestClientConfig getConfig();

  public RestClient(List<HttpMessageConverter> additionalMessageConverters) {
    setAdditionalMessageConverters(additionalMessageConverters);
  }

  protected void setAdditionalMessageConverters(List<HttpMessageConverter> additionalMessageConverters) {
    this.additionalMessageConverters = additionalMessageConverters;
  }

  protected <T> T doGet(RestCommand<T> restCommand) {
    RestTemplate rest = this.restTemplate;

    HttpHeaders headers = createHeaders(getConfig().getUsername(), getConfig().getPassword());
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));
    HttpEntity entity = new HttpEntity("parameters", headers);

    Map<String, Object> parameters = restCommand.getParameters();
    String url = restCommand.getUrl();
    if (!url.startsWith("/")) {
      url = "/" + url;
    }
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getConfig().getServerUrl() + url);
    if (parameters != null && !parameters.isEmpty()) {
      for (Map.Entry<String, Object> param : parameters.entrySet()) {
        builder.queryParam(param.getKey(), param.getValue());
      }
    }
    if (additionalMessageConverters != null) {
      for (HttpMessageConverter c : this.additionalMessageConverters) {
        rest.getMessageConverters().add(c);
      }
    }
    ResponseEntity<T> response = rest.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity,
                                               restCommand.getResponseType());

    return response.getBody();

  }

  private HttpHeaders createHeaders(final String username, final String password) {
    HttpHeaders headers = new HttpHeaders() {
      {
        String auth = username + ":" + password;
        String enchodedAuth = DatatypeConverter.printBase64Binary(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + enchodedAuth;
        set("Authorization", authHeader);
      }
    };
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    return headers;
  }


  public void setRestTemplate(RestTemplate restTemplate) {
    if (restTemplate != null) {
      this.restTemplate = restTemplate;
    }
  }
}

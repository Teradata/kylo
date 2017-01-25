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

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sr186054 on 10/1/15.
 */
public class TextPlainJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

  public TextPlainJackson2HttpMessageConverter() {
    List<MediaType> types = Arrays.asList(
        new MediaType("application", "json", DEFAULT_CHARSET),
        new MediaType("application", "*+json", DEFAULT_CHARSET),
        new MediaType("text", "plain", DEFAULT_CHARSET));
    super.setSupportedMediaTypes(types);
  }

}

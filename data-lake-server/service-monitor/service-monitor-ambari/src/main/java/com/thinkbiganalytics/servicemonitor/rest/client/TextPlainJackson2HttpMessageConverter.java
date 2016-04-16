package com.thinkbiganalytics.servicemonitor.rest.client;

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

package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

/**
 * Created by sr186054 on 10/2/15.
 */

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
                       "href",
                       "items"
                   })
public class AlertSummary {


  @JsonProperty("items")
  private List<AlertItem> items = new ArrayList<AlertItem>();
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();


  /**
   * @return The items
   */
  @JsonProperty("items")
  public List<AlertItem> getItems() {
    return items;
  }

  /**
   * @param items The items
   */
  @JsonProperty("items")
  public void setItems(List<AlertItem> items) {
    this.items = items;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
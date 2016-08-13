/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DirectoryDatasource.class),
    @JsonSubTypes.Type(value = HiveTableDatasource.class),
    }
)
public class Datasource implements Serializable {
    
    @JsonSerialize(using=DateTimeSerializer.class)
    private DateTime creationTime;

    private String id;
    private String name;
    private String description;
    private String ownder;
    private boolean encrypted;
    private boolean compressed;
    private List<Feed> sourceForFeeds = new ArrayList<>();
    private List<Feed> destinationForFeeds = new ArrayList<>();
    

    public Datasource() {
        super();
    }
    
    public Datasource(String name) {
        super();
        this.name = name;
    }

    public Datasource(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnder() {
        return ownder;
    }

    public void setOwnder(String ownder) {
        this.ownder = ownder;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }
    
    public List<Feed> getSourceForFeeds() {
        return sourceForFeeds;
    }
    
    public List<Feed> getDestinationForFeeds() {
        return destinationForFeeds;
    }

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

}

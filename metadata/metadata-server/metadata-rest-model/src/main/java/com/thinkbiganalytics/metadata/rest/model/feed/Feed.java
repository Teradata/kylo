/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed implements Serializable {

    public enum State { ENABLED, DISABLED, DELETED }

    private String id;
    private String systemName;
    private String displayName;
    private String description;
    private String owner;
    private State state;
    private DateTime createdTime;
    // TODO versions
    private FeedPrecondition precondition;
    private Set<FeedSource> sources = new HashSet<>();
    private Set<FeedDestination> destinations = new HashSet<>();
    private Properties properties = new Properties();
    private FeedCategory category;
    private InitializationStatus currentInitStatus;


    public Feed() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public DateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public FeedPrecondition getPrecondition() {
        return precondition;
    }

    public void setPrecondition(FeedPrecondition trigger) {
        this.precondition = trigger;
    }

    public Set<FeedSource> getSources() {
        return sources;
    }

    public void setSources(Set<FeedSource> sources) {
        this.sources = sources;
    }

    public Set<FeedDestination> getDestinations() {
        return destinations;
    }

    public void setDestinations(Set<FeedDestination> destinations) {
        this.destinations = destinations;
    }

    public FeedDestination getDestination(String datasourceId) {
        for (FeedDestination dest : this.destinations) {
            if (datasourceId.equals(dest.getDatasourceId()) || 
                    dest.getDatasource() != null && datasourceId.equals(dest.getDatasource().getId())) {
                return dest;
            }
        }
        
        return null;
    }


    public FeedCategory getCategory() {
        return category;
    }

    public void setCategory(FeedCategory category) {
        this.category = category;
    }

    public InitializationStatus getCurrentInitStatus() {
        return currentInitStatus;
    }

    public void setCurrentInitStatus(InitializationStatus currentInitStatus) {
        this.currentInitStatus = currentInitStatus;
    }
}

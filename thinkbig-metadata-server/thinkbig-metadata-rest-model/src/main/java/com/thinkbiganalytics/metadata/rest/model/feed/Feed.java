/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {

    public enum State { ENABLED, DISABLED, DELETED }

    private String id;
    private String systemName;
    private String displayName;
    private String description;
    private String owner;
    private State state;
    private boolean initialized;
    // TODO versions
    private FeedPrecondition precondition;
    private Set<FeedSource> sources = new HashSet<>();
    private Set<FeedDestination> destinations = new HashSet<>();

    public Feed() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
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


}

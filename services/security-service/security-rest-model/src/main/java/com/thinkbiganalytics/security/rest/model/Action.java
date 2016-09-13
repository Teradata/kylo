/**
 * 
 */
package com.thinkbiganalytics.security.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String systemName;
    private String title;
    private String description;
    private List<Action> actions = new ArrayList<>();
    
    public Action() {
    }

    public Action(String systemName) {
        this(systemName, null, null);
    }

    public Action(String systemName, String title, String description) {
        super();
        this.systemName = systemName;
        this.title = title;
        this.description = description;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean addAction(Action action) {
        return this.actions.add(action);
    }
   
    public Optional<Action> getAction(String name) {
        return this.actions.stream()
                        .filter(a -> a.getSystemName().equals(name))
                        .findFirst();
    }
}
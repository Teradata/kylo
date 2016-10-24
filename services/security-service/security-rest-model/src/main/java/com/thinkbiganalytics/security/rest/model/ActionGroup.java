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
public class ActionGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private List<Action> actions = new ArrayList<>();

    public ActionGroup() {}

    public ActionGroup(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

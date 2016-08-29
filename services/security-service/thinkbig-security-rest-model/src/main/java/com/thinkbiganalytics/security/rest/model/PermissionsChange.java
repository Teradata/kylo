/**
 * 
 */
package com.thinkbiganalytics.security.rest.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a request to change permissions for set of user/roles.
 * 
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionsChange {
    
    public enum ChangeType { ADD, REMOVE, REPLACE }
    
    private ChangeType change;
    private ActionSet actions;
    private Set<String> users = new HashSet<>();
    private Set<String> groups = new HashSet<>();
    
    public PermissionsChange() {
    }
    
    public PermissionsChange(ChangeType change, String name) {
        this(change, new ActionSet(name));
    }
    
    public PermissionsChange(ChangeType change, ActionSet actions) {
        super();
        this.change = change;
        this.actions = actions;
    }

    public ChangeType getChange() {
        return change;
    }

    public void setChange(ChangeType change) {
        this.change = change;
    }

    public ActionSet getActions() {
        return actions;
    }

    public void setActions(ActionSet actions) {
        this.actions = actions;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }
    
    public boolean addUser(String name) {
        return this.users.add(name);
    }
    
    public boolean addGroup(String name) {
        return this.groups.add(name);
    }

    public boolean addAction(Action action) {
        return this.actions.addAction(action);
    }
}

/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed.transform;

/**
 *
 * @author Sean Felten
 */
public class FieldCleansor {

    private String name;
    private String description;
    private String rule; // TODO what is this?

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

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

}

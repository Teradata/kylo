/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.extension;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensibleTypeDescriptor {

    private String name;
    private String displayName;
    private String description;
    private DateTime createdTime;
    private DateTime modifiedTime;
    private Set<FieldDescriptor> fields = new HashSet<>();
    
    public ExtensibleTypeDescriptor() {
    }
    
    public FieldDescriptor addField(String name, FieldDescriptor.Type type) {
        return addField(new FieldDescriptor(name, type));
    }
    
    public FieldDescriptor addField(FieldDescriptor field) {
        this.fields.add(field);
        return field;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public Set<FieldDescriptor> getFields() {
        return fields;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }


}

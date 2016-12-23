/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.op;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset implements Serializable {

    public enum ChangeType {
        UPDATE, DELETE
    }
    
    public enum ContentType {
        PARTITIONS, FILES
    }

    private String time;
    private Datasource datasource;
    private ChangeType changeType;
    private ContentType contentType;
    private List<? extends ChangeSet> changeSets;
    
    public Dataset() {
        super();
    }
    
    @Override
    public String toString() {
        return "Dataset - datasource: " + Objects.toString(this.datasource.getName()) + ", content type: " 
                + Objects.toString(this.contentType) + ", changes set: " + Objects.toString(this.changeSets);
    }
    
    public Dataset(Datasource src, ChangeType change, ContentType content, ChangeSet... changeSets) {
        this(new DateTime(), src, change, content, Arrays.asList(changeSets));
    }
    
    public Dataset(Datasource src, ChangeType change, ContentType content, List<? extends ChangeSet> changeSets) {
        this(new DateTime(), src, change, content, changeSets);
    }
    
    public Dataset(DateTime time, Datasource src, ChangeType change, ContentType content, ChangeSet... changeSets) {
        this(time, src, change, content, Arrays.asList(changeSets));
    }
    
    public Dataset(DateTime time, Datasource src, ChangeType change, ContentType content, List<? extends ChangeSet> changeSets) {
        super();
        this.time = Formatters.print(time);
        this.datasource = src;
        this.changeType = change;
        this.contentType = content;
        this.changeSets = changeSets;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    @JsonIgnore
    public void setTime(DateTime dateTime) {
        this.time = Formatters.print(dateTime);
    }
    
    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType getType) {
        this.changeType = getType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public List<? extends ChangeSet> getChangeSets() {
        return changeSets;
    }

    public void setChangeSets(List<? extends ChangeSet> changeSets) {
        this.changeSets = changeSets;
    }
}

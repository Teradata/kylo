package com.thinkbiganalytics.metadata.jpa.category;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.jpa.AuditTimestampListener;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.core.BaseId;
import com.thinkbiganalytics.metadata.jpa.NamedJpaQueries;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Created by sr186054 on 5/3/16.
 */
@Entity
@Table(name="CATEGORY")
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditTimestampListener.class)
@NamedQuery(
        name= NamedJpaQueries.CATEGORY_FIND_BY_SYSTEM_NAME,
        query="FROM JpaCategory c WHERE name = :systemName"
)
public class JpaCategory extends AbstractAuditedEntity implements Category {

    @EmbeddedId
    private CategoryId id;

    @OneToMany(targetEntity=JpaFeed.class,mappedBy = "category")
    private List<Feed> feeds;

    @Column(name="DISPLAY_NAME")
    private String displayName;

    @Column(name="NAME")
    private String name;

    @Column(name="DESCRIPTION")
    private String description;

    @Column(name="VERSION")
    @Version
    private Integer version = 1;

    public JpaCategory(CategoryId id){
        this.id = id;
    }


    public JpaCategory(){

    }

    public JpaCategory(String systemName){
        this.id = JpaCategory.CategoryId.create();
        this.setName(systemName);
    }

    @Override
    public List<? extends Feed> getFeeds() {
        return feeds;
    }

    @Override
    public ID getId() {
        return id;
    }

    public void setId(CategoryId id) {
        this.id = id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Embeddable
    public static class CategoryId extends BaseId implements ID {

        private static final long serialVersionUID = 241001606640713117L;

        @Column(name="id", columnDefinition="binary(16)", length = 16)
        private UUID uuid;

        public static CategoryId create() {
            return new CategoryId(UUID.randomUUID());
        }

        public CategoryId() {
        }

        public CategoryId(Serializable ser) {
            super(ser);
        }

        @Override
        public UUID getUuid() {
            return this.uuid;
        }

        @Override
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }

    @Override
    public Map<String, String> getUserProperties() {
        return null;
    }

    public void setUserProperties(Map<String, String> userProperties) {

    }

    @Override
    public void setUserProperties(Map<String, String> userProperties, Set<UserFieldDescriptor> userFields) {

    }
}

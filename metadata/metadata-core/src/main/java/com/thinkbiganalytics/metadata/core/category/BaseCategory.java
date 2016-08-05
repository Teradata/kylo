package com.thinkbiganalytics.metadata.core.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * A POJO implementation of {@link Category}.
 */
public class BaseCategory implements Category {

    private CategoryId id;

    private List<Feed> feeds;

    private String displayName;

    private String name;

    private String description;

    private Integer version;

    private DateTime createdTime;

    private DateTime modifiedTime;

    /**
     * User-defined properties
     */
    private Map<String, String> userProperties;

    @Override
    public CategoryId getId() {
        return id;
    }

    public void setId(CategoryId id) {
        this.id = id;
    }

    @Override
    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    @Override
    public String getDisplayName() {
        return displayName;
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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    private static class BaseId {

        private final UUID uuid;

        public BaseId() {
            this.uuid = UUID.randomUUID();
        }

        public BaseId(Serializable ser) {
            if (ser instanceof String) {
                this.uuid = UUID.fromString((String) ser);
            } else if (ser instanceof UUID) {
                this.uuid = (UUID) ser;
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass().isAssignableFrom(obj.getClass())) {
                BaseId that = (BaseId) obj;
                return Objects.equals(this.uuid, that.uuid);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }

        @Override
        public String toString() {
            return this.uuid.toString();
        }
    }

    protected static class CategoryId extends BaseId implements Category.ID {

        public CategoryId() {
            super();
        }

        public CategoryId(Serializable ser) {
            super(ser);
        }
    }

    @Override
    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Nonnull
    @Override
    public Map<String, String> getUserProperties() {
        return userProperties;
    }

    @Override
    public void setUserProperties(@Nonnull Map<String, String> userProperties) {
        this.userProperties = userProperties;
    }
}

package com.thinkbiganalytics.metadata.jpa.feedmgr.category;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.jpa.BaseId;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.feed.JpaFeedManagerFeed;
import com.thinkbiganalytics.metadata.jpa.op.JpaDataOperation;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by sr186054 on 5/3/16.
 */
@Entity
@Table(name="FM_CATEGORY")
@NamedQuery(
        name= FeedManagerNamedQueries.CATEGORY_FIND_BY_SYSTEM_NAME,
        query="FROM JpaFeedManagerCategory c WHERE systemName = :systemName"
)
public class JpaFeedManagerCategory  extends AbstractAuditedEntity implements com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory {

    @EmbeddedId
    private FeedManagerCategoryId id;

    @OneToMany(targetEntity=JpaFeedManagerFeed.class)
    @JoinColumn(name = "id")
    private List<FeedManagerFeed> feeds;


    @Column(name="DISPLAY_NAME")
    private String displayName;

    @Column(name="SYSTEM_NAME")
    private String systemName;

    @Column(name="DESCRIPTION")
    private String description;

    @Column(name="ICON")
    private String icon;

    @Column(name="ICON_COLOR")
    private String iconColor;

    @Lob
    @Basic
    @Column(name="JSON")
    private String json;

    public JpaFeedManagerCategory(FeedManagerCategoryId id){
        this.id = id;
    }

    public JpaFeedManagerCategory(String id){
        this.id = new FeedManagerCategoryId(id);
    }

    public JpaFeedManagerCategory(){

    }

    @Override
    public List<FeedManagerFeed> getFeeds() {
        return feeds;
    }

    @Override
    public void setFeeds(List<FeedManagerFeed> feeds) {
        this.feeds = feeds;
    }

    @Override
    public FeedManagerCategory.ID getId() {
        return id;
    }

    public void setId(FeedManagerCategoryId id) {
        this.id = id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    @Override
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    @Override
    public String getJson() {
        return json;
    }

    @Override
    public void setJson(String json) {
        this.json = json;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Embeddable
    public static class FeedManagerCategoryId extends BaseId implements FeedManagerCategory.ID {

        private static final long serialVersionUID = 241001606640713117L;

        @Column(name="id", columnDefinition="binary(16)", length = 16)
        private UUID uuid;

        public static FeedManagerCategoryId create() {
            return new FeedManagerCategoryId(UUID.randomUUID());
        }

        public FeedManagerCategoryId() {
        }

        public FeedManagerCategoryId(Serializable ser) {
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
}

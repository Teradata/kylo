package com.thinkbiganalytics.metadata.jpa.feedmgr.template;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.core.BaseId;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.feed.JpaFeedManagerFeed;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by sr186054 on 5/3/16.
 */
@Entity
@Table(name = "FM_TEMPLATE")
@NamedQueries(
        {@NamedQuery(
                name = FeedManagerNamedQueries.TEMPLATE_FIND_BY_NAME,
                query = "FROM JpaFeedManagerTemplate WHERE name = :name"
        ),
                @NamedQuery(
                        name = FeedManagerNamedQueries.TEMPLATE_FIND_BY_NIFI_ID,
                        query = "FROM JpaFeedManagerTemplate WHERE nifiTemplateId = :nifiTemplateId"
                )})
public class JpaFeedManagerTemplate extends AbstractAuditedEntity implements com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate {


    @OneToMany(targetEntity = JpaFeedManagerFeed.class)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private List<FeedManagerFeed> feeds;

    @EmbeddedId
    private FeedManagerTemplateId id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "NIFI_TEMPLATE_ID")
    private String nifiTemplateId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_DEFINE_TABLE")
    @Type(type = "yes_no")
    private boolean defineTable;

    @Column(name = "IS_DATA_TRANSFORM")
    @Type(type = "yes_no")
    private boolean dataTransformation;

    @Column(name = "ALLOW_PRECONDITIONS")
    @Type(type = "yes_no")
    private boolean allowPreconditions;

    @Column(name = "ICON")
    private String icon;

    @Column(name = "ICON_COLOR")
    private String iconColor;

    @Column(name = "STATE")
    private String state;

    @Lob
    @Basic
    @Column(name = "JSON")
    private String json;

    public JpaFeedManagerTemplate(FeedManagerTemplateId id) {
        this.id = id;
    }

    public JpaFeedManagerTemplate() {

    }

    public JpaFeedManagerTemplate(String name) {
        this.id = JpaFeedManagerTemplate.FeedManagerTemplateId.create();
        this.name = name;
    }

    @Override
    public List<FeedManagerFeed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<FeedManagerFeed> feeds) {
        this.feeds = feeds;
    }

    @Override
    public FeedManagerTemplateId getId() {
        return id;
    }

    public void setId(FeedManagerTemplateId id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getNifiTemplateId() {
        return nifiTemplateId;
    }

    public void setNifiTemplateId(String nifiTemplateId) {
        this.nifiTemplateId = nifiTemplateId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public boolean isDefineTable() {
        return defineTable;
    }

    public void setDefineTable(boolean defineTable) {
        this.defineTable = defineTable;
    }

    @Override
    public boolean isDataTransformation() {
        return dataTransformation;
    }

    public void setDataTransformation(boolean dataTransformation) {
        this.dataTransformation = dataTransformation;
    }

    @Override
    public boolean isAllowPreconditions() {
        return allowPreconditions;
    }

    public void setAllowPreconditions(boolean allowPreconditions) {
        this.allowPreconditions = allowPreconditions;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
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

    public void setJson(String json) {
        this.json = json;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Embeddable
    public static class FeedManagerTemplateId extends BaseId implements FeedManagerTemplate.ID {

        private static final long serialVersionUID = 241001606640713117L;

        @Column(name = "id", columnDefinition = "binary(16)", length = 16)
        private UUID uuid;

        public static FeedManagerTemplateId create() {
            return new FeedManagerTemplateId(UUID.randomUUID());
        }

        public FeedManagerTemplateId() {
        }

        public FeedManagerTemplateId(Serializable ser) {
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

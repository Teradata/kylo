package com.thinkbiganalytics.metadata.jpa.feedmgr.feed;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.jpa.BaseId;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategory;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplate;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


/**
 * Created by sr186054 on 5/3/16.
 */
@Entity
@Table(name="FM_FEED")
@NamedQueries(
        {@NamedQuery(
                name = FeedManagerNamedQueries.FEED_FIND_BY_SYSTEM_NAME,
                query = "FROM JpaFeedManagerFeed fmf WHERE fmf.feed.name = :systemName"
        ),
        @NamedQuery(name=FeedManagerNamedQueries.FEED_FIND_BY_TEMPLATE_ID,
        query = "FROM JpaFeedManagerFeed fmf WHERE fmf.template.id = :templateId"),
                @NamedQuery(name=FeedManagerNamedQueries.FEED_FIND_BY_CATEGORY_ID,
                        query = "FROM JpaFeedManagerFeed fmf WHERE fmf.category.id = :categoryId"),

        })
public class JpaFeedManagerFeed  extends AbstractAuditedEntity implements com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed {


    @OneToOne(targetEntity = JpaFeed.class, cascade = CascadeType.MERGE)
    @JoinColumn(name="feed_id",nullable = false, unique = true)
    private Feed feed;

    @EmbeddedId
    private FeedManagerFeedId id;

    @ManyToOne(targetEntity = JpaFeedManagerCategory.class)
    @JoinColumn(name="category_id", nullable = false, insertable = true, updatable = false )
    private FeedManagerCategory category;

    @ManyToOne(targetEntity = JpaFeedManagerTemplate.class)
    @JoinColumn(name="template_id", nullable = false, insertable = true, updatable = false)
    private FeedManagerTemplate template;

    @Lob
    @Column(name="JSON")
    private String json;

    @Column(name="STATE" )
    private String state;

    public JpaFeedManagerFeed(FeedManagerFeedId id){
        this.id = id;
    }

    public JpaFeedManagerFeed(){

    }
    @Override
    public Feed getFeed() {
        return feed;
    }

    @Override
    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    @Override
    public FeedManagerFeedId getId() {
        return id;
    }

    public void setId(FeedManagerFeedId id) {
        this.id = id;
    }

    @Override
    public FeedManagerCategory getCategory() {
        return category;
    }

    @Override
    public void setCategory(FeedManagerCategory category) {
        this.category = category;
    }

    @Override
    public String getJson() {
        return json;
    }

    @Override
    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public FeedManagerTemplate getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(FeedManagerTemplate template) {
        this.template = template;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isNew(){
        return StringUtils.isBlank(state) || "NEW".equalsIgnoreCase(state);
    }

    @Embeddable
    public static class FeedManagerFeedId extends BaseId implements FeedManagerFeed.ID {

        private static final long serialVersionUID = 241001606640713117L;

        @Column(name="id", columnDefinition="binary(16)", length = 16)
        private UUID uuid;

        public static FeedManagerFeedId create() {
            return new FeedManagerFeedId(UUID.randomUUID());
        }

        public FeedManagerFeedId() {
        }

        public FeedManagerFeedId(Serializable ser) {
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

package com.thinkbiganalytics.metadata.jpa.feedmgr.feed;

import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategory;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplate;

import javax.persistence.*;


/**
 * Created by sr186054 on 5/3/16.
 */
@Entity
@Table(name = "FM_FEED")
@PrimaryKeyJoinColumn(referencedColumnName="id")
@NamedQueries(
        {@NamedQuery(
                name = FeedManagerNamedQueries.FEED_FIND_BY_SYSTEM_NAME,
                query = "FROM JpaFeedManagerFeed fmf WHERE fmf.name = :systemName"
        ),
                @NamedQuery(name = FeedManagerNamedQueries.FEED_FIND_BY_TEMPLATE_ID,
                        query = "FROM JpaFeedManagerFeed fmf WHERE fmf.template.id = :templateId"),
                @NamedQuery(name = FeedManagerNamedQueries.FEED_FIND_BY_CATEGORY_ID,
                        query = "FROM JpaFeedManagerFeed fmf WHERE fmf.category.id = :categoryId"),

        })

public class JpaFeedManagerFeed extends JpaFeed implements FeedManagerFeed {


    @ManyToOne(targetEntity = JpaFeedManagerCategory.class)
    @JoinColumn(name = "category_id", nullable = false, insertable = true, updatable = false)
    private FeedManagerCategory category;

    @ManyToOne(targetEntity = JpaFeedManagerTemplate.class)
    @JoinColumn(name = "template_id", nullable = false, insertable = true, updatable = false)
    private FeedManagerTemplate template;

    @Lob
    @Column(name = "JSON")
    private String json;


    public JpaFeedManagerFeed(FeedId id) {
        super(id);

    }

    public JpaFeedManagerFeed(FeedId id,String name, String description) {
        super(name, description);
        this.setId(id);
    }

    public JpaFeedManagerFeed(String name, String description) {
        super(name, description);
    }

    public JpaFeedManagerFeed() {
        super();
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

    public boolean isNew() {
        return getState() == null || State.NEW.equals(getState());
    }





}

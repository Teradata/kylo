package com.thinkbiganalytics.metadata.jpa.feedmgr.feed;

import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategory;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplate;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;


/**
 * Created by sr186054 on 5/3/16.
 */
@Entity
@Table(name = "FM_FEED")
@PrimaryKeyJoinColumn(referencedColumnName="id")
@NamedQueries(
        {@NamedQuery(
                name = FeedManagerNamedQueries.FEED_FIND_BY_SYSTEM_NAME,
                query = "select feed FROM JpaFeedManagerFeed as feed INNER JOIN FETCH feed.category as c WHERE feed.name = :systemName and c.name = :categorySystemName"
        ),
                @NamedQuery(name = FeedManagerNamedQueries.FEED_FIND_BY_TEMPLATE_ID,
                        query = "FROM JpaFeedManagerFeed as feed WHERE feed.template.id = :templateId"),
                @NamedQuery(name = FeedManagerNamedQueries.FEED_FIND_BY_CATEGORY_ID,
                        query = "FROM JpaFeedManagerFeed as feed WHERE feed.category.id = :categoryId"),

        })
public class JpaFeedManagerFeed<C extends JpaFeedManagerCategory> extends JpaFeed<C> implements FeedManagerFeed<C> {


    @ManyToOne(targetEntity = JpaFeedManagerTemplate.class)
    @JoinColumn(name = "template_id", nullable = false, insertable = true, updatable = false)
    private FeedManagerTemplate template;

    @Lob
    @Column(name = "JSON")
    private String json;

    @Column(name="nifi_process_group_id")
    private String nifiProcessGroupId;

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

    public C getCategory() {
        return super.getCategory();
    }

    @ManyToOne(targetEntity = JpaFeedManagerCategory.class)
    @JoinColumn(name = "category_id", nullable = false, insertable = true, updatable = false)
    public void setCategory(C category) {
        super.setCategory(category);
    }

    public String getNifiProcessGroupId() {
        return nifiProcessGroupId;
    }

    public void setNifiProcessGroupId(String nifiProcessGroupId) {
        this.nifiProcessGroupId = nifiProcessGroupId;
    }

    @Override
    public void setVersionName(String version) {
        if (StringUtils.isNotBlank(version)) {
            Long l = new Long(version);
            setVersion(l.intValue());
        }
    }
}

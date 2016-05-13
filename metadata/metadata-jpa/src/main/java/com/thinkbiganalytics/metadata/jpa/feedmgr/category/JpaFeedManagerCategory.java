package com.thinkbiganalytics.metadata.jpa.feedmgr.category;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.jpa.BaseId;
import com.thinkbiganalytics.metadata.jpa.category.JpaCategory;
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
@PrimaryKeyJoinColumn(referencedColumnName="id")
public class JpaFeedManagerCategory  extends JpaCategory implements FeedManagerCategory {

    @Column(name="ICON")
    private String icon;

    @Column(name="ICON_COLOR")
    private String iconColor;

    public JpaFeedManagerCategory(CategoryId id){
       super(id);
    }


    public JpaFeedManagerCategory(){

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




}

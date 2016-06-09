package com.thinkbiganalytics.metadata.jpa.feedmgr.category;

import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.jpa.category.JpaCategory;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;

import javax.persistence.*;

/**
 * Created by sr186054 on 5/3/16.
 */
@Entity
@Table(name="FM_CATEGORY")
@PrimaryKeyJoinColumn(referencedColumnName="id")
@NamedQuery(
        name= FeedManagerNamedQueries.CATEGORY_FIND_BY_SYSTEM_NAME,
        query="select c FROM JpaFeedManagerCategory c WHERE name = :systemName"
)
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


    public JpaFeedManagerCategory(String name) {
        super(name);
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

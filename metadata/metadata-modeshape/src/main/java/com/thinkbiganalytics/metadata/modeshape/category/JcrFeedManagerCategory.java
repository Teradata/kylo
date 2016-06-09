package com.thinkbiganalytics.metadata.modeshape.category;

import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;

import javax.jcr.Node;

/**
 * Created by sr186054 on 6/8/16.
 */
public class JcrFeedManagerCategory extends JcrCategory implements FeedManagerCategory {

    public static String ICON = "tba:icon";
    public static String ICON_COLOR = "tba:iconColor";

    public JcrFeedManagerCategory(Node node) {
        super(node);
    }

    public JcrFeedManagerCategory(JcrCategory category) {
        super(category.getNode());
    }

    @Override
    public String getIcon() {
        return getProperty(ICON, String.class);
    }

    @Override
    public String getIconColor() {
        return getProperty(ICON_COLOR, String.class);
    }


    public void setIcon(String icon) {
        setProperty(ICON, icon);
    }

    public void setIconColor(String iconColor) {
        setProperty(ICON_COLOR, iconColor);
    }
}

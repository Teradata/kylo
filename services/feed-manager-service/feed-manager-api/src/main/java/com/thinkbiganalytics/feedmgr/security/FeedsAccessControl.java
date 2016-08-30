/**
 * 
 */
package com.thinkbiganalytics.feedmgr.security;

import com.thinkbiganalytics.security.action.Action;

/**
 * Actions involving feeds.
 * 
 * @author Sean Felten
 */
public interface FeedsAccessControl {

    public static final Action FEEDS_SUPPORT = Action.create("accessFeedsSupport");
    public static final Action ACCESS_CATEGORIES = FEEDS_SUPPORT.subAction("accessCategories");
    public static final Action CREATE_CATEGORIES = ACCESS_CATEGORIES.subAction("createCategories");
    public static final Action DELETE_CATEGORIES = ACCESS_CATEGORIES.subAction("deleteCategories");
    public static final Action ADMIN_CATEGORIES = ACCESS_CATEGORIES.subAction("adminCategories");
    public static final Action ACCESS_FEEDS = FEEDS_SUPPORT.subAction("accessFeeds");
    public static final Action CREATE_FEEDS = ACCESS_FEEDS.subAction("createFeeds");
    public static final Action DELETE_FEEDS = ACCESS_FEEDS.subAction("deleteFeeds");
    public static final Action IMPORT_FEEDS = ACCESS_FEEDS.subAction("importFeeds");
    public static final Action EXPORT_FEEDS = ACCESS_FEEDS.subAction("exportFeeds");
    public static final Action ADMIN_FEEDS = ACCESS_FEEDS.subAction("adminFeeds");
    public static final Action ACCESS_TEMPLATES = FEEDS_SUPPORT.subAction("accessTemplates");
    public static final Action CREATE_TEMPLATES = ACCESS_TEMPLATES.subAction("createTemplates");
    public static final Action DELETE_TEMPLATES = ACCESS_TEMPLATES.subAction("deleteTemplates");
    public static final Action ADMIN_TEMPLATES = ACCESS_TEMPLATES.subAction("adminTemplates");
    
    
}

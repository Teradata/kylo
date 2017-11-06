define([], function () {
    var AccessConstants = function () {
        /**
         * Allows access to categories.
         * @type {string}
         */
        this.CATEGORIES_ACCESS = "accessCategories";

        /**
         * Allows the administration of any category; even those created by others.
         * @type {string}
         */
        this.CATEGORIES_ADMIN = "adminCategories";

        /**
         * Allows creating and editing new categories.
         * @type {string}
         */
        this.CATEGORIES_EDIT = "editCategories";

        /**
         * Allows access to data sources.
         * @type {string}
         */
        this.DATASOURCE_ACCESS = "accessDatasources";

        /**
         * Allows creating and editing new data sources.
         * @type {string}
         */
        this.DATASOURCE_EDIT = "editDatasources";

        /**
         * Allows access to feeds.
         * @type {string}
         */
        this.FEEDS_ACCESS = "accessFeeds";

        /**
         * Allows the administration of any feed; even those created by others.
         * @type {string}
         */
        this.FEEDS_ADMIN = "adminFeeds";

        /**
         * Allows creating and editing new feeds.
         * @type {string}
         */
        this.FEEDS_EDIT = "editFeeds";

        /**
         * Allows exporting feeds definitions.
         * @type {string}
         */
        this.FEEDS_EXPORT = "exportFeeds";

        /**
         * Allows importing of previously exported feeds.
         * @type {string}
         */
        this.FEEDS_IMPORT = "importFeeds";

        /**
         * Allows access to feeds and feed-related functions.
         * @type {string}
         */
        this.FEED_MANAGER_ACCESS = "accessFeedsSupport";

        /**
         * Allows the ability to view existing groups.
         * @type {string}
         */
        this.GROUP_ACCESS = "accessGroups";

        /**
         * Allows the ability to create and manage groups.
         * @type {string}
         */
        this.GROUP_ADMIN = "adminGroups";

        /**
         * Allows access to Tables page
         * @type {string}
         */
        this.TABLES_ACCESS = "accessTables";

        /**
         * Allows users to access the SLA page
         * @type {string}
         */
        this.SLA_ACCESS = "accessServiceLevelAgreements";

        /**
         * Allows users to create new Service Level agreements
         * @type {string}
         */
        this.SLA_EDIT = "editServiceLevelAgreements";

        /**
         * Allows users to create new Service Level agreements
         * @type {string}
         */
        this.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE = "editServiceLevelAgreementEmailTemplate";

        /**
         * Allows access to feed templates.
         * @type {string}
         */
        this.TEMPLATES_ACCESS = "accessTemplates";

        /**
         * Allows the administration of any feed template; even those created by others.
         * @type {string}
         */
        this.TEMPLATES_ADMIN = "adminTemplates";

        /**
         * Allows created and editing new feed templates.
         * @type {string}
         */
        this.TEMPLATES_EDIT = "editTemplates";

        /**
         * Allows exporting template definitions.
         * @type {string}
         */
        this.TEMPLATES_EXPORT = "exportTemplates";

        /**
         * Allows importing of previously exported templates.
         * @type {string}
         */
        this.TEMPLATES_IMPORT = "importTemplates";

        /**
         * Allows the ability to view existing users.
         * @type {string}
         */
        this.USERS_ACCESS = "accessUsers";

        /**
         * Allows the ability to create and manage users.
         * @type {string}
         */
        this.USERS_ADMIN = "adminUsers";

        /**
         * Allows access to user and group-related functions.
         * @type {string}TEMPLATES_IMPORT
         */
        this.USERS_GROUPS_ACCESS = "accessUsersGroupsSupport";

        /**
         * allows access to the visual query link on the left
         * @type {string}
         */
        this.VISUAL_QUERY_ACCESS = "accessVisualQuery";

        /**
         * Access Search
         * @type {string}
         */
        this.SEARCH_ACCESS = "searchAccess";

        /**
         * Allows administration of operations; such as stopping and abandoning them.
         * @type {string}
         */
        this.OPERATIONS_ADMIN = "adminOperations";

        /**
         * Allows access to operational information like active feeds and execution history; etc.
         * @type {string}
         */
        this.OPERATIONS_MANAGER_ACCESS = "accessOperations";

        /**
         * access to ops manager alerts
         * @type {string}
         */
        this.ALERTS_ACCESS = "accessAlerts";

        /**
         * Access to ops manager feed details
         * @type {string}
         */
        this.FEED_OPERATIONS_DETAIL_ACCESS = "accessOperationsFeedDetails";

        /**
         * Access to ops manager jobs
         * @type {string}
         */
        this.JOBS_ACCESS = "accessJobs";

        /**
         * Access to ops manager job details
         * @type {string}
         */
        this.JOB_DETAILS_ACCESS = "accessJobDetails";

        /**
         * Access Services
         * @type {string}
         */
        this.SERVICES_ACCESS = "accessServices";

        /**
         * Access to ops manager charts
         * @type {string}
         */
        this.CHARTS_ACCESS = "accessCharts";

        /**
         * Allows access to search all indexed columns
         * @type {string}
         */
        this.GLOBAL_SEARCH_ACCESS = "accessSearch";

        this.ADMIN_METADATA= "adminMetadata";

        this.ENTITY_ACCESS = {
            CATEGORY: {
                //   EDIT_CATEGORY_SUMMARY: "editCategorySummary", // will not be used in v 0.8.0
                EDIT_CATEGORY_DETAILS: "editCategoryDetails",
                DELETE_CATEGORY: "deleteCategory",
                CREATE_FEED: "createFeedUnderCategory",
                ENABLE_CATEGORY: "enableCategory",
                CHANGE_CATEGORY_PERMISSIONS: "changeCategoryPermissions"
            },
            FEED: {
                //EDIT_FEED_SUMMARY: "editFeedSummary", // will not be used in v0.8.0
                EDIT_FEED_DETAILS: "editFeedDetails",
                DELETE_FEED: "deleteFeed",
                //ENABLE_FEED: "enableFeed",  /// Do we need this??... can enable be inferred from edit details
                CHANGE_FEED_PERMISSIONS: "changeFeedPermissions",
                EXPORT: "exportFeed"
            },
            TEMPLATE: {
                EDIT_TEMPLATE: "editTemplate",
                DELETE_TEMPLATE: "deleteTemplate",
                EXPORT: "exportTemplate",
                CREATE_TEMPLATE: "createFeedFromTemplate",
                CHANGE_TEMPLATE_PERMISSIONS: "changeTemplatePermissions"
            },
            DATASOURCE: {
                EDIT_DETAILS: "editDatasourceDetails",
                DELETE_DATASOURCE: "deleteDatasource",
                CHANGE_DATASOURCE_PERMISSIONS: "changeDatasourcePermissions"
            }
        };

        this.UI_STATES = {
            FEEDS: {state: "feeds", permissions: [this.FEEDS_ACCESS]},
            DEFINE_FEED: {state: "define-feed", permissions: [this.FEEDS_EDIT]},
            DEFINE_FEED_COMPLETE: {state: "define-feed-complete", permissions: [this.FEEDS_ACCESS]},
            IMPORT_FEED: {state: "import-feed", permissions: [this.FEEDS_IMPORT]},
            FEED_DETAILS: {state: "feed-details", permissions: [this.FEEDS_ACCESS]},
            EDIT_FEED: {state: "edit-feed", permissions: [this.FEEDS_ACCESS]},
            CATEGORIES: {state: "categories", permissions: [this.CATEGORIES_ACCESS]},
            CATEGORY_DETAILS: {state: "category-details", permissions: [this.CATEGORIES_ACCESS]},
            BUSINESS_METADATA: {state: "business-metadata", permissions: [this.CATEGORIES_ADMIN]},
            USERS: {state: "users", permissions: [this.USERS_ACCESS]},
            USERS_DETAILS: {state: "user-details", permissions: [this.USERS_ACCESS]},
            GROUPS: {state: "groups", permissions: [this.USERS_GROUPS_ACCESS]},
            GROUP_DETAILS: {state: "group-details", permissions: [this.USERS_GROUPS_ACCESS]},
            VISUAL_QUERY: {state: "visual-query", permissions: [this.VISUAL_QUERY_ACCESS]},
            SERVICE_LEVEL_AGREEMENTS: {state: "service-level-agreements", permissions: [this.SLA_ACCESS]},
            SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES: {state: "sla-email-templates", permissions: [this.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE]},
            TABLES: {state: "tables", permissions: [this.TABLES_ACCESS]},
            TABLE: {state: "table", permissions: [this.TABLES_ACCESS]},
            DATASOURCES: {state: "datasources", permissions: [this.DATASOURCE_ACCESS]},
            DATASOURCE_DETAILS: {state: "datasource-details", permissions: [this.DATASOURCE_ACCESS]},
            REGISTERED_TEMPLATES: {state: "registered-templates", permissions: [this.TEMPLATES_ACCESS]},
            REGISTER_NEW_TEMPLATE: {state: "register-new-template", permissions: [this.TEMPLATES_EDIT]},
            REGISTER_TEMPLATE: {state: "register-template", permissions: [this.TEMPLATES_EDIT]},
            REGISTER_TEMPLATE_COMPLETE: {state: "register-template-complete", permissions: [this.TEMPLATES_EDIT]},
            IMPORT_TEMPLATE: {state: "import-template", permissions: [this.TEMPLATES_IMPORT]},
            SEARCH: {state: "search", permissions: []},
            DOMAIN_TYPES: {state: "domain-types", permissions: [this.FEEDS_ADMIN]},
            DOMAIN_TYPE_DETAILS: {state: "domain-type-details", permissions: [this.FEEDS_ADMIN]},
            JCR_ADMIN:{state:"jcr-query",permissions:[this.ADMIN_METADATA]},
            //Ops Manager
            ALERTS: {state: "alerts", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            ALERT_DETAILS: {state: "alert-details", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            CHARTS: {state: "charts", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            OPS_FEED_DETAILS: {state: "ops-feed-details", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            FEED_STATS: {state: "feed-stats", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            JOBS: {state: "jobs", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            JOB_DETAILS: {state: "job-details", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            DASHBOARD: {state: "dashboard", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            SCHEDULER: {state: "scheduler", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            SERVICE_HEALTH: {state: "service-health", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            SERVICE_DETAILS: {state: "service-details", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            SERVICE_COMPONENT_DETAILS: {state: "service-component-details", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            SERVICE_LEVEL_ASSESSMENTS: {state: "service-level-assessments", permissions: [this.OPERATIONS_MANAGER_ACCESS]},
            SERVICE_LEVEL_ASSESSMENT: {state: "service-level-assessment", permissions: [this.OPERATIONS_MANAGER_ACCESS]}
        }
    };
    return new AccessConstants();
});

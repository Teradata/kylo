define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessConstants = /** @class */ (function () {
        function AccessConstants() {
        }
        /**
         * Allows access to categories.
         * @type {string}
         */
        AccessConstants.CATEGORIES_ACCESS = "accessCategories";
        /**
         * Allows the administration of any category; even those created by others.
         * @type {string}
         */
        AccessConstants.CATEGORIES_ADMIN = "adminCategories";
        /**
         * Allows creating and editing new categories.
         * @type {string}
         */
        AccessConstants.CATEGORIES_EDIT = "editCategories";
        /**
         * Allows access to data sources.
         * @type {string}
         */
        AccessConstants.DATASOURCE_ACCESS = "accessDatasources";
        /**
         * Allows creating and editing new data sources.
         * @type {string}
         */
        AccessConstants.DATASOURCE_EDIT = "editDatasources";
        /**
         * Allows access to feeds.
         * @type {string}
         */
        AccessConstants.FEEDS_ACCESS = "accessFeeds";
        /**
         * Allows the administration of any feed; even those created by others.
         * @type {string}
         */
        AccessConstants.FEEDS_ADMIN = "adminFeeds";
        /**
         * Allows creating and editing new feeds.
         * @type {string}
         */
        AccessConstants.FEEDS_EDIT = "editFeeds";
        /**
         * Allows exporting feeds definitions.
         * @type {string}
         */
        AccessConstants.FEEDS_EXPORT = "exportFeeds";
        /**
         * Allows importing of previously exported feeds.
         * @type {string}
         */
        AccessConstants.FEEDS_IMPORT = "importFeeds";
        /**
         * Allows access to feeds and feed-related functions.
         * @type {string}
         */
        AccessConstants.FEED_MANAGER_ACCESS = "accessFeedsSupport";
        /**
         * Allows the ability to view existing groups.
         * @type {string}
         */
        AccessConstants.GROUP_ACCESS = "accessGroups";
        /**
         * Allows the ability to create and manage groups.
         * @type {string}
         */
        AccessConstants.GROUP_ADMIN = "adminGroups";
        /**
         * Allows access to Tables page
         * @type {string}
         */
        AccessConstants.TABLES_ACCESS = "accessTables";
        /**
         * Allows users to access the SLA page
         * @type {string}
         */
        AccessConstants.SLA_ACCESS = "accessServiceLevelAgreements";
        /**
         * Allows users to create new Service Level agreements
         * @type {string}
         */
        AccessConstants.SLA_EDIT = "editServiceLevelAgreements";
        /**
         * Allows users to create new Service Level agreements
         * @type {string}
         */
        AccessConstants.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE = "editServiceLevelAgreementEmailTemplate";
        /**
         * Allows access to feed templates.
         * @type {string}
         */
        AccessConstants.TEMPLATES_ACCESS = "accessTemplates";
        /**
         * Allows the administration of any feed template; even those created by others.
         * @type {string}
         */
        AccessConstants.TEMPLATES_ADMIN = "adminTemplates";
        /**
         * Allows created and editing new feed templates.
         * @type {string}
         */
        AccessConstants.TEMPLATES_EDIT = "editTemplates";
        /**
         * Allows exporting template definitions.
         * @type {string}
         */
        AccessConstants.TEMPLATES_EXPORT = "exportTemplates";
        /**
         * Allows importing of previously exported templates.
         * @type {string}
         */
        AccessConstants.TEMPLATES_IMPORT = "importTemplates";
        /**
         * Allows the ability to view existing users.
         * @type {string}
         */
        AccessConstants.USERS_ACCESS = "accessUsers";
        /**
         * Allows the ability to create and manage users.
         * @type {string}
         */
        AccessConstants.USERS_ADMIN = "adminUsers";
        /**
         * Allows access to user and group-related functions.
         * @type {string}TEMPLATES_IMPORT
         */
        AccessConstants.USERS_GROUPS_ACCESS = "accessUsersGroupsSupport";
        /**
         * allows access to the visual query link on the left
         * @type {string}
         */
        AccessConstants.VISUAL_QUERY_ACCESS = "accessVisualQuery";
        /**
         * Access Search
         * @type {string}
         */
        AccessConstants.SEARCH_ACCESS = "searchAccess";
        /**
         * Allows administration of operations; such as stopping and abandoning them.
         * @type {string}
         */
        AccessConstants.OPERATIONS_ADMIN = "adminOperations";
        /**
         * Allows access to operational information like active feeds and execution history; etc.
         * @type {string}
         */
        AccessConstants.OPERATIONS_MANAGER_ACCESS = "accessOperations";
        /**
         * access to ops manager alerts
         * @type {string}
         */
        AccessConstants.ALERTS_ACCESS = "accessAlerts";
        /**
         * Access to ops manager feed details
         * @type {string}
         */
        AccessConstants.FEED_OPERATIONS_DETAIL_ACCESS = "accessOperationsFeedDetails";
        /**
         * Access to ops manager jobs
         * @type {string}
         */
        AccessConstants.JOBS_ACCESS = "accessJobs";
        /**
         * Access to ops manager job details
         * @type {string}
         */
        AccessConstants.JOB_DETAILS_ACCESS = "accessJobDetails";
        /**
         * Access Services
         * @type {string}
         */
        AccessConstants.SERVICES_ACCESS = "accessServices";
        /**
         * Access to ops manager charts
         * @type {string}
         */
        AccessConstants.CHARTS_ACCESS = "accessCharts";
        /**
         * Allows access to search all indexed columns
         * @type {string}
         */
        AccessConstants.GLOBAL_SEARCH_ACCESS = "accessSearch";
        AccessConstants.ADMIN_METADATA = "adminMetadata";
        AccessConstants.ENTITY_ACCESS = {
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
                EXPORT: "exportFeed",
                START: "startFeed"
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
        AccessConstants.UI_STATES = {
            FEEDS: { state: "feeds", permissions: [AccessConstants.FEEDS_ACCESS] },
            DEFINE_FEED: { state: "define-feed", permissions: [AccessConstants.FEEDS_EDIT] },
            DEFINE_FEED_COMPLETE: { state: "define-feed-complete", permissions: [AccessConstants.FEEDS_ACCESS] },
            IMPORT_FEED: { state: "import-feed", permissions: [AccessConstants.FEEDS_IMPORT] },
            FEED_DETAILS: { state: "feed-details", permissions: [AccessConstants.FEEDS_ACCESS] },
            EDIT_FEED: { state: "edit-feed", permissions: [AccessConstants.FEEDS_ACCESS] },
            CATEGORIES: { state: "categories", permissions: [AccessConstants.CATEGORIES_ACCESS] },
            CATEGORY_DETAILS: { state: "category-details", permissions: [AccessConstants.CATEGORIES_ACCESS] },
            BUSINESS_METADATA: { state: "business-metadata", permissions: [AccessConstants.CATEGORIES_ADMIN] },
            USERS: { state: "users", permissions: [AccessConstants.USERS_ACCESS] },
            USERS_DETAILS: { state: "user-details", permissions: [AccessConstants.USERS_ACCESS] },
            GROUPS: { state: "groups", permissions: [AccessConstants.USERS_GROUPS_ACCESS] },
            GROUP_DETAILS: { state: "group-details", permissions: [AccessConstants.USERS_GROUPS_ACCESS] },
            VISUAL_QUERY: { state: "visual-query", permissions: [AccessConstants.VISUAL_QUERY_ACCESS] },
            SERVICE_LEVEL_AGREEMENTS: { state: "service-level-agreements", permissions: [AccessConstants.SLA_ACCESS] },
            SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES: { state: "sla-email-templates", permissions: [AccessConstants.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE] },
            CATALOG: { state: "catalog", permissions: [AccessConstants.TABLES_ACCESS] },
            SCHEMAS: { state: "schemas", permissions: [AccessConstants.TABLES_ACCESS] },
            TABLES: { state: "schemas-schema", permissions: [AccessConstants.TABLES_ACCESS] },
            TABLE: { state: "schemas-schema-table", permissions: [AccessConstants.TABLES_ACCESS] },
            DATASOURCES: { state: "datasources", permissions: [AccessConstants.DATASOURCE_ACCESS] },
            DATASOURCE_DETAILS: { state: "datasource-details", permissions: [AccessConstants.DATASOURCE_ACCESS] },
            REGISTERED_TEMPLATES: { state: "registered-templates", permissions: [AccessConstants.TEMPLATES_ACCESS] },
            REGISTER_NEW_TEMPLATE: { state: "register-new-template", permissions: [AccessConstants.TEMPLATES_EDIT] },
            REGISTER_TEMPLATE: { state: "register-template", permissions: [AccessConstants.TEMPLATES_EDIT] },
            REGISTER_TEMPLATE_COMPLETE: { state: "register-template-complete", permissions: [AccessConstants.TEMPLATES_EDIT] },
            IMPORT_TEMPLATE: { state: "import-template", permissions: [AccessConstants.TEMPLATES_IMPORT] },
            SEARCH: { state: "search", permissions: AccessConstants.searchpermissions },
            DOMAIN_TYPES: { state: "domain-types", permissions: [AccessConstants.FEEDS_ADMIN] },
            DOMAIN_TYPE_DETAILS: { state: "domain-type-details", permissions: [AccessConstants.FEEDS_ADMIN] },
            JCR_ADMIN: { state: "jcr-query", permissions: [AccessConstants.ADMIN_METADATA] },
            //Ops Manager
            ALERTS: { state: "alerts", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            ALERT_DETAILS: { state: "alert-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            CHARTS: { state: "charts", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            OPS_FEED_DETAILS: { state: "ops-feed-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            FEED_STATS: { state: "feed-stats", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            JOBS: { state: "jobs", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            JOB_DETAILS: { state: "job-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            DASHBOARD: { state: "dashboard", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            SCHEDULER: { state: "scheduler", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            SERVICE_HEALTH: { state: "service-health", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            SERVICE_DETAILS: { state: "service-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            SERVICE_COMPONENT_DETAILS: { state: "service-component-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            SERVICE_LEVEL_ASSESSMENTS: { state: "service-level-assessments", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] },
            SERVICE_LEVEL_ASSESSMENT: { state: "service-level-assessment", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS] }
        };
        return AccessConstants;
    }());
    exports.default = AccessConstants;
});
//# sourceMappingURL=AccessConstants.js.map
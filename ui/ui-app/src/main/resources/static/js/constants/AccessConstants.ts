export default class AccessConstants {

    constructor() {
    }

    /**
     * Allows access to categories.
     * @type {string}
     */
    public static CATEGORIES_ACCESS: string = "accessCategories";

    /**
     * Allows the administration of any category; even those created by others.
     * @type {string}
     */
    public static CATEGORIES_ADMIN: string = "adminCategories";

    /**
     * Allows creating and editing new categories.
     * @type {string}
     */
    public static CATEGORIES_EDIT: string = "editCategories";


    /**
     * Allows access to data sources.
     * @type {string}
     */
    public static DATASOURCE_ACCESS: string = "accessDatasources";

    /**
     * Allows getting data source details with sensitive info
     */
    public static DATASOURCE_ADMIN = "adminDatasources";

    /**
     * Allows creating and editing new data sources.
     * @type {string}
     */
    public static DATASOURCE_EDIT: string = "editDatasources";

    /**
     * Allows access to feeds.
     * @type {string}
     */
    public static FEEDS_ACCESS: string = "accessFeeds";

    /**
     * Allows the administration of any feed; even those created by others.
     * @type {string}
     */
    public static FEEDS_ADMIN: string = "adminFeeds";

    /**
     * Allows creating and editing new feeds.
     * @type {string}
     */
    public static FEEDS_EDIT: string = "editFeeds";

    /**
     * Allows exporting feeds definitions.
     * @type {string}
     */
    public static FEEDS_EXPORT: string = "exportFeeds";

    /**
     * Allows importing of previously exported feeds.
     * @type {string}
     */
    public static FEEDS_IMPORT: string = "importFeeds";

    /**
     * Allows access to feeds and feed-related functions.
     * @type {string}
     */
    public static FEED_MANAGER_ACCESS: string = "accessFeedsSupport";

    /**
     * Allows the ability to view existing groups.
     * @type {string}
     */
    public static GROUP_ACCESS: string = "accessGroups";

    /**
     * Allows the ability to create and manage groups.
     * @type {string}
     */
    public static GROUP_ADMIN: string = "adminGroups";

    /**
     * Allows access to Tables page
     * @type {string}
     */
    public static TABLES_ACCESS: string = "accessTables";

    /**
     * Allows users to access the SLA page
     * @type {string}
     */
    public static SLA_ACCESS: string = "accessServiceLevelAgreements";

    /**
     * Allows users to create new Service Level agreements
     * @type {string}
     */
    public static SLA_EDIT: string = "editServiceLevelAgreements";

    /**
     * Allows users to create new Service Level agreements
     * @type {string}
     */
    public static EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE: string = "editServiceLevelAgreementEmailTemplate";

    /**
     * Allows access to feed templates.
     * @type {string}
     */
    public static TEMPLATES_ACCESS: string = "accessTemplates";

    /**
     * Allows the administration of any feed template; even those created by others.
     * @type {string}
     */
    public static TEMPLATES_ADMIN: string = "adminTemplates";

    /**
     * Allows created and editing new feed templates.
     * @type {string}
     */
    public static TEMPLATES_EDIT: string = "editTemplates";

    /**
     * Allows exporting template definitions.
     * @type {string}
     */
    public static TEMPLATES_EXPORT: string = "exportTemplates";

    /**
     * Allows importing of previously exported templates.
     * @type {string}
     */
    public static TEMPLATES_IMPORT: string = "importTemplates";

    /**
     * Allows the ability to view existing users.
     * @type {string}
     */
    public static USERS_ACCESS: string = "accessUsers";

    /**
     * Allows the ability to create and manage users.
     * @type {string}
     */
    public static USERS_ADMIN: string = "adminUsers";

    /**
     * Allows access to user and group-related functions.
     * @type {string}TEMPLATES_IMPORT
     */
    public static USERS_GROUPS_ACCESS: string = "accessUsersGroupsSupport";

    /**
     * allows access to the visual query link on the left
     * @type {string}
     */
    public static VISUAL_QUERY_ACCESS: string = "accessVisualQuery";

    /**
     * Access Search
     * @type {string}
     */
    public static SEARCH_ACCESS: string = "searchAccess";

    /**
     * Allows administration of operations; such as stopping and abandoning them.
     * @type {string}
     */
    public static OPERATIONS_ADMIN: string = "adminOperations";

    /**
     * Allows access to operational information like active feeds and execution history; etc.
     * @type {string}
     */
    public static OPERATIONS_MANAGER_ACCESS: string = "accessOperations";

    /**
     * access to ops manager alerts
     * @type {string}
     */
    public static ALERTS_ACCESS: string = "accessAlerts";

    /**
     * Access to ops manager feed details
     * @type {string}
     */
    public static FEED_OPERATIONS_DETAIL_ACCESS: string = "accessOperationsFeedDetails";

    /**
     * Access to ops manager jobs
     * @type {string}
     */
    public static JOBS_ACCESS: string = "accessJobs";

    /**
     * Access to ops manager job details
     * @type {string}
     */
    public static JOB_DETAILS_ACCESS: string = "accessJobDetails";

    /**
     * Access Services
     * @type {string}
     */
    public static SERVICES_ACCESS: string = "accessServices";

    /**
     * Access to ops manager charts
     * @type {string}
     */
    public static CHARTS_ACCESS: string = "accessCharts";

    /**
     * Allows access to search all indexed columns
     * @type {string}
     */
    public static GLOBAL_SEARCH_ACCESS: string = "accessSearch";

    public static ADMIN_METADATA: string = "adminMetadata";

    public static ACCESS_CONNECTOR: string = "accessConnectors";

    public static ADMIN_CONNECTORS: string = "adminConnectors";

    public static ACCESS_CATALOG:string = "accessCatalog"

    public static EDIT_DATASOURCES:string = "editDatasources";

    public static ENTITY_ACCESS = {
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
            ACCESS_DATASOURCE: "accessDatasource",
            EDIT_DETAILS: "editDatasourceDetails",
            DELETE_DATASOURCE: "deleteDatasource",
            CHANGE_DATASOURCE_PERMISSIONS: "changeDatasourcePermissions"
        },
        CONNECTOR: {
            EDIT_CONNECTOR:"editConnector",
            CHANGE_CONNECTOR_PERMISSIONS:"changeConnectorPermissions",
            ACTIVATE_CONNECTOR:"activateConnector",
            CREATE_DATA_SOURCE:"createDataSource"
        }
    };

    public static searchpermissions: any[];
    public static UI_STATES = {
        FEEDS: {state: "feeds", permissions: [AccessConstants.FEEDS_ACCESS]},
        DEFINE_FEED: {state: "define-feed", permissions: [AccessConstants.FEEDS_EDIT]},
        DEFINE_FEED_COMPLETE: {state: "define-feed-complete", permissions: [AccessConstants.FEEDS_ACCESS]},
        IMPORT_FEED: {state: "import-feed", permissions: [AccessConstants.FEEDS_IMPORT]},
        FEED_DETAILS: {state: "feed-details", permissions: [AccessConstants.FEEDS_ACCESS]},
        EDIT_FEED: {state: "edit-feed", permissions: [AccessConstants.FEEDS_ACCESS]},
        CATEGORIES: {state: "categories", permissions: [AccessConstants.CATEGORIES_ACCESS]},
        CATEGORY_DETAILS: {state: "category-details", permissions: [AccessConstants.CATEGORIES_ACCESS]},
        BUSINESS_METADATA: {state: "business-metadata", permissions: [AccessConstants.CATEGORIES_ADMIN]},
        USERS: {state: "users", permissions: [AccessConstants.USERS_ACCESS]},
        USERS_DETAILS: {state: "user-details", permissions: [AccessConstants.USERS_ACCESS]},
        GROUPS: {state: "groups", permissions: [AccessConstants.USERS_GROUPS_ACCESS]},
        GROUP_DETAILS: {state: "group-details", permissions: [AccessConstants.USERS_GROUPS_ACCESS]},
        VISUAL_QUERY: {state: "visual-query", permissions: [AccessConstants.VISUAL_QUERY_ACCESS, AccessConstants.DATASOURCE_ACCESS]},
        SERVICE_LEVEL_AGREEMENTS: {state: "sla.list", permissions: [AccessConstants.SLA_ACCESS]},
        EDIT_SERVICE_LEVEL_AGREEMENTS: {state: "sla.edit", permissions: [AccessConstants.SLA_EDIT]},
        SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES: {state: "sla-email-templates", permissions: [AccessConstants.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE]},
        CATALOG: {state: "catalog", permissions: [AccessConstants.ACCESS_CATALOG]},
        SCHEMAS: {state: "schemas", permissions: [AccessConstants.TABLES_ACCESS]},
        TABLES: {state: "schemas-schema", permissions: [AccessConstants.TABLES_ACCESS]},
        TABLE: {state: "schemas-schema-table", permissions: [AccessConstants.TABLES_ACCESS]},
        DATASOURCES: {state: "datasources", permissions: [AccessConstants.DATASOURCE_ACCESS]},
        DATASOURCE_DETAILS: {state: "datasource-details", permissions: [AccessConstants.DATASOURCE_ACCESS]},
        EDIT_DATASOURCES:{state:"catalog.connectors",permissions:[AccessConstants.EDIT_DATASOURCES]},
        REGISTERED_TEMPLATES: {state: "registered-templates", permissions: [AccessConstants.TEMPLATES_ACCESS]},
        REGISTER_NEW_TEMPLATE: {state: "register-new-template", permissions: [AccessConstants.TEMPLATES_EDIT]},
        REGISTER_TEMPLATE: {state: "register-template", permissions: [AccessConstants.TEMPLATES_EDIT]},
        REGISTER_TEMPLATE_COMPLETE: {state: "register-template-complete", permissions: [AccessConstants.TEMPLATES_EDIT]},
        IMPORT_TEMPLATE: {state: "import-template", permissions: [AccessConstants.TEMPLATES_IMPORT]},
        SEARCH: {state: "search", permissions: AccessConstants.searchpermissions},//permissions: [null]},
        DOMAIN_TYPES: {state: "domain-types", permissions: [AccessConstants.FEEDS_ADMIN]},
        DOMAIN_TYPE_DETAILS: {state: "domain-type-details", permissions: [AccessConstants.FEEDS_ADMIN]},
        JCR_ADMIN: {state: "jcr-query", permissions: [AccessConstants.ADMIN_METADATA]},
        REPOSITORY: {state: "repository", permissions: [AccessConstants.TEMPLATES_IMPORT]},
        //Ops Manager
        ALERTS: {state: "alerts", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        ALERT_DETAILS: {state: "alert-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        CHARTS: {state: "charts", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        OPS_FEED_DETAILS: {state: "ops-feed-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        FEED_STATS: {state: "feed-stats", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        JOBS: {state: "jobs", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        JOB_DETAILS: {state: "job-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        DASHBOARD: {state: "dashboard", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        SCHEDULER: {state: "scheduler", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        SERVICE_HEALTH: {state: "service-health", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        SERVICE_DETAILS: {state: "service-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        SERVICE_COMPONENT_DETAILS: {state: "service-component-details", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        SERVICE_LEVEL_ASSESSMENTS: {state: "service-level-assessments", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        SERVICE_LEVEL_ASSESSMENT: {state: "service-level-assessment", permissions: [AccessConstants.OPERATIONS_MANAGER_ACCESS]},
        ADMIN_CONNECTORS:{state:"catalog.admin-connectors",permissions:[AccessConstants.ADMIN_CONNECTORS]},
        ADMIN_CONNECTOR:{state:"catalog.admin-connector",permissions:[AccessConstants.ADMIN_CONNECTORS]},
        FEED_STEP_WRANGLER:{state:"feed-definition.section.wrangler",permissions:[AccessConstants.DATASOURCE_ACCESS, AccessConstants.FEEDS_ACCESS]}
    }

    public static getStatePermissions(key:string){
       let state:any = AccessConstants.UI_STATES[key];
       if(state != undefined && state.permissions){
           return state.permissions;
       }
       else {
           return [];
       }
    }
}

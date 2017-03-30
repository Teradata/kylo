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
         * Allows access to SLA page
         * @type {string}
         */
        this.SLA_ACCESS = "accessServiceLevelAgreements";

        /**
         * Allows access to Tables page
         * @type {string}
         */
        this.TABLES_ACCESS = "accessTables";

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
         * @type {string}
         */
        this.USERS_GROUPS_ACCESS = "accessUsersGroupsSupport";

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
         * Access to ops manager sla scheduler
         * @type {string}
         */
        this.SLA_SCHEDULER_ACCESS = "accessSLAScheduler";




    }
    return new AccessConstants();
});
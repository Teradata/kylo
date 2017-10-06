define(['angular', "auth/module-name"], function (angular, moduleName) {

    /**
     * Identifier for this page.
     * @type {string}
     */
    var PAGE_NAME = "users";

    /**
     * Displays a list of users in a table.
     *
     * @constructor
     * @param $scope the application model
     * @param AddButtonService the Add button service
     * @param PaginationDataService the table pagination service
     * @param StateService the page state service
     * @param TableOptionsService the table options service
     * @param UserService the user service
     */
    function UsersTableController($scope, AddButtonService, PaginationDataService, StateService, TableOptionsService, UserService) {
        var self = this;

        /**
         * Page title.
         * @type {string}
         */
        self.cardTitle = "Users";

        /**
         * Index of the current page.
         * @type {number}
         */
        self.currentPage = PaginationDataService.currentPage(PAGE_NAME) || 1;

        /**
         * Helper for table filtering.
         * @type {*}
         */
        self.filter = PaginationDataService.filter(PAGE_NAME);

        /**
         * Mapping of group names to group metadata.
         * @type {Object.<string, GroupPrincipal>}
         */
        self.groups = {};

        /**
         * Indicates that the table data is being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Identifier for this page.
         * @type {string}
         */
        self.pageName = PAGE_NAME;

        /**
         * Helper for table pagination.
         * @type {*}
         */
        self.paginationData = (function() {
            var paginationData = PaginationDataService.paginationData(PAGE_NAME);
            PaginationDataService.setRowsPerPageOptions(PAGE_NAME, ['5', '10', '20', '50']);
            return paginationData;
        })();

        /**
         * Options for sorting the table.
         * @type {*}
         */
        self.sortOptions = (function() {
            var fields = {"Display Name": "displayName", "Email Address": "email", "State": "enabled", "Groups": "groups"};
            var sortOptions = TableOptionsService.newSortOptions(PAGE_NAME, fields, "displayName", "asc");
            var currentOption = TableOptionsService.getCurrentSort(PAGE_NAME);
            if (currentOption) {
                TableOptionsService.saveSortOption(PAGE_NAME, currentOption)
            }
            return sortOptions;
        })();

        /**
         * List of users.
         * @type {Array.<UserPrincipal>}
         */
        self.users = [];

        /**
         * Type of view for the table.
         * @type {any}
         */
        self.viewType = PaginationDataService.viewType(PAGE_NAME);

        /**
         * Gets the display name of the specified user. Defaults to the system name if the display name is blank.
         *
         * @param user the user
         * @returns {string} the display name
         */
        self.getDisplayName = function(user) {
            return (angular.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
        };

        /**
         * Gets the title for each group the user belongs to.
         *
         * @param user the user
         * @returns {Array.<string>} the group titles
         */
        self.getGroupTitles = function(user) {
            return _.map(user.groups, function(group) {
                if (angular.isDefined(self.groups[group]) && angular.isString(self.groups[group].title)) {
                    return self.groups[group].title;
                } else {
                    return group;
                }
            });
        };

        /**
         * Updates the order of the table.
         *
         * @param order the sort order
         */
        self.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
        };

        /**
         * Updates the pagination of the table.
         *
         * @param page the page number
         */
        self.onPaginationChange = function(page) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Updates the order of the table.
         *
         * @param option the sort order
         */
        self.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName, sortString);
            TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
        };

        /**
         * Navigates to the details page for the specified user.
         *
         * @param user the user
         */
        self.userDetails = function(user) {
            StateService.Auth().navigateToUserDetails(user.systemName);
        };

        // Notify pagination service of changes to view type
        $scope.$watch(function() {
            return self.viewType;
        }, function(viewType) {
            PaginationDataService.viewType(PAGE_NAME, viewType);
        });

        // Register Add button
        AddButtonService.registerAddButton('users', function() {
            StateService.Auth().navigateToUserDetails();
        });

        // Get the list of users and groups
        UserService.getGroups().then(function(groups) {
            self.groups = {};
            angular.forEach(groups, function(group) {
                self.groups[group.systemName] = group;
            });
        });
        UserService.getUsers().then(function(users) {
            self.users = users;
            self.loading = false;
        });
    }

    angular.module(moduleName).controller("UsersTableController", ["$scope","AddButtonService","PaginationDataService","StateService","TableOptionsService","UserService",UsersTableController]);
});

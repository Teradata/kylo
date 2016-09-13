(function() {
    /**
     * Identifier for this page.
     * @type {string}
     */
    var PAGE_NAME = "groups";

    /**
     * Displays a list of groups in a table.
     *
     * @constructor
     * @param $scope the application model
     * @param AddButtonService the Add button service
     * @param PaginationDataService the table pagination service
     * @param StateService the page state service
     * @param TableOptionsService the table options service
     * @param UserService the user service
     */
    function GroupsTableController($scope, AddButtonService, PaginationDataService, StateService, TableOptionsService, UserService) {
        var self = this;

        /**
         * Page title.
         * @type {string}
         */
        self.cardTitle = "Groups";

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
         * List of groups.
         * @type {Array.<GroupPrincipal>}
         */
        self.groups = [];

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
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', 'All']);
            return paginationData;
        })();

        /**
         * Options for sorting the table.
         * @type {*}
         */
        self.sortOptions = (function() {
            var fields = {"Title": "title", "Description": "description", "Members": "memberCount"};
            var sortOptions = TableOptionsService.newSortOptions(PAGE_NAME, fields, "title", "asc");
            var currentOption = TableOptionsService.getCurrentSort(PAGE_NAME);
            if (currentOption) {
                TableOptionsService.saveSortOption(PAGE_NAME, currentOption)
            }
            return sortOptions;
        })();

        /**
         * Type of view for the table.
         * @type {any}
         */
        self.viewType = PaginationDataService.viewType(PAGE_NAME);

        /**
         * Navigates to the details page for the specified group.
         *
         * @param group the group
         */
        self.groupDetails = function(group) {
            StateService.navigateToGroupDetails(group.systemName);
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

        // Notify pagination service of changes to view type
        $scope.$watch(function() {
            return self.viewType;
        }, function(viewType) {
            PaginationDataService.viewType(PAGE_NAME, viewType);
        });

        // Register Add button
        AddButtonService.registerAddButton('groups', function() {
            StateService.navigateToGroupDetails();
        });

        // Get the list of groups
        UserService.getGroups()
                .then(function(groups) {
                    self.groups = groups;
                    self.loading = false;
                });
    }

    angular.module(MODULE_FEED_MGR).controller("GroupsTableController", GroupsTableController);
}());

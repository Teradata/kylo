define(["require", "exports", "angular", "../module-name", "../module", "../module-require"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Identifier for this page.
     * @type {string}
     */
    var PAGE_NAME = "groups";
    var GroupsTableController = /** @class */ (function () {
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
            var _this = this;
            this.$scope = $scope;
            this.AddButtonService = AddButtonService;
            this.PaginationDataService = PaginationDataService;
            this.StateService = StateService;
            this.TableOptionsService = TableOptionsService;
            this.UserService = UserService;
            /**
             * Page title.
             * @type {string}
             */
            this.cardTitle = "Groups";
            /**
             * Index of the current page.
             * @type {number}
             */
            this.currentPage = this.PaginationDataService.currentPage(PAGE_NAME) || 1;
            /**
             * Helper for table filtering.
             * @type {*}
             */
            this.filter = this.PaginationDataService.filter(PAGE_NAME);
            /**
             * Indicates that the table data is being loaded.
             * @type {boolean}
             */
            this.loading = true;
            /**
             * Identifier for this page.
             * @type {string}
             */
            this.pageName = PAGE_NAME;
            /**
             * Helper for table pagination.
             * @type {*}
             */
            this.paginationData = this.getPaginatedData();
            this.sortOptions = this.getSortOptions();
            /**
             * List of groups.
             * @type {Array.<GroupPrincipal>}
             */
            this.groups = [];
            // options:any = this.TableOptionsService.currentOption;
            /**
             * Type of view for the table
             * @type {any}
             */
            this.viewType = this.PaginationDataService.viewType(PAGE_NAME);
            /**
             * Updates the order of the table.
             * @param option the sort order
             */
            this.selectedTableOption = function (option) {
                var sortString = _this.TableOptionsService.toSortString(option);
                _this.PaginationDataService.sort(_this.pageName, sortString);
                _this.TableOptionsService.toggleSort(_this.pageName, option);
                _this.TableOptionsService.setSortOption(_this.pageName, sortString);
            };
            /**
            * Updates the order of the table.
            * @param order the sort order
            */
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
            };
            /**
             * Updates the pagination of the table.
             * @param page the page number
             */
            this.onPaginationChange = function (page) {
                _this.PaginationDataService.currentPage(_this.pageName, null, page);
                _this.currentPage = page;
            };
            /**
             * Gets the title of the specified group. Defaults to the system name if the title is blank.
             * @param group the group
             * @returns {string} the title
             */
            this.getTitle = function (group) {
                return (angular.isString(group.title) && group.title.length > 0) ? group.title : group.systemName;
            };
            /**
            * Navigates to the details page for the specified user.
            *
            * @param user the user
            */
            this.groupDetails = function (group) {
                _this.StateService.Auth().navigateToGroupDetails(group.systemName);
            };
            // Notify pagination service of changes to view type
            this.$scope.$watch(function () {
                return _this.viewType;
            }, function (viewType) {
                PaginationDataService.viewType(PAGE_NAME, viewType);
            });
            // Register Add button
            AddButtonService.registerAddButton('groups', function () {
                StateService.Auth().navigateToGroupDetails();
            });
            // Get the list of users and groups
            UserService.getGroups().then(function (groups) {
                _this.groups = groups;
                _this.loading = false;
            });
        }
        GroupsTableController.prototype.getPaginatedData = function () {
            var paginationData = this.PaginationDataService.paginationData(PAGE_NAME);
            this.PaginationDataService.setRowsPerPageOptions(PAGE_NAME, ['5', '10', '20', '50']);
            return paginationData;
        };
        GroupsTableController.prototype.getSortOptions = function () {
            var fields = { "Title": "title", "Description": "description", "Members": "memberCount" };
            var sortOptions = this.TableOptionsService.newSortOptions(PAGE_NAME, fields, "title", "asc");
            var currentOption = this.TableOptionsService.getCurrentSort(PAGE_NAME);
            if (currentOption) {
                this.TableOptionsService.saveSortOption(PAGE_NAME, currentOption);
            }
            return sortOptions;
        };
        /**
         * Gets the display name of the specified group. Defaults to the system name if the display name is blank.
         * @param group the group
         * @returns {string} the display name
         */
        GroupsTableController.prototype.getDisplayName = function (group) {
            return (angular.isString(group.title) && group.title.length > 0) ? group.title : group.systemName;
        };
        ;
        //$inject = ["$scope","AddButtonService","PaginationDataService","StateService","TableOptionsService","UserService"];
        GroupsTableController.$inject = ["$scope", "AddButtonService", "PaginationDataService", "StateService", "TableOptionsService", "UserService"];
        return GroupsTableController;
    }());
    exports.default = GroupsTableController;
    angular.module(module_name_1.moduleName)
        .component("groupsTableController", {
        controller: GroupsTableController,
        controllerAs: "vm",
        templateUrl: "js/auth/groups/groups-table.html"
    });
});
//.controller("GroupsTableController", GroupsTableController]); 
//# sourceMappingURL=GroupsTableController.js.map
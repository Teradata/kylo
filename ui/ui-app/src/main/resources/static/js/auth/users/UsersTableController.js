define(["require", "exports", "angular", "underscore", "../module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var PAGE_NAME = "users";
    //const moduleName = require('auth/module-name');
    var UsersTableController = /** @class */ (function () {
        function UsersTableController($scope, AddButtonService, PaginationDataService, StateService, TableOptionsService, UserService //UserService
        ) {
            var _this = this;
            this.$scope = $scope;
            this.AddButtonService = AddButtonService;
            this.PaginationDataService = PaginationDataService;
            this.StateService = StateService;
            this.TableOptionsService = TableOptionsService;
            this.UserService = UserService; //UserService
            /**
             * Page title.
             * @type {string}
             */
            this.cardTitle = "Users";
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
             * Mapping of group names to group metadata.
             * @type {Object.<string, GroupPrincipal>}
             */
            this.groups = {};
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
             * List of users.
             * @type {Array.<UserPrincipal>}
             */
            this.users = [];
            /**
             * Type of view for the table.
             * @type {any}
             */
            this.viewType = this.PaginationDataService.viewType(PAGE_NAME);
            /**
             * Updates the order of the table.
             *
             * @param order the sort order
             */
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
            };
            /**
             * Updates the pagination of the table.
             *
             * @param page the page number
             */
            this.onPaginationChange = function (page) {
                _this.PaginationDataService.currentPage(_this.pageName, null, page);
                _this.currentPage = page;
            };
            /**
             * Updates the order of the table.
             *
             * @param option the sort order
             */
            this.selectedTableOption = function (option) {
                var sortString = _this.TableOptionsService.toSortString(option);
                _this.PaginationDataService.sort(_this.pageName, sortString);
                _this.TableOptionsService.toggleSort(_this.pageName, option);
                _this.TableOptionsService.setSortOption(_this.pageName, sortString);
            };
            // Notify pagination service of changes to view type
            this.$scope.$watch(function () {
                return _this.viewType;
            }, function (viewType) {
                _this.PaginationDataService.viewType(PAGE_NAME, viewType);
            });
            // Register Add button
            this.AddButtonService.registerAddButton('users', function () {
                _this.StateService.Auth().navigateToUserDetails();
            });
            // Get the list of users and groups
            this.UserService.getGroups().then(function (groups) {
                _this.groups = {};
                angular.forEach(groups, function (group) {
                    _this.groups[group.systemName] = group;
                });
            });
            this.UserService.getUsers().then(function (users) {
                _this.users = users;
                _this.loading = false;
            });
        }
        UsersTableController.prototype.getPaginatedData = function () {
            var paginationData = this.PaginationDataService.paginationData(PAGE_NAME);
            this.PaginationDataService.setRowsPerPageOptions(PAGE_NAME, ['5', '10', '20', '50']);
            return paginationData;
        };
        UsersTableController.prototype.getSortOptions = function () {
            var fields = { "Display Name": "displayName", "Email Address": "email", "State": "enabled", "Groups": "groups" };
            var sortOptions = this.TableOptionsService.newSortOptions(PAGE_NAME, fields, "displayName", "asc");
            var currentOption = this.TableOptionsService.getCurrentSort(PAGE_NAME);
            if (currentOption) {
                this.TableOptionsService.saveSortOption(PAGE_NAME, currentOption);
            }
            return sortOptions;
        };
        /**
         * Gets the display name of the specified user. Defaults to the system name if the display name is blank.
         *
         * @param user the user
         * @returns {string} the display name
         */
        UsersTableController.prototype.getDisplayName = function (user) {
            return (angular.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
        };
        ;
        /**
         * Gets the title for each group the user belongs to.
         *
         * @param user the user
         * @returns {Array.<string>} the group titles
         */
        UsersTableController.prototype.getGroupTitles = function (user) {
            var _this = this;
            return _.map(user.groups, function (group) {
                if (angular.isDefined(_this.groups[group]) && angular.isString(_this.groups[group].title)) {
                    return _this.groups[group].title;
                }
                else {
                    return group;
                }
            });
        };
        ;
        /**
         * Navigates to the details page for the specified user.
         *
         * @param user the user
         */
        UsersTableController.prototype.userDetails = function (user) {
            this.StateService.Auth().navigateToUserDetails(user.systemName);
        };
        ;
        return UsersTableController;
    }());
    exports.default = UsersTableController;
    angular.module(module_name_1.moduleName)
        .controller("UsersTableController", ["$scope",
        "AddButtonService",
        "PaginationDataService",
        "StateService",
        "TableOptionsService",
        "UserService",
        UsersTableController]);
});
//# sourceMappingURL=UsersTableController.js.map
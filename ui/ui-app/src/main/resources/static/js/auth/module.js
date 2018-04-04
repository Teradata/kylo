define(["require", "exports", "angular", "../constants/AccessConstants", "../kylo-utils/LazyLoadUtil", "./module-name"], function (require, exports, angular, AccessConstants_1, LazyLoadUtil_1, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.config(['$stateProvider', this.configFn.bind(this)]);
        }
        ModuleFactory.prototype.configFn = function ($stateProvider) {
            $stateProvider.state(AccessConstants_1.default.UI_STATES.USERS.state, {
                url: '/users',
                params: {},
                views: {
                    'content': {
                        // templateUrl: 'js/auth/users/users-table.html',
                        component: "usersTableController",
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['auth/users/UsersTableController'])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: 'Users',
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.USERS.permissions
                }
            });
            $stateProvider.state(AccessConstants_1.default.UI_STATES.USERS_DETAILS.state, {
                url: "/user-details/{userId}",
                params: {
                    userId: null
                },
                views: {
                    'content': {
                        // templateUrl: 'js/auth/users/user-details/user-details.html',
                        component: "userDetailsController",
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['auth/users/user-details/UserDetailsController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: "User Details",
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.USERS_DETAILS.permissions
                }
            });
            $stateProvider.state(AccessConstants_1.default.UI_STATES.GROUPS.state, {
                url: "/groups",
                params: {},
                views: {
                    'content': {
                        //  templateUrl: 'js/auth/groups/groups-table.html',
                        component: "groupsTableController",
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['auth/groups/GroupsTableController'])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: "Groups",
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.GROUPS.permissions
                }
            });
            $stateProvider.state(AccessConstants_1.default.UI_STATES.GROUP_DETAILS.state, {
                url: "/group-details/{groupId}",
                params: {
                    groupId: null
                },
                views: {
                    'content': {
                        //  templateUrl: 'js/auth/groups/group-details/group-details.html',
                        component: "groupDetailsController",
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(['auth/groups/group-details/GroupDetailsController'])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: "Group Details",
                    module: module_name_1.moduleName,
                    permissions: AccessConstants_1.default.UI_STATES.GROUP_DETAILS.permissions
                }
            });
        };
        ModuleFactory.prototype.lazyLoadController = function (path) {
            return LazyLoadUtil_1.default.lazyLoadController(path, "auth/module-require");
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map
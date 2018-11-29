import * as angular from 'angular';
import AccessConstants from "../constants/AccessConstants";
import lazyLoadUtil from "../kylo-utils/LazyLoadUtil";
import UserDetailsController from "./users/user-details/UserDetailsController";
import GroupDetailsController from "./groups/group-details/GroupDetailsController";
import {moduleName} from "./module-name";
import {StateProvider} from "@uirouter/angularjs";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$stateProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
    }
    configFn($stateProvider:StateProvider) {
        $stateProvider.state(AccessConstants.UI_STATES.USERS.state, {
            url: '/users',
            params: {},
            views: {
                'content': {
                   // templateUrl: './users/users-table.html',
                    component: "usersTableController",
                   // controllerAs: "vm"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['auth/users/UsersTableController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "users.table.controller" */ './users/UsersTableController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load UsersTableController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: true,
                displayName: 'Users',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.USERS.permissions
            }
        });
        $stateProvider.state(AccessConstants.UI_STATES.USERS_DETAILS.state, {
            url: "/user-details/{userId}",
            params: {
                userId: null
            },
            views: {
                'content': {
                   // templateUrl: './users/user-details/user-details.html',
                    component: "userDetailsController",
                   // controllerAs: "vm"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['auth/users/user-details/UserDetailsController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "user.details.controller" */ './users/user-details/UserDetailsController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load UserDetailsController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: "User Details",
                module:moduleName,
                permissions:AccessConstants.UI_STATES.USERS_DETAILS.permissions
            }
        });
        $stateProvider.state(AccessConstants.UI_STATES.GROUPS.state, {
            url: "/groups",
            params: {},
            views: {
                'content': {
                  //  templateUrl: './groups/groups-table.html',
                    component: "groupsTableController",
                   // controllerAs: "vm"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['auth/groups/GroupsTableController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "groups.table.controller" */ './groups/GroupsTableController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load GroupsTableController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: true,
                displayName: "Groups",
                module:moduleName,
                permissions:AccessConstants.UI_STATES.GROUPS.permissions
            }
        });
        $stateProvider.state(AccessConstants.UI_STATES.GROUP_DETAILS.state, {
            url: "/group-details/{groupId}",
            params: {
                groupId: null
            },
            views: {
                'content': {
                  //  templateUrl: './groups/group-details/group-details.html',
                    component: "groupDetailsController",
                   // controllerAs: "vm"
                }
            },
            resolve: {
                // loadMyCtrl:this.lazyLoadController(['auth/groups/group-details/GroupDetailsController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "groups.details.controller" */ './groups/group-details/GroupDetailsController')
                        .then(mod => {

                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load GroupDetailsController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: "Group Details",
                module:moduleName,
                permissions:AccessConstants.UI_STATES.GROUP_DETAILS.permissions
            }
        });
    }

    runFn($ocLazyLoad: any){
        return import(/* webpackChunkName: "users.module-require" */ "./module-require")
            .then(mod => {
                $ocLazyLoad.load({name:moduleName});
            })
            .catch(err => {
                throw new Error("Failed to load users.module-require, " + err);
            });
    }
}

const module = new ModuleFactory();
export default module;



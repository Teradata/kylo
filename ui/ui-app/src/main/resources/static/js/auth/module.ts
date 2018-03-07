import * as angular from 'angular';
import AccessConstants from "../constants/AccessConstants";
import lazyLoadUtil from "../kylo-utils/LazyLoadUtil";
//const lazyLoadUtil = require('../kylo-utils/LazyLoadUtil');
import UserDetailsController from "./users/user-details/UserDetailsController";
import GroupDetailsController from "./groups/group-details/GroupDetailsController";
import * as moduleRequire  from "./module-require";
import {moduleName} from "./module-name";
//const moduleName = require('auth/module-name');

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$stateProvider',this.configFn.bind(this)]);
    }
    configFn($stateProvider:any) {
            
        $stateProvider.state(AccessConstants.UI_STATES.USERS.state, {
            url: '/users',
            params: {},
            views: {
                'content': {
                    templateUrl: 'js/auth/users/users-table.html',
                    controller: "UsersTableController",
                    controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['auth/users/UsersTableController'])
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
                    templateUrl: 'js/auth/users/user-details/user-details.html',
                    controller: "UserDetailsController",
                    controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['auth/users/user-details/UserDetailsController'])
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
                    templateUrl: 'js/auth/groups/groups-table.html',
                    controller: "GroupsTableController",
                    controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['auth/groups/GroupsTableController'])
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
                    templateUrl: 'js/auth/groups/group-details/group-details.html',
                    controller: "GroupDetailsController",
                    controllerAs: "vm"
                }
            },
            resolve: {
                loadMyCtrl:this.lazyLoadController(['auth/groups/group-details/GroupDetailsController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: "Group Details",
                module:moduleName,
                permissions:AccessConstants.UI_STATES.GROUP_DETAILS.permissions
            }
        });
    }  

    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,"auth/module-require");
    }

    
} 

const module = new ModuleFactory();
export default module;



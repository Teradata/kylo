define(['angular','auth/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants', 'kylo-services'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {

    var module = angular.module(moduleName, []);

    module.config(['$stateProvider',function ($stateProvider) {
        $stateProvider.state('users', {
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
                loadMyCtrl: lazyLoadController(['auth/users/UsersTableController'])
            },
            data: {
                breadcrumbRoot: true,
                displayName: 'Users',
                module:moduleName,
                permissions:AccessConstants.USERS_ACCESS
            }
        });

        $stateProvider.state("user-details", {
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
                loadMyCtrl: lazyLoadController(['auth/users/user-details/UserDetailsController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: "User Details",
                module:moduleName,
                permissions:AccessConstants.USERS_ACCESS
            }
        }).state("groups", {
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
                loadMyCtrl: lazyLoadController(['auth/groups/GroupsTableController'])
            },
            data: {
                breadcrumbRoot: true,
                displayName: "Groups",
                module:moduleName,
                permissions:AccessConstants.USERS_GROUPS_ACCESS
            }
        }).state("group-details", {
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
                loadMyCtrl: lazyLoadController(['auth/groups/group-details/GroupDetailsController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: "Group Details",
                module:moduleName,
                permissions:AccessConstants.USERS_GROUPS_ACCESS
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,'auth/module-require');
        }
    }]);







    return module;

});


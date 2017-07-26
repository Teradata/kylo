define(["angular", "feed-mgr/domain-types/module-name", "kylo-utils/LazyLoadUtil", "constants/AccessConstants", "codemirror-require/module", "kylo-feedmgr", "kylo-common", "kylo-services"],
    function (angular, moduleName, lazyLoadUtil, AccessConstants) {
        //LAZY LOADED into the application
        var module = angular.module(moduleName, []);

        module.config(["$stateProvider", "$compileProvider", function ($stateProvider, $compileProvider) {
            //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);

            $stateProvider.state(AccessConstants.UI_STATES.DOMAIN_TYPES.state, {
                url: "/domain-types",
                params: {},
                views: {
                    content: {
                        templateUrl: "js/feed-mgr/domain-types/domain-types.html",
                        controller: "DomainTypesController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: lazyLoadController(["feed-mgr/domain-types/DomainTypesController"])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: "Domain Types",
                    module: moduleName,
                    permissions: AccessConstants.UI_STATES.DOMAIN_TYPES.permissions
                }
            }).state(AccessConstants.UI_STATES.DOMAIN_TYPE_DETAILS.state, {
                url: "/domain-type-details/{domainTypeId}",
                params: {
                    domainTypeId: null
                },
                views: {
                    content: {
                        templateUrl: "js/feed-mgr/domain-types/domain-type-details.html",
                        controller: "DomainTypeDetailsController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: lazyLoadController(["feed-mgr/domain-types/DomainTypeDetailsController"])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: "Domain Type Details",
                    module: moduleName,
                    permissions: AccessConstants.UI_STATES.DOMAIN_TYPE_DETAILS.permissions
                }
            });

        }]);

        module.run(['$ocLazyLoad', function ($ocLazyLoad) {
            $ocLazyLoad.load({
                name: 'kylo',
                files: [
                    "js/feed-mgr/domain-types/codemirror-regex.css",
                    "js/feed-mgr/domain-types/domain-types.css"
                ]
            });
        }]);

        function lazyLoadController(path) {
            return lazyLoadUtil.lazyLoadController(path, "feed-mgr/domain-types/module-require");
        }
    });

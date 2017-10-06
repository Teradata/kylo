define(['angular','feed-mgr/feeds/define-feed/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','feed-mgr/feeds/module','@uirouter/angularjs','kylo-feedmgr','feed-mgr/visual-query/module'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, []);
    module.config(["$compileProvider",function($compileProvider) {
        $compileProvider.preAssignBindingsEnabled(true);
    }]);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.DEFINE_FEED.state, {
            url: '/define-feed?templateId&templateName&feedDescriptor',
            params: {
                templateId: null,
                templateName:null,
                feedDescriptor:null,
                bcExclude_cloning:null,
                bcExclude_cloneFeedName:null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/feeds/define-feed/define-feed.html',
                    controller: 'DefineFeedController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/feeds/define-feed/DefineFeedController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Define Feed',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.DEFINE_FEED.permissions
            }
        });

        $stateProvider.state(AccessConstants.UI_STATES.DEFINE_FEED_COMPLETE.state, {
            url: '/define-feed-complete',
            params: {
                templateId: null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-complete.html',
                    controller: 'DefineFeedCompleteController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/feeds/define-feed/feed-details/DefineFeedCompleteController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Define Feed',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.DEFINE_FEED_COMPLETE.permissions
            }
        });



        $stateProvider.state(AccessConstants.UI_STATES.IMPORT_FEED.state, {
            url: '/import-feed',
            params: {},
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/feeds/define-feed/import-feed.html',
                    controller: 'ImportFeedController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/feeds/define-feed/ImportFeedController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Import Feed',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.IMPORT_FEED.permissions
            }
        });

        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['feed-mgr/feeds/module-require','feed-mgr/feeds/define-feed/module-require','feed-mgr/visual-query/module-require']);
        }
    }]);




return module;



});

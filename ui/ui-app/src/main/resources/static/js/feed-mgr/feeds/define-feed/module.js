define(['angular','feed-mgr/feeds/define-feed/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','feed-mgr/feeds/module','@uirouter/angularjs','kylo-feedmgr'], function (angular,moduleName,lazyLoadUtil,AccessConstants) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, []);
    module.config(["$compileProvider",function($compileProvider) {
        $compileProvider.preAssignBindingsEnabled(true);
    }]);

    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.default.UI_STATES.DEFINE_FEED.state, {
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
                // loadMyCtrl: lazyLoadController(['feed-mgr/feeds/define-feed/DefineFeedController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad) => {
                    return import(/* webpackChunkName: "feeds.define-feed.controller" */ 'feed-mgr/feeds/define-feed/DefineFeedController')
                        .then(mod => {
                            console.log('imported DefineFeedController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load DefineFeedController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Define Feed',
                module:moduleName,
                permissions:AccessConstants.default.UI_STATES.DEFINE_FEED.permissions
            }
        });

        $stateProvider.state(AccessConstants.default.UI_STATES.DEFINE_FEED_COMPLETE.state, {
            url: '/define-feed-complete',
            params: {
                templateId: null
            },
            views: {
                'content': {
                    component: 'thinkbigDefineFeedCompleteController',
                }
            },
            resolve: {
                // loadMyCtrl: lazyLoadController(['feed-mgr/feeds/define-feed/feed-details/DefineFeedCompleteController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad) => {
                    return import(/* webpackChunkName: "feeds.define-feed-complete.controller" */ 'feed-mgr/feeds/define-feed/feed-details/DefineFeedCompleteController')
                        .then(mod => {
                            console.log('imported DefineFeedCompleteController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load DeDefineFeedCompleteControllerfineFeedController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Define Feed',
                module:moduleName,
                permissions:AccessConstants.default.UI_STATES.DEFINE_FEED_COMPLETE.permissions
            }
        });



        $stateProvider.state(AccessConstants.default.UI_STATES.IMPORT_FEED.state, {
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
                // loadMyCtrl: lazyLoadController(['feed-mgr/feeds/define-feed/ImportFeedController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad) => {
                    return import(/* webpackChunkName: "feeds.import-feed.controller" */ 'feed-mgr/feeds/define-feed/ImportFeedController')
                        .then(mod => {
                            console.log('imported ImportFeedController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load ImportFeedController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Import Feed',
                module:moduleName,
                permissions:AccessConstants.default.UI_STATES.IMPORT_FEED.permissions
            }
        });

        function lazyLoadController(path){
            return lazyLoadUtil.default.lazyLoadController(path);
        }
    }]);




return module;



});

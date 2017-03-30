define(['angular','feed-mgr/feeds/edit-feed/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','vis','kylo-feedmgr','feed-mgr/feeds/module','feed-mgr/sla/module','feed-mgr/visual-query/module','angular-nvd3'], function (angular,moduleName,lazyLoadUtil,AccessConstants, vis) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, []);
      // load vis in the global state
        if(window.vis === undefined) {
            window.vis = vis;
        }
    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('feed-details',{
            url:'/feed-details/{feedId}',
            params: {
                feedId: null,
                tabIndex: 0
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/feeds/edit-feed/feed-details.html',
                    controller: 'FeedDetailsController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['feed-mgr/feeds/edit-feed/FeedDetailsController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Edit Feed',
                module:moduleName,
                permissions:AccessConstants.FEEDS_ACCESS
            }
        }).state('edit-feed',{
                url:'/edit-feed/{feedId}',
                params: {
                    feedId: null
                },
                views: {
                    'content': {
                        templateUrl: 'js/feed-mgr/feeds/edit-feed/edit-feed.html',
                        controller: 'EditFeedController',
                        controllerAs: 'vm'
                    }
                },
               resolve: {
                   loadMyCtrl: lazyLoadController(['feed-mgr/feeds/edit-feed/EditFeedController'])
               },
                data:{
                    breadcrumbRoot: false,
                    displayName: 'Edit Feed',
                    module:moduleName,
                    permissions:AccessConstants.FEEDS_EDIT
                }
            })

        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['feed-mgr/feeds/edit-feed/module-require','feed-mgr/sla/module-require','feed-mgr/visual-query/module-require','angular-visjs']);
        }
    }]);

    module.run(['$ocLazyLoad',function($ocLazyLoad){
        $ocLazyLoad.load({name:'kylo',files:['js/vendor/font-awesome/css/font-awesome.min.css'],serie: true})
    }]);

return module;



});
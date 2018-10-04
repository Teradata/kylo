define(['angular','feed-mgr/feeds/edit-feed/module-name','kylo-utils/LazyLoadUtil','constants/AccessConstants','vis',
        'kylo-feedmgr','feed-mgr/feeds/module','feed-mgr/sla/module','feed-mgr/feeds/define-feed/module','angular-nvd3', 'fattable'], function (angular,moduleName,lazyLoadUtil,AccessConstants, vis) {
    //LAZY LOADED into the application
    var module = angular.module(moduleName, ['nvd3']);
      // load vis in the global state
        if(window.vis === undefined) {
            window.vis = vis;
        }
    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.default.UI_STATES.FEED_DETAILS.state,{
            url:'/feed-details/{feedId}',
            params: {
                feedId: null,
                tabIndex: 0
            },
            views: {
                'content': {
                    template: require('./feed-details.html'),
                    controller: 'FeedDetailsController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                loadMyCtrl: ['$q','$ocLazyLoad', ($q, $ocLazyLoad) => {
                    return lazyLoadController($q, $ocLazyLoad);
                }]
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Edit Feed',
                module:moduleName,
                permissions:AccessConstants.default.UI_STATES.FEED_DETAILS.permissions
            }
        }).state(AccessConstants.default.UI_STATES.EDIT_FEED.state,{
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
                   // loadMyCtrl: lazyLoadController(['feed-mgr/feeds/edit-feed/EditFeedController', "feed-mgr/feeds/define-feed/module-require"])
                   loadMyCtrl: ['$q','$ocLazyLoad', ($q, $ocLazyLoad) => {
                       return lazyLoadController($q, $ocLazyLoad);
                   }]
               },
                data:{
                    breadcrumbRoot: false,
                    displayName: 'Edit Feed',
                    module:moduleName,
                    permissions:AccessConstants.default.UI_STATES.EDIT_FEED.permissions
                }
            })

        function lazyLoadController($q, $ocLazyLoad){
            $q((resolve) => {
                require.ensure([], () => {
                    let module = require('../module-require');
                    console.log('loading 1 ' , module);
                    $ocLazyLoad.load(module);
                    resolve(module);
                })
            });

            $q((resolve) => {
                require.ensure([], () => {
                    let module = require('./module-require');
                    console.log('loading 2 ' , module);
                    $ocLazyLoad.load(module);
                    resolve(module);
                })
            });

            $q((resolve) => {
                require.ensure([], () => {
                    let module = require('../../sla/module-require');
                    console.log('loading 3 ' , module);
                    $ocLazyLoad.load(module);
                    resolve(module);
                })
            });

            $q((resolve) => {
                require.ensure([], () => {
                    let module = require('angular-visjs');
                    console.log('loading 4 ' , module);
                    $ocLazyLoad.load(module);
                    resolve(module);
                })
            });

            return $q((resolve) => {
                require.ensure([], () => {
                    let module = require('./FeedDetailsController');
                    console.log('loading 5 ' , module);
                    console.log('loading 5.name' + module.name);
                    $ocLazyLoad.load(module.default);
                    resolve(module.controller);
                })
            });

            // return lazyLoadUtil.default.lazyLoadController(path,['../module-require','./module-require','../../sla/module-require','angular-visjs']);
            // return lazyLoadUtil.default.lazyLoadController(path,['feed-mgr/feeds/module-require','feed-mgr/feeds/edit-feed/module-require','feed-mgr/sla/module-require','angular-visjs']);
        }
    }]);

    module.run(['$ocLazyLoad',function ($ocLazyLoad) {
        $ocLazyLoad.load({
            name: 'kylo',
            files: [
                // 'js/feed-mgr/feeds/edit-feed/feed-details.css'
            ],
            serie: true
        });
    }]);

return module;



});

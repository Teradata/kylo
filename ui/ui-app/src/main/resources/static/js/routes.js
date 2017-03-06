define(['angular', 'kylo-common','kylo-services',
    'main/IndexController'], function (angular,app,lazyLoadUtil) {
    'use strict';
   app.config(["$ocLazyLoadProvider","$stateProvider","$urlRouterProvider",function ($ocLazyLoadProvider,$stateProvider, $urlRouterProvider) {


        $ocLazyLoadProvider.config({
            modules: ['kylo','kylo.common','kylo.services','kylo.feedmgr','kylo.feedmgr.templates','kylo.opsmgr'],
            asyncLoader: require,
            debug:false
        });

        $urlRouterProvider.otherwise("/home");

        $stateProvider
            .state('home', {
                url: '/home',
               views: {
                    "content": {
                        templateUrl: "js/main/home.html",
                        controller:'HomeController',
                        controllerAs:'vm'
                    }
                },
                resolve: { // Any property in resolve should return a promise and is executed before the view is loaded
                    loadMyCtrl: ['$ocLazyLoad', function($ocLazyLoad) {
                        // you can lazy load files for an existing module
                        return $ocLazyLoad.load('main/HomeController');
                    }]
                }
            })

       //Feed Manager
       $stateProvider.state({
           name: 'feeds.**',
           url: '/feeds',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/feeds/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('feeds')
                   return args;
               }, function error(err) {
                   console.log("Error loading feeds ",err);
                   return err;
               });;
           }
       }).state({
           name: 'define-feed.**',
           url: '/define-feed',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/feeds/define-feed/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('define-feed')
                   return args;
               }, function error(err) {
                   console.log("Error loading define-feed ",err);
                   return err;
               });;
           }
       }).state({
           name:'feed-details.**',
           url:'/feed-details/{feedId}',
           params: {
               feedId: null,
               tabIndex: 0
           },
           lazyLoad: function(transition,state) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/feeds/edit-feed/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('feed-details',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading feed-details ",err);
                   return err;
               });;
           }
       });



       $stateProvider.state({
           name: 'categories.**',
           url: '/categories',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/categories/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('categories')
                   return args;
               }, function error(err) {
                   console.log("Error loading categories ",err);
                   return err;
               });;
           }
       }).state('category-details.**',{
           url:'/category-details/{categoryId}',
           params: {
               categoryId:null
           },
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/categories/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('category-details',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading categories ",err);
                   return err;
               });;
           }
       });



       $stateProvider.state('registered-templates.**',{
           url:'/registered-templates',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/templates/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('registered-templates')
                   return args;
               }, function error(err) {
                   console.log("Error loading registered-templates ",err);
                   return err;
               });;
           }
       })

       $stateProvider.state('register-template.**',{
           url:'/register-template',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/templates/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('register-template')
                   return args;
               }, function error(err) {
                   console.log("Error loading register-template ",err);
                   return err;
               });;
           }
       })




       $stateProvider.state({
           name: 'service-level-agreements.**',
           url: '/service-level-agreements',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/sla/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('service-level-agreements')
                   return args;
               }, function error(err) {
                   console.log("Error loading service-level-agreements ",err);
                   return err;
               });;
           }
       });

       $stateProvider.state({
           name: 'tables.**',
           url: '/tables',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/tables/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('tables')
                   return args;
               }, function error(err) {
                   console.log("Error loading tables ",err);
                   return err;
               });;
           }
       });

       $stateProvider.state({
           name: 'users.**',
           url: '/users',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('auth/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('users')
                   return args;
               }, function error(err) {
                   console.log("Error loading users ",err);
                   return err;
               });;
           }
       });


       $stateProvider.state({
           name: 'groups.**',
           url: '/groups',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('auth/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('groups')
                   return args;
               }, function error(err) {
                   console.log("Error loading groups ",err);
                   return err;
               });;
           }
       });

       $stateProvider.state('search.**',{
           url:'/search',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('search/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('search')
                   return args;
               }, function error(err) {
                   console.log("Error loading search ",err);
                   return err;
               });;
           }
       });

       $stateProvider.state({
           name: 'business-metadata.**',
           url: '/business-metadata',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/business-metadata/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('business-metadata')
                   return args;
               }, function error(err) {
                   console.log("Error loading business-metadata ",err);
                   return err;
               });;
           }
       });

       $stateProvider.state({
           name: 'visual-query.**',
           url: '/visual-query',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('feed-mgr/visual-query/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('visual-query')
                   return args;
               }, function error(err) {
                   console.log("Error loading business-metadata ",err);
                   return err;
               });;
           }
       });







       //Ops Manager

       $stateProvider.state({
           name: 'dashboard.**',
           url: '/dashboard',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/overview/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('dashboard')
                   return args;
               }, function error(err) {
                   console.log("Error loading ops manager dashboard ",err);
                   return err;
               });
           }
       });

       $stateProvider.state({
           name: 'ops-feed-details.**',
           url: '/ops-feed-details/{feedName}',
           params: {
               feedName: null
           },
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/feeds/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('ops-feed-details',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading ops-feed-details ",err);
                   return err;
               });
           }
       });

       $stateProvider.state({
           name: 'feed-stats.**',
           url: '/feed-stats/{feedName}',
           params: {
               feedName: null
           },
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/feeds/feed-stats/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('feed-stats',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading feed-stats ",err);
                   return err;
               });
           }
       });

       $stateProvider.state({
           name: 'job-details.**',
           url: '/job-details/{executionId}',
           params: {
               executionId: null
           },
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/jobs/details/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('job-details',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading job-details ",err);
                   return err;
               });
           }
       });


       $stateProvider.state({
           name: 'jobs.**',
           url: '/jobs',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/jobs/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('jobs',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading jobs ",err);
                   return err;
               });
           }
       });

       $stateProvider.state({
           name: 'service-health.**',
           url: '/service-health',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/service-health/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('service-health',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading service-health ",err);
                   return err;
               });
           }
       }).state({
           name: 'service-details.**',
           url: '/service-details/{serviceName}',
           params: {
               serviceName: null
           },
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/service-health/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('service-details',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading service-details ",err);
                   return err;
               });
           }
       }).state({
           name: 'service-component-details.**',
           url: '/service-details/{serviceName}/{componentName}',
           params: {
               serviceName: null,
               componentName:null
           },
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/service-health/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('service-component-details',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading service-component-details ",err);
                   return err;
               });
           }
       })



       $stateProvider.state({
           name: 'scheduler.**',
           url: '/scheduler',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/scheduler/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('scheduler',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading scheduler ",err);
                   return err;
               });
           }
       })


       $stateProvider.state({
           name: 'alerts.**',
           url: '/alerts',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/alerts/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('alerts',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading alerts ",err);
                   return err;
               });
           }
       }).state({
           name: 'alert-details.**',
           url: '/alert-details/{alertId}',
           params: {
               alertId: null
           },
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/alerts/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('alert-details',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading alert-detailss ",err);
                   return err;
               });
           }
       });


       $stateProvider.state({
           name: 'charts.**',
           url: '/charts',
           lazyLoad: function(transition) {
               transition.injector().get('$ocLazyLoad').load('ops-mgr/charts/module').then(function success(args) {
                   //upon success go back to the state
                   $stateProvider.stateService.go('charts',transition.params())
                   return args;
               }, function error(err) {
                   console.log("Error loading charts ",err);
                   return err;
               });
           }
       });













/*
   .state('alerts', {
           url: '/alerts',
           params: {
               tab: null
           },
           views: {
               'content': {
                   templateUrl: 'js/alerts/alerts-table.html'
               }
           },
           data: {
               breadcrumbRoot: true,
               displayName: 'Alerts'
           }
       })
           .state('scheduler', {
               url:'/scheduler',
               views: {
                   'content': {
                       templateUrl: 'js/scheduler/scheduler.html'
                   }
               },
               data:{
                   breadcrumbRoot:true,
                   displayName:'Scheduler'
               }
           })
           .state('feed-stats', {
           url: '/feed-stats/:feedName',
           params: {
               executionId: null
           },
           views: {
               'content': {
                   templateUrl: 'js/feeds/feed-stats/feed-stats.html'
               }
           },
           data: {
               displayName: 'Job Details'
           }
       })

         .state('service-health',{
           url:'/service-health',
           views: {
               'content': {
                   templateUrl: 'js/service-health/service-health.html'
               }
           },
           data:{
               breadcrumbRoot:true,
               displayName:'Service Health'
           }
       }).state('service-details',{
           url:'/service-details/:serviceName',
           params: {
               serviceName: null
           },
           views: {
               'content': {
                   templateUrl: 'js/service-health/service-detail.html'
               }
           },
           data:{
               displayName:'Service Details'
           }
       }).state('service-component-details',{
           url:'/service-details/:serviceName/:componentName',
           params: {
               serviceName: null,
               componentName:null
           },
           views: {
               'content': {
                   templateUrl: 'js/service-health/service-component-detail.html'
               }
           },
           data:{
               displayName:'Service Component'
           }
       }).state('charts',{
           url:'/charts',
           views: {
               'content': {
                   templateUrl: 'js/charts/charts.html'
               }
           },
           data:{
               breadcrumbRoot:true,
               displayName:'Charts'
           }
       }).state("alert-details",{
           url:"/alert-details/:alertId",
           params: {
               alertId: null
           },
           views: {
               'content': {
                   templateUrl: 'js/alerts/alert-details.html'
               }
           },
           data:{
               displayName:'Alert Details'
           }
       })
       */
























   }]);


    app.run(
        [          '$rootScope', '$state', '$location',
                   function ($rootScope,   $state, $location) {

                       $rootScope.$state = $state;
                       //$rootScope.$transition$ = $transition$;
                       $rootScope.$location = $location;

                       $rootScope.typeOf = function(value) {
                           return typeof value;
                       };
                   }
        ]
    );
/*
    app.run(function($trace) {
        $trace.enable(1);
    });
*/




    return angular.bootstrap(document,['kylo']);


});



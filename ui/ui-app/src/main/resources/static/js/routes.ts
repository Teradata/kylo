import * as angular from 'angular';
//import app from "./app";

import {app} from './common/module-require';
//const app = require('./common/module-require');//kylo-common
//import {moduleName} from "./common/module-name";
import '@uirouter/angular';
import 'kylo-services';
import './main/IndexController';
import './main/HomeController';
import './main/AccessDeniedController';
'use strict';
class Route{
   // app: ng.IModule;
    constructor () {
      //  this.app = app;
        /*this.*/app.config(["$ocLazyLoadProvider", "$stateProvider", "$urlRouterProvider",this.configFn.bind(this)]);
        /*this.*/app.run(['$rootScope', '$state', '$location', "$transitions","$timeout","$q", "$uiRouter","AccessControlService","AngularModuleExtensionService",
                this.runFn.bind(this)]);
    }

//var app = angular.module("", ["ngRoute"]);
configFn($ocLazyLoadProvider: any, $stateProvider: any, $urlRouterProvider: any){
        $ocLazyLoadProvider.config({
            modules: ['kylo', 'kylo.common', 'kylo.services', 'kylo.feedmgr', 'kylo.feedmgr.templates', 'kylo.opsmgr'],
            asyncLoader: require,
            debug: false
        });

        function onOtherwise(AngularModuleExtensionService: any, $state: any,url: any){
            var stateData = AngularModuleExtensionService.stateAndParamsForUrl(url);
            if(stateData.valid) {
                $state.go(stateData.state,stateData.params);
            }
            else {
                $state.go('home')
            }
        }

        $urlRouterProvider.otherwise(($injector: any, $location: any)=>{
            var $state = $injector.get('$state');
            var svc = $injector.get('AngularModuleExtensionService');
            var url = $location.url();
            if(svc != null) {
                if (svc.isInitialized()) {
                    onOtherwise(svc,$state,url)
                    return true;
                }
                else {
                    $injector.invoke( ($window: any, $state: any,AngularModuleExtensionService: any)=>{
                        AngularModuleExtensionService.registerModules().then(()=> {
                            onOtherwise(AngularModuleExtensionService,$state,url)
                            return true;
                        });
                    });
                    return true;
                }
            }
            else {
                $location.url("/home")
            }

        });

        $stateProvider
            .state('home', {
                url: '/home',
                views: {
                    "content": {
                        templateUrl: "js/main/home.html",
                        controller: 'HomeController',
                        controllerAs: 'vm'
                    }
                },
                resolve: { // Any property in resolve should return a promise and is executed before the view is loaded
                    loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any)=> {
                        // you can lazy load files for an existing module
                        return $ocLazyLoad.load('main/HomeController');
                    }]
                }
            })

        //Feed Manager
        $stateProvider.state({
            name: 'feeds.**',
            url: '/feeds',
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/feeds/module').
                                    then(function success(args: any) {
                                //upon success go back to the state
                                $stateProvider.stateService.go('feeds')
                                return args;
                            }, function error(err: any) {
                                console.log("Error loading feeds ", err);
                                return err;
                });
            }
        }).state({
            name: 'define-feed.**',
            url: '/define-feed?templateId&templateName&feedDescriptor',
            params: {
                templateId:null,
                templateName:null,
                feedDescriptor:null,
                bcExclude_cloning:null,
                bcExclude_cloneFeedName:null
            },
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/feeds/define-feed/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('define-feed', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading define-feed ", err);
                    return err;
                });
            }
        }).state({
            name: 'feed-details.**',
            url: '/feed-details/{feedId}',
            params: {
                feedId: null,
                tabIndex: 0
            },
            lazyLoad: (transition: any, state: any)=>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/feeds/edit-feed/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('feed-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading feed-details ", err);
                    return err;
                });
            }
        }).state({
            name: 'edit-feed.**',
            url: '/edit-feed/{feedId}',
            params: {
                feedId: null
            },
            lazyLoad: (transition: any, state: any)=> {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/feeds/edit-feed/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('edit-feed', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading edit-feed", err);
                    return err;
                });
            }
        })

        $stateProvider.state({
            name: 'categories.**',
            url: '/categories',
            lazyLoad: (transition: any) =>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/categories/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('categories')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading categories ", err);
                    return err;
                });
            }
        }).state('category-details.**', {
            url: '/category-details/{categoryId}',
            params: {
                categoryId: null
            },
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/categories/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('category-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading categories ", err);
                    return err;
                });
            }
        });

        $stateProvider.state('registered-templates.**', {
            url: '/registered-templates',
            lazyLoad: (transition: any) =>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/templates/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('registered-templates')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading registered-templates ", err);
                    return err;
                });
            }
        })

        $stateProvider.state('register-template.**', {
            url: '/register-template',
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/templates/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('register-template')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading register-template ", err);
                    return err;
                });
            }
        })

        $stateProvider.state({
            name: 'service-level-agreements.**',
            url: '/service-level-agreements',
            params: {
                slaId: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/sla/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-level-agreements',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-level-agreements ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'catalog.**',
            url: '/catalog',
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/tables/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('catalog');
                    return args;
                }, function error(err: any) {
                    console.log("Error loading catalog data sources ", err);
                    return err;
                });
            }
        }).state({
            name: 'schemas.**',
            url: '/catalog/{datasource}/schemas',
            params: {
                datasource: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/tables/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('schemas', transition.params());
                    return args;
                }, function error(err: any) {
                    console.log("Error loading schemas ", err);
                    return err;
                });
            }
        }).state({
            name: 'schemas-schema.**',
            url: '/catalog/{datasource}/schemas/{schema}',
            params: {
                datasource: null,
                schema: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/tables/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('schemas-schema', transition.params());
                    return args;
                }, function error(err: any) {
                    console.log("Error loading tables ", err);
                    return err;
                });
            }
        }).state({
            name: 'schemas-schema-table.**',
            url: '/catalog/{datasource}/schemas/{schema}/{tableName}',
            params: {
                datasource: null,
                schema: null,
                tableName: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/tables/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('schemas-schema-table', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading table ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'users.**',
            url: '/users',
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('auth/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('users')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading users ", err);
                    return err;
                });
                ;
            }
        });

        $stateProvider.state({
            name: 'groups.**',
            url: '/groups',
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('auth/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('groups')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading groups ", err);
                    return err;
                });
            }
        });

        $stateProvider.state('search.**', {
            url: '/search',
            params: {
                bcExclude_globalSearchResetPaging: null
            },
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('search/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('search',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading search ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'business-metadata.**',
            url: '/business-metadata',
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/business-metadata/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('business-metadata')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading business-metadata ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'visual-query.**',
            url: '/visual-query/{engine}',
            params: {
                engine: null
            },
            loadChildren: "feed-mgr/visual-query/visual-query.module#VisualQueryModule"
        });

        //Ops Manager

        $stateProvider.state({
            name: 'dashboard.**',
            url: '/dashboard',
            lazyLoad: (transition: any) =>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/overview/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('dashboard')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading ops manager dashboard ", err);
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
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/feeds/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('ops-feed-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading ops-feed-details ", err);
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
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/feeds/feed-stats/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('feed-stats', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading feed-stats ", err);
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
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/jobs/details/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('job-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading job-details ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'jobs.**',
            url: '/jobs',
            params: {
                filter: null,
                tab:null
            },
            lazyLoad:(transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/jobs/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('jobs', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading jobs ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'service-health.**',
            url: '/service-health',
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/service-health/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-health', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-health ", err);
                    return err;
                });
            }
        }).state({
            name: 'service-details.**',
            url: '/service-details/{serviceName}',
            params: {
                serviceName: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/service-health/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-details ", err);
                    return err;
                });
            }
        }).state({
            name: 'service-component-details.**',
            url: '/service-details/{serviceName}/{componentName}',
            params: {
                serviceName: null,
                componentName: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/service-health/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-component-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-component-details ", err);
                    return err;
                });
            }
        })

        $stateProvider.state({
            name: 'scheduler.**',
            url: '/scheduler',
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/scheduler/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('scheduler', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading scheduler ", err);
                    return err;
                });
            }
        })

        $stateProvider.state({
            name: 'alerts.**',
            url: '/alerts',
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/alerts/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('alerts', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading alerts ", err);
                    return err;
                });
            }
        }).state({
            name: 'alert-details.**',
            url: '/alert-details/{alertId}',
            params: {
                alertId: null
            },
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/alerts/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('alert-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading alert-details ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'charts.**',
            url: '/charts',
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/charts/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('charts', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading charts ", err);
                    return err;
                });
            }
        });



        $stateProvider.state({
            name: "datasources.**",
            url: "/datasources",
            lazyLoad: (transition: any)=>{
                transition.injector().get("$ocLazyLoad").load("feed-mgr/datasources/module").then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go("datasources", transition.params());
                    return args;
                }, function error(err: any) {
                    console.log("Error loading datasources.", err);
                    return err;
                });
            }
        }).state({
            name: "datasource-details.**",
            url: "/datasource-details",
            lazyLoad: (transition: any) =>{
                transition.injector().get("$ocLazyLoad").load("feed-mgr/datasources/module").then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go("datasource-details", transition.params());
                    return args;
                }, function error(err: any) {
                    console.log("Error loading datasource-details.", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: "domain-types.**",
            url: "/domain-types",
            lazyLoad: (transition: any)=>{
                transition.injector().get("$ocLazyLoad")
                    .load("feed-mgr/domain-types/module")
                    .then(function (args: any) {
                        $stateProvider.stateService.go("domain-types", transition.params());
                        return args;
                    }, function (err: any) {
                        console.log("Error loading domain-types.", err);
                        return err;
                    });
            }
        }).state({
            name: "domain-type-details.**",
            url: "/domain-type-details/{domainTypeId}",
            lazyLoad: (transition: any)=>{
                transition.injector().get("$ocLazyLoad")
                    .load("feed-mgr/domain-types/module")
                    .then(function (args: any) {
                        $stateProvider.stateService.go("domain-type-details", transition.params());
                        return args;
                    }, function (err: any) {
                        console.log("Error loading domain-type-details.", err);
                        return err;
                    });
            }
        });

        $stateProvider.state({
            name: 'service-level-assessment.**',
            url: '/service-level-assessment/{assessmentId}',
            params: {
                assessmentId: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('ops-mgr/sla/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-level-assessment',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-level-assessment ", err);
                    return err;
                });

            }
        });

        $stateProvider.state({
            name: 'service-level-assessments.**',
            url: '/service-level-assessments',
            params: {
                filter: null
            },
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/sla/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-level-assessments',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-level-assessments ", err);
                    return err;
                });

            }
        });

        $stateProvider.state('jcr-query.**', {
            url: '/admin/jcr-query',
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('admin/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('jcr-query',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading admin jcr ", err);
                    return err;
                });
            }
        });

        $stateProvider.state('sla-email-templates.**', {
            url: '/sla-email-templates',
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('feed-mgr/sla/module')
                        .then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('sla-email-templates',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading sla email templates ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'sla-email-template.**',
            url: '/sla-email-template/:emailTemplateId',
            params: {
                emailTemplateId: null
            },
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/sla/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('sla-email-template',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading sla email template ", err);
                    return err;
                });

            }
        });

        $stateProvider.state('cluster.**', {
            url: '/admin/cluster',
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('admin/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('cluster',transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading admin cluster ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'projects.**',
            url: '/projects',
            lazyLoad: (transition: any)=> {
                transition.injector().get('$ocLazyLoad').load('plugin/projects/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('projects')
                    return args;
                }, function error(err: any) {
                    console.log("Error loading projects ", err);
                    return err;
                });
            }
        }).state('project-details.**', {
            url: '/project-details/{projectId}',
            params: {
                projectId: null
            },
            lazyLoad: (transition: any)=>{
                transition.injector().get('$ocLazyLoad').load('plugin/projects/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('project-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading projects ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
           name:'access-denied',
           url:'/access-denied',
           params:{attemptedState:null},
           views: {
               "content": {
                   templateUrl: "js/main/access-denied.html",
                   controller:'AccessDeniedController',
                   controllerAs:'vm'
               }
           },
           resolve: { // Any property in resolve should return a promise and is executed before the view is loaded
               loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any)=>{
                   // you can lazy load files for an existing module
                   return $ocLazyLoad.load('main/AccessDeniedController');
               }]
           }
        });
    }

runFn($rootScope: any, $state: any, $location: any, $transitions: any,$timeout: any, $q: any,
                    $uiRouter: any, AccessControlService: any,AngularModuleExtensionService: any) {
             //initialize the access control
             AccessControlService.init();

             $rootScope.$state = $state;
             $rootScope.$location = $location;

             $rootScope.typeOf = (value: any)=> {
                 return typeof value;
             };

             var onStartOfTransition =  (trans: any) => {
                 
                 if (!AccessControlService.isFutureState(trans.to().name)) {
                     //if we havent initialized the user yet, init and defer the transition
                     if (!AccessControlService.initialized) {
                         var defer = $q.defer();
                         $q.when(AccessControlService.init(), ()=> {
                             //if not allowed, go to access-denied
                             if (!AccessControlService.hasAccess(trans)) {
                                 if (trans.to().name != 'access-denied') {
                                     defer.resolve($state.target("access-denied", {attemptedState: trans.to()}));
                                 }
                             }
                             else {
                                 defer.resolve($state.target(trans.to().name, trans.params()));
                             }
                         });
                         return defer.promise;
                     }
                     else {
                         if (!AccessControlService.hasAccess(trans)) {
                             if (trans.to().name != 'access-denied') {
                                 return $state.target("access-denied", {attemptedState: trans.to()});
                             }
                         }
                     }
                 }
             }

             /**
              * Add a listener to the start of every transition to do Access control on the page
              * and redirect if not authorized
              */
             $transitions.onStart({}, (trans: any)=> {
                 if (AngularModuleExtensionService.isInitialized()) {
                     return onStartOfTransition(trans);
                 }
                 else {
                     var defer = $q.defer();
                     $q.when(AngularModuleExtensionService.registerModules(),()=> {
                         defer.resolve(onStartOfTransition(trans));
                     });
                     return defer.promise;
                 }

             });
         }
}

const routes = new Route();
export default routes;

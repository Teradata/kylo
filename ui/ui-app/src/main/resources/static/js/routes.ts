import {app} from './common/module-require';
import '@uirouter/angular';
import 'kylo-services';
import './main/IndexController';
import {AccessControlService} from './services/AccessControlService';
import {LoginNotificationService} from "./services/LoginNotificationService";
import { AccessDeniedComponent } from './main/AccessDeniedComponent';
import { HomeComponent } from './main/HomeComponent';
import {KyloRouterService} from "./services/kylo-router.service";
import {Lazy} from './kylo-utils/LazyLoadUtil';

'use strict';

class Route {
    // app: ng.IModule;
    constructor() {
        //  this.app = app;
        /*this.*/
        app.config(["$ocLazyLoadProvider", "$stateProvider", "$urlRouterProvider", this.configFn.bind(this)]);
        /*this.*/
        app.run(['$rootScope', '$state', '$location', "$transitions", "$timeout", "$q", "$uiRouter", "AccessControlService", "AngularModuleExtensionService", "LoginNotificationService","KyloRouterService","$ocLazyLoad",
            this.runFn.bind(this)]);
    }

//var app = angular.module("", ["ngRoute"]);
    configFn($ocLazyLoadProvider: any, $stateProvider: any, $urlRouterProvider: any) {
        function onOtherwise(AngularModuleExtensionService: any, $state: any, url: any) {
            var stateData = AngularModuleExtensionService.stateAndParamsForUrl(url);
            if (stateData.valid) {
                $state.go(stateData.state, stateData.params);
            }
            else {
                $state.go('home')
            }
        }

        $urlRouterProvider.otherwise(($injector: any, $location: any) => {
            var $state = $injector.get('$state');
            var svc = $injector.get('AngularModuleExtensionService');
            var url = $location.url();
            if (svc != null) {
                if (svc.isInitialized()) {
                    onOtherwise(svc, $state, url)
                    return true;
                }
                else {
                    $injector.invoke(($window: any, $state: any, AngularModuleExtensionService: any) => {
                        AngularModuleExtensionService.registerModules().then(() => {
                            onOtherwise(AngularModuleExtensionService, $state, url)
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
                        component: HomeComponent,
                    }
                },
                lazyLoad: ($transition$: any) => {
                    const $ocLazyLoad = $transition$.injector().get("$ocLazyLoad");
                    return import(/* webpackChunkName: "home.module" */ './main/HomeComponent')
                        .then(mod => {

                            $ocLazyLoad.load(mod);
                        })
                        .catch(err => {
                            throw new Error("Failed to load home controller, " + err);
                        });
                }
            });

        //Feed Manager
        $stateProvider.state({
            name: 'feeds.**',
            url: '/feeds',
            lazyLoad: ($transition$: any) => {
                const $ocLazyLoad = $transition$.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "feedmgr.feeds.module" */ "./feed-mgr/feeds/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "feeds"));
                };

                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });
        $stateProvider.state({
            name: 'import-feed.**',
            url: '/import-feed',
            params: {},
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "feedmgr.import-feed.module" */ "./feed-mgr/feeds/define-feed/module")
                    .then(mod => {

                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('import-feed', transition.params());
                            return args;
                        }, function error(err: any) {
                            console.error("Error loading import-feed ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load feed-mgr/feeds/define-feed/module, " + err);
                    });
            }
        });
        $stateProvider.state({
            name: 'categories.**',
            url: '/categories',
            loadChildren: './feed-mgr/categories/categories.module#CategoriesModule'
        });

        $stateProvider.state({
            name: 'registered-templates.**',
            url: '/registered-templates',
            loadChildren: './feed-mgr/templates/templates.module#TemplateModule'
        });

        $stateProvider.state({
            name: 'register-template.**',
            url: '/registered-template',
            loadChildren: './feed-mgr/templates/templates.module#TemplateModule'
        });

        $stateProvider.state({
            name: 'service-level-agreements.**',
            url: '/service-level-agreements',
            loadChildren: './feed-mgr/sla/sla.module#SLAModule'
        });
        $stateProvider.state({
            name: 'users.**',
            url: '/users',
            loadChildren: './auth/auth.module#AuthModule'
        });

        $stateProvider.state({
            name: 'groups.**',
            url: '/groups',
            loadChildren: './auth/auth.module#AuthModule'
        });

        $stateProvider.state({
            name: 'datasources.**',
            url: '/datasources',
            loadChildren: './feed-mgr/datasources/datasources.module#DataSourcesModule'
        });

        $stateProvider.state({
            name: 'search.**',
            url: '/search',
            loadChildren: './search/search.module#SearchModule'
        });


        $stateProvider.state({
            name: 'business-metadata.**',
            url: '/business-metadata',
            loadChildren: './feed-mgr/business-metadata/business-metadata.module#BusinessMetadataModule'
        });

        //Ops Manager

        $stateProvider.state({
            name: 'dashboard.**',
            url: '/dashboard',
            loadChildren: './ops-mgr/overview/overview.module#OverviewModule'
        });

        $stateProvider.state({
            name: 'ops-feed-details.**',
            url: '/ops-feed-details/{feedName}',
            params: {
                feedName: null
            },
            loadChildren: './ops-mgr/feeds/ops-mgr-feeds.module#OpsManagerFeedsModule'
        });

        $stateProvider.state({
            name: 'feed-stats.**',
            url: '/feed-stats/{feedName}',
            loadChildren: './ops-mgr/feeds/feed-stats/feed-stats.module#FeedStatsModule'
        });

        $stateProvider.state({
            name: 'jobs.**',
            url: '/jobs',
            loadChildren: './ops-mgr/jobs/jobs.module#JobsModule'
        });

        $stateProvider.state({
            name: 'job-details.**',
            url: '/job-details/{executionId}',
            params: {
                        executionId: null
                    },
            loadChildren: './ops-mgr/jobs/details/job-details.module#JobDetailsModule'
        });

        $stateProvider.state({
            name: 'service-health.**',
            url: '/service-health',
            loadChildren: './ops-mgr/service-health/ops-mgr-service-health.module#OpsManagerServiceHealthModule'
        });
        $stateProvider.state({
            name: 'service-details.**',
            url: '/service-details',
            loadChildren: './ops-mgr/service-health/ops-mgr-service-health.module#OpsManagerServiceHealthModule'
        });

        $stateProvider.state({
            name: 'scheduler.**',
            url: '/scheduler',
            loadChildren: './ops-mgr/scheduler/ops-mgr-scheduler.module#OpsManagerSchedulerModule'
        });

        $stateProvider.state({
            name: 'alerts.**',
            url: '/alerts',
            loadChildren: './ops-mgr/alerts/alerts.module#AlertsModule'
        });

        $stateProvider.state({
            name: 'charts.**',
            url: '/charts',
            loadChildren: './ops-mgr/charts/ops-mgr-charts.module#OpsManagerChartsModule'
        });

        $stateProvider.state({
            name: 'domain-types.**',
            url: '/domain-types',
            loadChildren: './feed-mgr/domain-types/domain-types.module#DomainTypesModule'
        });

        $stateProvider.state({
            name: 'service-level-assessment.**',
            url: '/service-level-assessment/{assessmentId}',
            loadChildren: './ops-mgr/sla/sla.module#SLAModule'
        });

        $stateProvider.state({
            name: 'service-level-assessments.**',
            url: '/service-level-assessments',
            loadChildren: './ops-mgr/sla/sla.module#SLAModule'
        });

        $stateProvider.state({
            name: 'sla-email-templates.**',
            url: '/sla-email-templates',
            loadChildren: './feed-mgr/sla/sla.module#SLAModule'
        });

        $stateProvider.state({
            name: 'projects.**',
            url: '/projects',
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('plugin/projects/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('projects')
                    return args;
                }, function error(err: any) {
                    console.error("Error loading projects ", err);
                    return err;
                });
            }
        }).state('project-details.**', {
            url: '/project-details/{projectId}',
            params: {
                projectId: null
            },
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('plugin/projects/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('project-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.error("Error loading projects ", err);
                    return err;
                });
            }
        });


        $stateProvider
            .state('access-denied', {
                url: '/access-denied',
                views: {
                    "content": {
                        component: AccessDeniedComponent,
                    }
                },
                lazyLoad: ($transition$: any) => {
                    const $ocLazyLoad = $transition$.injector().get("$ocLazyLoad");
                    return import(/* webpackChunkName: "accessDenied.module" */ './main/AccessDeniedComponent')
                        .then(mod => {

                            $ocLazyLoad.load(mod);
                        })
                        .catch(err => {
                            throw new Error("Failed to load access denied controller, " + err);
                        });
                }
            });
    }

    runFn($rootScope: any, $state: any, $location: any, $transitions: any, $timeout: any, $q: any,
          $uiRouter: any, accessControlService: AccessControlService, AngularModuleExtensionService: any,
          loginNotificationService: LoginNotificationService,
          kyloRouterService:KyloRouterService, $ocLazyLoad: any) {

        require('./services/module');
        require('./common/module');
        require('./feed-mgr/module');
        require('./feed-mgr/module-require');


        //initialize the access control
        // accessControlService.init();
        loginNotificationService.initNotifications();

        $rootScope.$state = $state;
        $rootScope.$location = $location;

        $rootScope.typeOf = (value: any) => {
            return typeof value;
        };



        var checkAccess = (trans: any) => {

            if (!accessControlService.isFutureState(trans.to().name)) {
                //if we havent initialized the user yet, init and defer the transition
                if (!accessControlService.initialized) {
                    var defer = $q.defer();
                    $q.when(accessControlService.init(), () => {
                        //if not allowed, go to access-denied
                        if (!accessControlService.hasAccess(trans)) {
                            if (trans.to().name != 'access-denied') {

                                let redirect = "access-denied";
                                if(trans.to().data) {
                                    redirect = trans.to().data.accessRedirect != undefined ? trans.to().data.accessRedirect : "access-denied";
                                }

                                defer.resolve($state.target(redirect, {attemptedState: trans.to()}));
                            }
                        }
                        else {
                            kyloRouterService.saveTransition(trans)
                            defer.resolve($state.target(trans.to().name, trans.params()));
                        }
                    });
                    return defer.promise;
                }
                else {
                    if (!accessControlService.hasAccess(trans)) {
                        if (trans.to().name != 'access-denied') {

                            let redirect = "access-denied";
                            if(trans.to().data) {
                                redirect = trans.to().data.accessRedirect != undefined ? trans.to().data.accessRedirect : "access-denied";
                            }
                            return $state.target(redirect, {attemptedState: trans.to()});
                        }
                    }
                    else {
                        kyloRouterService.saveTransition(trans)
                    }
                }
            }
            else {
                kyloRouterService.saveTransition(trans)
            }
        }

        var onBeforeTransition = (trans: any) => {
            return checkAccess(trans);
        }


        /**
         * Add a listener to the start of every transition to do Access control on the page
         * and redirect if not authorized
         *

           var onStartOfTransition = (trans: any) => {
            return checkAccess(trans);
        }

        $transitions.onStart({}, (trans: any) => {
            if (AngularModuleExtensionService.isInitialized()) {
                return onStartOfTransition(trans);
            }
            else {
                var defer = $q.defer();
                $q.when(AngularModuleExtensionService.registerModules(), () => {
                    defer.resolve(onStartOfTransition(trans));
                });
                return defer.promise;
            }

        });
         */

        $transitions.onBefore({}, (trans: any) => {
            if (AngularModuleExtensionService.isInitialized()) {
                return onBeforeTransition(trans);
            }
            else {
                var defer = $q.defer();
                $q.when(AngularModuleExtensionService.registerModules(), () => {
                    defer.resolve(onBeforeTransition(trans));
                });
                return defer.promise;
            }

        });
    }
}

const routes = new Route();
export default routes;

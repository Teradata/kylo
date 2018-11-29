import {app} from './common/module-require';
import '@uirouter/angular';
import 'kylo-services';
import './main/IndexController';
import './main/HomeController';
import './main/AccessDeniedController';
import {AccessControlService} from './services/AccessControlService';
import LoginNotificationService from "./services/LoginNotificationService";
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
                        component: 'homeController',
                    }
                },
                lazyLoad: ($transition$: any) => {
                    const $ocLazyLoad = $transition$.injector().get("$ocLazyLoad");
                    return import(/* webpackChunkName: "home.module" */ './main/HomeController')
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
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "feedmgr.categories.module" */ "./feed-mgr/categories/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "categories", transition.params()));
                };
                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });
        $stateProvider.state({
            name: 'category-details.**',
            url: '/category-details/{categoryId}',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "feedmgr.categories.module" */ "./feed-mgr/categories/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "category-details", transition.params()));
                };
                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state('registered-templates.**', {
            url: '/registered-templates',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "admin.registered-templates.module" */ "././feed-mgr/templates/module")
                    .then(mod => {

                        return $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('registered-templates')
                            return args;
                        }, function error(err: any) {
                            console.error("Error loading registered-templates ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ././feed-mgr/templates/module, " + err);
                    });
            }
        });

        $stateProvider.state('register-template.**', {
            url: '/register-template',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "feedmgr.templates.module" */ "./feed-mgr/templates/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "register-template"));
                };
                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        })
        $stateProvider.state({
            name: 'users.**',
            url: '/users',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "admin.auth.module" */ "./auth/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "users", transition.params()));
                };
                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });
        $stateProvider.state({
            name: 'user-details.**',
            url: '/user-details/{userId}',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "admin.auth.module" */ "./auth/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "user-details", transition.params()));
                };
                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'groups.**',
            url: '/groups',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "admin.auth.module" */ "./auth/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "groups", transition.params()));
                };
                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });
        $stateProvider.state({
            name: 'group-details.**',
            url: '/group-details/{groupId}',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "admin.auth.module" */ "./auth/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "group-details", transition.params()));
                };
                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'search.**',
            url: '/search',
            params: {
                bcExclude_globalSearchResetPaging: null
            },
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "kylo.search" */ "./search/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "search", transition.params()));
                };

                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'business-metadata.**',
            url: '/business-metadata',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "feedmgr.business-metadata.module" */ "./feed-mgr/business-metadata/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "business-metadata"));
                };

                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        //Ops Manager

        $stateProvider.state({
            name: 'dashboard.**',
            url: '/dashboard',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "ops-mgr.overview.module" */ "./ops-mgr/overview/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "dashboard"));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'ops-feed-details.**',
            url: '/ops-feed-details/{feedName}',
            params: {
                feedName: null
            },
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('./ops-mgr/feeds/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('ops-feed-details', transition.params())
                    return args;
                }, function error(err: any) {
                    console.error("Error loading ops-feed-details ", err);
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
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('./ops-mgr/feeds/feed-stats/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('feed-stats', transition.params())
                    return args;
                }, function error(err: any) {
                    console.error("Error loading feed-stats ", err);
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
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "ops-mgr.job-details.module" */ './ops-mgr/jobs/details/module')
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, 'job-details', transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'jobs.**',
            url: '/jobs',
            params: {
                filter: null,
                tab: null
            },
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "ops-mgr.jobs.module" */ "./ops-mgr/jobs/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, 'jobs', transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'service-health.**',
            url: '/service-health',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "opsmgr.service-health.module" */ "./ops-mgr/service-health/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "service-health", transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });
        $stateProvider.state({
            name: 'service-details.**',
            url: '/service-details/{serviceName}',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "opsmgr.service-health.module" */ "./ops-mgr/service-health/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "service-details", transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'scheduler.**',
            url: '/scheduler',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "ops-mgr.scheduler.module" */ './ops-mgr/scheduler/module')
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('scheduler', transition.params())
                            return args;
                        }, function error(err: any) {
                            console.error("Error loading scheduler ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./ops-mgr/scheduler/module, " + err);
                    });
            }
        });

        $stateProvider.state({
            name: 'alerts.**',
            url: '/alerts',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "ops-mgr.alerts.module" */ "./ops-mgr/alerts/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, 'alerts', transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });
        $stateProvider.state({
            name: 'alert-details.**',
            url: '/alert-details/{alertId}',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "ops-mgr.alerts.module" */ "./ops-mgr/alerts/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, 'alert-details', transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'charts.**',
            url: '/charts',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "ops-mgr.charts.module" */ "./ops-mgr/charts/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, 'charts', transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });


        $stateProvider.state({
            name: "domain-types.**",
            url: "/domain-types",
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "admin.domain-types.module" */ "./feed-mgr/domain-types/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "domain-types", transition.params()));
                };

                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });
        $stateProvider.state({
            name: "domain-type-details.**",
            url: "/domain-type-details/{domainTypeId}",
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "admin.domain-types.module" */ "./feed-mgr/domain-types/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "domain-type-details", transition.params()));
                };

                import(/* webpackChunkName: "feed-mgr.module-require" */ "./feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'service-level-assessment.**',
            url: '/service-level-assessment/{assessmentId}',
            params: {
                assessmentId: null
            },
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "ops-mgr.slas.module" */ "./ops-mgr/sla/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "service-level-assessment", transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
        });

        $stateProvider.state({
            name: 'service-level-assessments.**',
            url: '/service-level-assessments',
            params: {
                filter: null
            },
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                const onModuleLoad = () => {
                    import(/* webpackChunkName: "ops-mgr.slas.module" */ "./ops-mgr/sla/module")
                        .then(Lazy.onModuleFactoryImport($ocLazyLoad)).then(Lazy.goToState($stateProvider, "service-level-assessments", transition.params()));
                };
                import(/* webpackChunkName: "ops-mgr.module-require" */ "./ops-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }
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
                        component: 'accessDeniedController',
                    }
                },
                lazyLoad: ($transition$: any) => {
                    const $ocLazyLoad = $transition$.injector().get("$ocLazyLoad");
                    return import(/* webpackChunkName: "accessDenied.module" */ './main/AccessDeniedController')
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
        accessControlService.init();
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

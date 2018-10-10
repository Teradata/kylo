import {app} from './common/module-require';
//const app = require('./common/module-require');//kylo-common
//import {moduleName} from "./common/module-name";
import '@uirouter/angular';
import 'kylo-services';
import './main/IndexController';
import './main/HomeController';
import './main/AccessDeniedController';
import AccessControlService from './services/AccessControlService';
import LoginNotificationService from "./services/LoginNotificationService";
import {CatalogRouterModule} from "./feed-mgr/catalog/catalog.module";
import {KyloRouterService} from "./services/kylo-router.service";


const Angular = require('angular');

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
                            console.log('imported home controller', mod);
                            $ocLazyLoad.load(mod);
                        })
                        .catch(err => {
                            throw new Error("Failed to load future.feeds, " + err);
                        });
                }
            });

        //Feed Manager
        $stateProvider.state({
            name: 'feeds.**',
            url: '/feeds',
            lazyLoad: ($transition$: any) => {
                const $ocLazyLoad = $transition$.injector().get("$ocLazyLoad");

                return import(/* webpackChunkName: "feedmgr.feeds.module" */ "./feed-mgr/feeds/module")
                    .then(mod => {
                        $ocLazyLoad.load(mod.default.module).then(function success(args: any) {
                            $stateProvider.stateService.go('feeds');
                        }, function error(err: any) {
                            console.log("Error loading feeds", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load feed-mgr/feeds/feeds.module, " + err);
                    });
            }
        }).state({
            name: 'define-feed.**',
            url: '/define-feed?templateId&templateName&feedDescriptor',
            params: {
                templateId: null,
                templateName: null,
                feedDescriptor: null,
                bcExclude_cloning: null,
                bcExclude_cloneFeedName: null
            },
            lazyLoad: (transition: any) => {
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
            name: 'import-feed.**',
            url: '/import-feed',
            params: {},
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "feedmgr.import-feed.module" */ "./feed-mgr/feeds/define-feed/module.js")
                    .then(mod => {
                        console.log('imported feed-mgr/feeds/define-feed/module', mod);
                        $ocLazyLoad.load({name: 'kylo.feedmgr.definefeed'}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('import-feed', transition.params());
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading import-feed ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load feed-mgr/feeds/define-feed/module, " + err);
                    });
            }
        }).state({
            name: 'feed-details.**',
            url: '/feed-details/{feedId}',
            params: {
                feedId: null,
                tabIndex: 0
            },
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "feedmgr.feed-details.module" */ "./feed-mgr/feeds/edit-feed/module.js")
                    .then(mod => {
                        console.log('imported ./feed-mgr/feeds/edit-feed/module', mod);
                        $ocLazyLoad.load({name: 'kylo.feedmgr.editfeed'}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('feed-details', transition.params());
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading ./feed-mgr/feeds/edit-feed/module ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./feed-mgr/feeds/edit-feed/module, " + err);
                    });
            }
        }).state({
            name: 'edit-feed.**',
            url: '/edit-feed/{feedId}',
            params: {
                feedId: null
            },
            lazyLoad: (transition: any, state: any) => {
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
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "feedmgr.categories.module" */ "./feed-mgr/categories/module.js")
                    .then(mod => {
                        console.log('imported ./feed-mgr/categories/module.js', mod);
                        return $ocLazyLoad.load({name: 'kylo.feedmgr.categories'}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('categories')
                        }, function error(err: any) {
                            console.log("Error loading categories ", err);
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./feed-mgr/categories/module.js, " + err);
                    });
            }
        }).state('category-details.**', {
            url: '/category-details/{categoryId}',
            params: {
                categoryId: null
            },
            lazyLoad: (transition: any) => {
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
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "admin.registered-templates.module" */ "./feed-mgr/templates/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('registered-templates')
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading registered-templates ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./feed-mgr/templates/module, " + err);
                    });
            }
        });

        $stateProvider.state('register-template.**', {
            url: '/register-template',
            lazyLoad: (transition: any) => {
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
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('feed-mgr/sla/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-level-agreements', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-level-agreements ", err);
                    return err;
                });
            }
        });

        $stateProvider.state({
            name: 'users.**',
            url: '/users',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "admin.auth.module" */ "./auth/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('users')
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading users ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./auth/module, " + err);
                    });
            }
        });

        $stateProvider.state({
            name: 'groups.**',
            url: '/groups',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "admin.auth.module" */ "./auth/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('groups');
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading groups ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./auth/module, " + err);
                    });
            }
        });

        $stateProvider.state('search.**', {
            url: '/search',
            params: {
                bcExclude_globalSearchResetPaging: null
            },
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('search/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('search', transition.params())
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
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "feedmgr.business-metadata.module" */ "./feed-mgr/business-metadata/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('business-metadata')
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading business-metadata ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./feed-mgr/categories/module.js, " + err);
                    });
            }
        });

        $stateProvider.state({
            name: 'visual-query.**',
            url: '/visual-query/{engine}',
            params: {
                engine: null
            },
            loadChildren: "./feed-mgr/visual-query/visual-query.module#VisualQueryRouterModule"
        });

        //Ops Manager

        $stateProvider.state({
            name: 'dashboard.**',
            url: '/dashboard',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "ops-mgr.overview.module" */ "./ops-mgr/overview/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('dashboard')
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading ops manager dashboard ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./ops-mgr/overview/module, " + err);
                    });
            }
        });

        $stateProvider.state({
            name: 'ops-feed-details.**',
            url: '/ops-feed-details/{feedName}',
            params: {
                feedName: null
            },
            lazyLoad: (transition: any) => {
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
            lazyLoad: (transition: any) => {
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
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "ops-mgr.job-details.module" */ './ops-mgr/jobs/details/module')
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('job-details', transition.params())
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading job-details ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./ops-mgr/job/details/module, " + err);
                    });
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
                return import(/* webpackChunkName: "ops-mgr.jobs.module" */ "./ops-mgr/jobs/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('jobs', transition.params())
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading jobs ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ops-mgr/jobs/module, " + err);
                    });
            }
        });

        $stateProvider.state({
            name: 'service-health.**',
            url: '/service-health',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "opsmgr.service-health.module" */ "./ops-mgr/service-health/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('service-health', transition.params())
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading service-health ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ../ops-mgr/service-health/module, " + err);
                    });
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
                            console.log("Error loading scheduler ", err);
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
                return import(/* webpackChunkName: "ops-mgr.alerts.module" */ "./ops-mgr/alerts/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('alerts', transition.params())
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading alerts ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./ops-mgr/alerts/module, " + err);
                    });
            }
        });

        $stateProvider.state({
            name: 'charts.**',
            url: '/charts',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "ops-mgr.charts.module" */ "./ops-mgr/charts/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name}).then(function success(args: any) {
                            //upon success go back to the state
                            $stateProvider.stateService.go('charts', transition.params());
                            return args;
                        }, function error(err: any) {
                            console.log("Error loading charts ", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load ./ops-mgr/charts/module, " + err);
                    });
            }
        });


        $stateProvider.state({
            name: "domain-types.**",
            url: "/domain-types",
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "admin.domain-types.module" */ "./feed-mgr/domain-types/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name})
                        .then(function (args: any) {
                            $stateProvider.stateService.go("domain-types", transition.params());
                            return args;
                        }, function (err: any) {
                            console.log("Error loading domain-types.", err);
                            return err;
                        });
                    })
                    .catch(err => {
                        throw new Error("Failed to load feed-mgr/domain-types/module, " + err);
                    });
            }
        });

        $stateProvider.state({
            name: 'service-level-assessment.**',
            url: '/service-level-assessment/{assessmentId}',
            params: {
                assessmentId: null
            },
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/sla/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-level-assessment', transition.params())
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
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('ops-mgr/sla/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('service-level-assessments', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading service-level-assessments ", err);
                    return err;
                });

            }
        });

        $stateProvider.state('jcr-query.**', {
            url: '/admin/jcr-query',
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('admin/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('jcr-query', transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading admin jcr ", err);
                    return err;
                });
            }
        });

        $stateProvider.state('sla-email-templates.**', {
            url: '/sla-email-templates',
            lazyLoad: (transition: any) => {
                const $ocLazyLoad = transition.injector().get('$ocLazyLoad');
                return import(/* webpackChunkName: "admin.sla-email-templates.module" */ "./feed-mgr/sla/module")
                    .then(mod => {
                        $ocLazyLoad.load({name: mod.default.module.name})
                            .then(function success(args: any) {
                                //upon success go back to the state
                                $stateProvider.stateService.go('sla-email-templates', transition.params())
                                return args;
                            }, function error(err: any) {
                                console.log("Error loading sla email templates ", err);
                                return err;
                            });
                    })
                    .catch(err => {
                        throw new Error("Failed to load feed-mgr/sla/module, " + err);
                    });
            }
        });

        $stateProvider.state('cluster.**', {
            url: '/admin/cluster',
            lazyLoad: (transition: any) => {
                transition.injector().get('$ocLazyLoad').load('admin/module').then(function success(args: any) {
                    //upon success go back to the state
                    $stateProvider.stateService.go('cluster', transition.params())
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
            lazyLoad: (transition: any) => {
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
            lazyLoad: (transition: any) => {
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
            name: 'access-denied',
            url: '/access-denied',
            params: {attemptedState: null},
            views: {
                "content": {
                    // templateUrl: "js/main/access-denied.html",
                    component: 'acessDeniedController',
                    //controllerAs:'vm'
                }
            },
            resolve: { // Any property in resolve should return a promise and is executed before the view is loaded
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    // you can lazy load files for an existing module
                    return $ocLazyLoad.load('main/AccessDeniedController');
                }]
            }
        });

        $stateProvider.state({
            name: 'catalog.**',
            url: '/catalog',
            loadChildren: './feed-mgr/catalog/catalog.module#CatalogRouterModule'
        });

        $stateProvider.state({
            name: 'feed-definition.**',
            url: '/feed-definition',
            loadChildren: './feed-mgr/feeds/define-feed-ng2/define-feed.module#DefineFeedModule'
        });

        $stateProvider.state({
            name: 'repository.**',
            url: '/repository',
            loadChildren: './repository/repository.module#RepositoryModule'
        });

        $stateProvider.state({
            name: 'template-info.**',
            url: '/template-info',
            loadChildren: './repository/repository.module#RepositoryModule'
        });

        $stateProvider.state({
            name: 'import-template.**',
            url: '/importTemplate',
            loadChildren: './repository/repository.module#RepositoryModule'
        });
    }

    runFn($rootScope: any, $state: any, $location: any, $transitions: any, $timeout: any, $q: any,
          $uiRouter: any, accessControlService: AccessControlService, AngularModuleExtensionService: any,
          loginNotificationService: LoginNotificationService,
          kyloRouterService:KyloRouterService, $ocLazyLoad: any) {
        //initialize the access control
        accessControlService.init();
        loginNotificationService.initNotifications();

        $rootScope.$state = $state;
        $rootScope.$location = $location;

        $rootScope.typeOf = (value: any) => {
            return typeof value;
        };

        var onStartOfTransition = (trans: any) => {

            if (!accessControlService.isFutureState(trans.to().name)) {
                //if we havent initialized the user yet, init and defer the transition
                if (!accessControlService.initialized) {
                    var defer = $q.defer();
                    $q.when(accessControlService.init(), () => {
                        //if not allowed, go to access-denied
                        if (!accessControlService.hasAccess(trans)) {
                            if (trans.to().name != 'access-denied') {
                                defer.resolve($state.target("access-denied", {attemptedState: trans.to()}));
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
                            return $state.target("access-denied", {attemptedState: trans.to()});
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

        /**
         * Add a listener to the start of every transition to do Access control on the page
         * and redirect if not authorized
         */
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

        import(/* webpackChunkName: "services.module" */ './services/module')
            .then(mod => {
                $ocLazyLoad.load(mod.default)
            })
            .catch(err => {
                throw new Error("Failed to load services module, " + err);
            });
        import(/* webpackChunkName: "common.module" */ './common/module')
            .then(mod => {
                $ocLazyLoad.load(mod.default)
            })
            .catch(err => {
                throw new Error("Failed to load common module, " + err);
            });
        import(/* webpackChunkName: "feedmgr.module" */ './feed-mgr/module')
            .then(mod => {
                $ocLazyLoad.load(mod.default)
            })
            .catch(err => {
                throw new Error("Failed to load feed-mgr module, " + err);
            });
    }
}

const routes = new Route();
export default routes;

import * as angular from 'angular';
const moduleName = require('./module-name');
import AccessConstants from "../../../constants/AccessConstants";

class ModuleFactory  {

    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$stateProvider','$compileProvider',this.configFn.bind(this)]);
    }

    configFn($stateProvider:any, $compileProvider:any) {
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
                    templateUrl: './define-feed.html',
                    controller: 'DefineFeedController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                // loadMyCtrl: lazyLoadController(['./DefineFeedController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.define-feed.controller" */ './DefineFeedController')
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
                    component: 'thinkbigDefineFeedCompleteController',
                }
            },
            resolve: {
                // loadMyCtrl: lazyLoadController(['./feed-details/DefineFeedCompleteController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad:any) => {
                    return import(/* webpackChunkName: "feeds.define-feed-complete.controller" */ './feed-details/DefineFeedCompleteController')
                        .then(mod => {
                            console.log('imported DefineFeedCompleteController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load DefineFeedCompleteController, " + err);
                        });
                }]
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
                    templateUrl: './import-feed.html',
                    controller: 'ImportFeedController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                // loadMyCtrl: lazyLoadController(['./ImportFeedController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.import-feed.controller" */ './ImportFeedController')
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
                permissions:AccessConstants.UI_STATES.IMPORT_FEED.permissions
            }
        });
    }
}

const moduleFactory = new ModuleFactory();
export default moduleFactory;

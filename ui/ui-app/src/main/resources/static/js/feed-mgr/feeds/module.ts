import * as angular from 'angular';

import AccessConstants from "../../constants/AccessConstants";
const moduleName = require('./module-name');

class ModuleFactory  {

    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$stateProvider','$compileProvider',this.configFn.bind(this)]);
    }

    configFn($stateProvider:any, $compileProvider:any) {
        $compileProvider.preAssignBindingsEnabled(true);
        $stateProvider.state(AccessConstants.UI_STATES.FEEDS.state, {
            url: '/feeds',
            params: {
                tab: null
            },
            views: {
                'content': {
                    templateUrl: 'js/feed-mgr/feeds/feeds-table.html',
                    controller:'FeedsTableController',
                    controllerAs:'vm'
                }
            },
            lazyLoad: ($transition$: any) => {
                const $ocLazyLoad = $transition$.injector().get("$ocLazyLoad");

                return import(/* webpackChunkName: "feedmgr.module-require" */ '../module-require')
                    .then(mod => {
                        console.log('imported feed-mgr/module-require', mod);
                        $ocLazyLoad.load(mod);

                        return import(/* webpackChunkName: "feeds.table.module" */ './FeedsTableController')
                            .then(mod => $ocLazyLoad.load(mod.default))
                            .catch(err => {
                                throw new Error("Failed to load FeedsTableController, " + err);
                            });
                    })
                    .catch(err => {
                        throw new Error("Failed to load feed-mgr module-require, " + err);
                    });
            },
            data: {
                breadcrumbRoot: true,
                displayName: 'Feeds',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.FEEDS.permissions
            }
        });

    }
}

let moduleFactory = new ModuleFactory();
export default moduleFactory;

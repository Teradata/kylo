import * as angular from 'angular';

import AccessConstants from "../../constants/AccessConstants";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
//const lazyLoadUtil = require('../../kylo-utils/LazyLoadUtil');
const moduleName = require('./module-name');
const feedManager = require('kylo-feedmgr');

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
            resolve: {
                loadMyCtrl: this.lazyLoadController('feed-mgr/feeds/FeedsTableController')
            },
            data: {
                breadcrumbRoot: true,
                displayName: 'Feeds',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.FEEDS.permissions
            }
        });

    }
    lazyLoadController(path:string){
        return lazyLoadUtil.lazyLoadController(path,['feed-mgr/feeds/module-require']);
    }

}

export default new ModuleFactory();

/**
 * 
 define(['angular', 'search/module-name', 'kylo-common', 'kylo-services','kylo-feedmgr', "search/common/SearchController"],function(angular, moduleName){
    return angular.module(moduleName);
});

*/

import * as angular from 'angular';
import {moduleName} from "./module-name";
export {KyloCommonModule} from "../common/common.module";
export {KyloServicesModule} from "../services/services.module";
export const {KyloFeedManager} = require('../feed-mgr/module');
export {controller} from "./common/SearchController";

export  let moduleRequire = angular.module(moduleName);
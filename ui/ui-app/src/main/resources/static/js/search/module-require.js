/**
 *
 define(['angular', 'search/module-name', 'kylo-common', 'kylo-services','kylo-feedmgr', "search/common/SearchController"],function(angular, moduleName){
    return angular.module(moduleName);
});

*/
define(["require", "exports", "angular", "./module-name", "../common/common.module", "../services/services.module", "./common/SearchController"], function (require, exports, angular, module_name_1, common_module_1, services_module_1, SearchController_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.KyloCommonModule = common_module_1.KyloCommonModule;
    exports.KyloServicesModule = services_module_1.KyloServicesModule;
    exports.KyloFeedManager = require('../feed-mgr/module').KyloFeedManager;
    exports.controller = SearchController_1.controller;
    exports.moduleRequire = angular.module(module_name_1.moduleName);
});
//# sourceMappingURL=module-require.js.map
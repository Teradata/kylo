define(["require", "exports", "angular", "./module-name", "kylo-services", "./dir-pagination/dirPagination-arrows", "angular-nvd3", "/common.module"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            this.module = angular.module(module_name_1.moduleName, ['kylo.services', 'templates.navigate-before.html', 'templates.navigate-first.html', 'templates.navigate-last.html', 'templates.navigate-next.html']);
            this.module.config(['$compileProvider', this.configFn.bind(this)]);
        }
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        ModuleFactory.prototype.configFn = function ($compileProvider) {
            //pre-assign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map
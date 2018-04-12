define(["require", "exports", "angular", "./module-name", "../kylo-utils/LazyLoadUtil", "../../bower_components/angular-ui-codemirror/ui-codemirror"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var CodeMirror = require('../../bower_components/codemirror/lib/codemirror');
    var ModuleFactory = /** @class */ (function () {
        function ModuleFactory() {
            window.CodeMirror = CodeMirror;
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
        }
        ModuleFactory.prototype.runFn = function ($ocLazyLoad) {
            $ocLazyLoad.load({ name: 'kylo', files: [
                    'bower_components/codemirror/lib/codemirror.css',
                    'bower_components/codemirror/addon/hint/show-hint.css',
                    'bower_components/codemirror/addon/dialog/dialog.css',
                    'bower_components/codemirror/addon/tern/tern.css',
                    'codemirror/mode/pig/pig',
                    'codemirror/mode/properties/properties',
                    'codemirror/mode/python/python',
                    'codemirror/mode/velocity/velocity',
                    'codemirror/mode/xml/xml',
                    'codemirror/mode/shell/shell',
                    'codemirror/mode/javascript/javascript',
                    'codemirror/mode/sql/sql',
                    'codemirror/addon/tern/tern',
                    'codemirror/addon/hint/show-hint',
                    'codemirror/addon/hint/sql-hint',
                    'codemirror/addon/hint/xml-hint',
                    'codemirror/mode/groovy/groovy',
                    'codemirror/addon/dialog/dialog'
                ] });
        };
        return ModuleFactory;
    }());
    var module = new ModuleFactory();
    exports.default = module;
});
//# sourceMappingURL=module.js.map
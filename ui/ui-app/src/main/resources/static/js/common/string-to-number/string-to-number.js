define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive("stringToNumber", [function () {
            return {
                require: 'ngModel',
                link: function (scope, element, attrs, ngModel) {
                    ngModel.$parsers.push(function (value) {
                        return '' + value;
                    });
                    ngModel.$formatters.push(function (value) {
                        return parseFloat(value);
                        //parseFloat(value, 10);
                        /*Since parseFloat only parses numeric expressions in radix 10, there's no need for a radix parameter here.*/
                    });
                }
            };
        }
    ]);
});
//# sourceMappingURL=string-to-number.js.map
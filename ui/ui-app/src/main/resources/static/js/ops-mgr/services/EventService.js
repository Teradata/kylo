define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var EventService = /** @class */ (function () {
        function EventService($rootScope) {
            this.$rootScope = $rootScope;
        }
        return EventService;
    }());
    exports.default = EventService;
    angular.module(module_name_1.moduleName).service('EventService', ["$rootScope", EventService]);
});
//# sourceMappingURL=EventService.js.map
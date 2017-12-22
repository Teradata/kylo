define(["require", "exports", "angular", "rxjs/Subject", "./wrangler-event", "./wrangler-event-type"], function (require, exports, angular, Subject_1, wrangler_event_1, wrangler_event_type_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * Handles communication between UI components and the wrangler table.
     */
    var WranglerTableService = /** @class */ (function () {
        function WranglerTableService() {
            this.init();
        }
        /**
         * Refreshes the wrangler table.
         */
        WranglerTableService.prototype.refresh = function () {
            this.tableSource.next(new wrangler_event_1.WranglerEvent(wrangler_event_type_1.WranglerEventType.REFRESH));
        };
        /**
         * Registers a new wrangler table.
         */
        WranglerTableService.prototype.registerTable = function (cb) {
            this.tableSource.subscribe(cb);
        };
        /**
         * Subscribes a parent component or data source.
         */
        WranglerTableService.prototype.subscribe = function (cb) {
            this.dataSource.subscribe(cb);
        };
        /**
         * Removes all event listeners and resets this service.
         */
        WranglerTableService.prototype.unsubscribe = function () {
            this.dataSource.unsubscribe();
            this.tableSource.unsubscribe();
            this.init();
        };
        /**
         * Resets this service.
         */
        WranglerTableService.prototype.init = function () {
            this.dataSource = new Subject_1.Subject();
            this.tableSource = new Subject_1.Subject();
        };
        return WranglerTableService;
    }());
    exports.WranglerTableService = WranglerTableService;
    angular.module(moduleName).service("WranglerTableService", WranglerTableService);
});
//# sourceMappingURL=wrangler-table.service.js.map
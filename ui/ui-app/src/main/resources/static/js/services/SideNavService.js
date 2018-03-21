define(["require", "exports", "angular", "./module-name", "./BroadcastConstants", "./broadcast-service"], function (require, exports, angular, module_name_1, BroadcastConstants_1, broadcast_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var SideNavService = /** @class */ (function () {
        function SideNavService(BroadcastService) {
            this.BroadcastService = BroadcastService;
            this.isLockOpen = true;
            this.hideSideNav = function () {
                if (this.isLockOpen) {
                    this.isLockOpen = false;
                    BroadcastService.notify(BroadcastConstants_1.default.CONTENT_WINDOW_RESIZED, null, 600);
                }
            };
            this.showSideNav = function () {
                if (!this.isLockOpen) {
                    this.isLockOpen = true;
                    BroadcastService.notify(BroadcastConstants_1.default.CONTENT_WINDOW_RESIZED, null, 600);
                }
            };
        }
        return SideNavService;
    }());
    exports.default = SideNavService;
    angular.module(module_name_1.moduleName)
        .service('BroadcastService', ["$rootScope", "$timeout", broadcast_service_1.default])
        .service('SideNavService', ["BroadcastService", SideNavService]);
});
//# sourceMappingURL=SideNavService.js.map
define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var WranglerEventType = (function () {
        function WranglerEventType() {
            throw new Error("Instances of WranglerEventType cannot be constructed");
        }
        return WranglerEventType;
    }());
    /**
     * Indicates the UI should be refreshed
     */
    WranglerEventType.REFRESH = "refresh";
    exports.WranglerEventType = WranglerEventType;
});
//# sourceMappingURL=wrangler-event-type.js.map
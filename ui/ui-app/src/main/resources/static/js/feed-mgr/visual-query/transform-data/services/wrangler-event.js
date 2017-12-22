define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var WranglerEvent = /** @class */ (function () {
        /**
         * Constructs a new WranglerEvent of the specified type.
         */
        function WranglerEvent(type) {
            this.type_ = type;
        }
        Object.defineProperty(WranglerEvent.prototype, "type", {
            /**
             * Event type
             */
            get: function () {
                return this.type_;
            },
            enumerable: true,
            configurable: true
        });
        return WranglerEvent;
    }());
    exports.WranglerEvent = WranglerEvent;
});
//# sourceMappingURL=wrangler-event.js.map
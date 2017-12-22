define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var WindowUnloadService = /** @class */ (function () {
        function WindowUnloadService($transitions, $window) {
            var _this = this;
            this.$transitions = $transitions;
            // Setup event listeners
            $transitions.onBefore({}, function () { return _this.shouldChangeState(); });
            $window.onbeforeunload = function (e) { return _this.onBeforeUnload(e); };
        }
        /**
         * Disables the confirmation dialog.
         */
        WindowUnloadService.prototype.clear = function () {
            this.text_ = null;
        };
        /**
         * Called to setup the confirmation dialog on an unload event.
         *
         * @param e - the unload event
         * @returns the dialog text to be displayed, or {@code null} to allow the event
         */
        WindowUnloadService.prototype.onBeforeUnload = function (e) {
            if (this.text_ !== null) {
                e.returnValue = this.text_;
            }
            return this.text_;
        };
        /**
         * Enables the confirmation dialog and sets the dialog text.
         *
         * @param text - the dialog text
         */
        WindowUnloadService.prototype.setText = function (text) {
            this.text_ = text;
        };
        /**
         * Called when changing states.
         */
        WindowUnloadService.prototype.shouldChangeState = function () {
            if (this.text_ === null || confirm(this.text_)) {
                this.clear();
                return true;
            }
            else {
                return false;
            }
        };
        WindowUnloadService.$inject = ["$transitions", "$window"];
        return WindowUnloadService;
    }());
    exports.WindowUnloadService = WindowUnloadService;
    angular.module(require("services/module-name")).service("WindowUnloadService", WindowUnloadService);
});
//# sourceMappingURL=WindowUnloadService.js.map
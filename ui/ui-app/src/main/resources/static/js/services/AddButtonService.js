define(["require", "exports", "angular", "./module-name", "./broadcast-service"], function (require, exports, angular, module_name_1, broadcast_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AddButtonService = /** @class */ (function () {
        function AddButtonService(BroadcastService) {
            var _this = this;
            this.BroadcastService = BroadcastService;
            this.addButtons = {};
            this.NEW_ADD_BUTTON_EVENT = 'newAddButton';
            this.HIDE_ADD_BUTTON_EVENT = 'hideAddButton';
            this.SHOW_ADD_BUTTON_EVENT = 'showAddButton';
            //__tag = new AddButtonServiceTag();
            this.__tag = this.AddButtonServiceTag();
            this.registerAddButton = function (state, action) {
                _this.addButtons[state] = action;
                _this.BroadcastService.notify(_this.NEW_ADD_BUTTON_EVENT, state);
            };
            this.hideAddButton = function () {
                _this.BroadcastService.notify(_this.HIDE_ADD_BUTTON_EVENT);
            };
            this.showAddButton = function () {
                _this.BroadcastService.notify(_this.SHOW_ADD_BUTTON_EVENT);
            };
            this.isShowAddButton = function (state) {
                return _this.addButtons[state] != undefined;
            };
            this.unregisterAddButton = function (state) {
                _this.addButtons[state];
            };
            this.onClick = function (state) {
                var action = _this.addButtons[state];
                if (action) {
                    action();
                }
            };
        }
        AddButtonService.prototype.AddButtonServiceTag = function () { };
        return AddButtonService;
    }());
    exports.default = AddButtonService;
    angular.module(module_name_1.moduleName)
        .service('BroadcastService', ["$rootScope", "$timeout", broadcast_service_1.default])
        .service('AddButtonService', ["BroadcastService", AddButtonService]);
});
//# sourceMappingURL=AddButtonService.js.map
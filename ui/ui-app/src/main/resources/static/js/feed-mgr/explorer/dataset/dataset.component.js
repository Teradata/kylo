var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "@uirouter/angular"], function (require, exports, core_1, angular_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Displays tabs for configuring a data set (or connection).
     */
    var DatasetComponent = /** @class */ (function () {
        function DatasetComponent(state, stateRegistry) {
            this.state = state;
            this.stateRegistry = stateRegistry;
            /**
             * List of tabs
             */
            this.tabs = [];
        }
        DatasetComponent.prototype.ngOnInit = function () {
            // Add tabs and register router states
            if (this.dataSet.connector.tabs) {
                this.tabs = this.dataSet.connector.tabs;
                for (var _i = 0, _a = this.dataSet.connector.tabs; _i < _a.length; _i++) {
                    var tab = _a[_i];
                    if (tab.state) {
                        this.stateRegistry.register(tab.state);
                    }
                }
            }
            // Add system tabs
            this.tabs.push({ label: "Preview", sref: ".preview" });
            // Go to the first tab
            this.state.go(this.tabs[0].sref);
        };
        __decorate([
            core_1.Input(),
            __metadata("design:type", Object)
        ], DatasetComponent.prototype, "dataSet", void 0);
        DatasetComponent = __decorate([
            core_1.Component({
                selector: "explorer-dataset",
                templateUrl: "js/feed-mgr/explorer/dataset/dataset.component.html"
            }),
            __metadata("design:paramtypes", [angular_1.StateService, angular_1.StateRegistry])
        ], DatasetComponent);
        return DatasetComponent;
    }());
    exports.DatasetComponent = DatasetComponent;
});
//# sourceMappingURL=dataset.component.js.map
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "./wrangler/query-engine"], function (require, exports, core_1, query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * Displays the Visual Query page.
     */
    var VisualQueryComponent = (function () {
        /**
         * Constructs a {@code VisualQueryComponent}.
         */
        function VisualQueryComponent($scope, SideNavService, StateService) {
            this.SideNavService = SideNavService;
            this.StateService = StateService;
            // Manage the sidebar navigation
            SideNavService.hideSideNav();
            $scope.$on("$destroy", this.ngOnDestroy.bind(this));
        }
        /**
         * Navigates to the Feeds page when the stepper is cancelled.
         */
        VisualQueryComponent.prototype.cancelStepper = function () {
            this.StateService.navigateToHome();
        };
        /**
         * Resets the side state.
         */
        VisualQueryComponent.prototype.ngOnDestroy = function () {
            this.SideNavService.showSideNav();
        };
        VisualQueryComponent.prototype.ngOnInit = function () {
            this.dataModel = { engine: this.engine, model: {} };
        };
        VisualQueryComponent.prototype.$onInit = function () {
            this.ngOnInit();
        };
        return VisualQueryComponent;
    }());
    __decorate([
        core_1.Input(),
        __metadata("design:type", query_engine_1.QueryEngine)
    ], VisualQueryComponent.prototype, "engine", void 0);
    angular.module(moduleName).component('visualQuery', {
        bindings: {
            engine: "<"
        },
        controller: ["$scope", "SideNavService", "StateService", VisualQueryComponent],
        controllerAs: "vm",
        templateUrl: "js/feed-mgr/visual-query/visual-query.component.html"
    });
});
//# sourceMappingURL=visual-query.component.js.map
define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("./module-name");
    /**
     * Displays the Visual Query page for AngularJS.
     */
    angular.module(moduleName).component('visualQuery', {
        bindings: {
            engine: "<"
        },
        controller: (_a = /** @class */ (function () {
                /**
                 * Constructs a {@code VisualQueryComponent}.
                 */
                function class_1($scope, SideNavService, StateService) {
                    this.SideNavService = SideNavService;
                    this.StateService = StateService;
                    // Manage the sidebar navigation
                    SideNavService.hideSideNav();
                    $scope.$on("$destroy", this.ngOnDestroy.bind(this));
                }
                /**
                 * Navigates to the Feeds page when the stepper is cancelled.
                 */
                class_1.prototype.cancelStepper = function () {
                    this.StateService.navigateToHome();
                };
                /**
                 * Resets the side state.
                 */
                class_1.prototype.ngOnDestroy = function () {
                    this.SideNavService.showSideNav();
                };
                class_1.prototype.ngOnInit = function () {
                    this.dataModel = { engine: this.engine, model: {} };
                };
                class_1.prototype.$onInit = function () {
                    this.ngOnInit();
                };
                return class_1;
            }()),
            _a.$inject = ["$scope", "SideNavService", "StateService"],
            _a),
        controllerAs: "vm",
        template: "\n        <thinkbig-stepper total-steps=\"3\" stepper-name=\"VisualQueryStepper\" core-data-model=\"vm.dataModel\" on-cancel-stepper=\"vm.cancelStepper()\"\n                          template-url=\"js/feed-mgr/visual-query/visual-query.stepper.html\"></thinkbig-stepper>\n    "
    });
    var _a;
});
//# sourceMappingURL=visual-query.component.js.map
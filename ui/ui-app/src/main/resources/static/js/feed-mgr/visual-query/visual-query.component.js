define(["require", "exports", "./services/query-engine-factory.service"], function (require, exports) {
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
        function VisualQueryComponent($scope, $transition$, queryEngineFactory, SideNavService, StateService) {
            this.SideNavService = SideNavService;
            this.StateService = StateService;
            // Create the query engine and data model
            var engine = this.createEngine($transition$.params().engine, queryEngineFactory);
            this.dataModel = { engine: engine, model: {} };
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
        /**
         * Creates a new query engine from the specified factory.
         *
         * @param name - the path parameter or name of the engine
         * @param factory - the query engine factory
         * @returns the query engine
         */
        VisualQueryComponent.prototype.createEngine = function (name, factory) {
            var engineName = (function (name) {
                if (name === null) {
                    return "spark";
                }
                else {
                    return name;
                }
            })(name);
            return factory.getEngine(engineName);
        };
        return VisualQueryComponent;
    }());
    angular.module(moduleName).controller("VisualQueryComponent", ["$scope", "$transition$", "VisualQueryEngineFactory", "SideNavService", "StateService", VisualQueryComponent]);
});
//# sourceMappingURL=visual-query.component.js.map
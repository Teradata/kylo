define(["angular", "feed-mgr/feeds/define-feed/module-name"], function (angular, moduleName) {
    /**
     * Displays a table option stepper template.
     */
    var kyloTableOptionsStepper = function ($compile, $mdDialog, $templateRequest, StateService, UiComponentsService) {
        return {
            restrict: "E",
            scope: {
                coreDataModel: "=",
                selectedStepIndex: "=",
                stepIndex: "=",
                steps: "=",
                type: "@"
            },
            link: function ($scope, $element) {
                /**
                 * Gets the object for the table option step at the specified index.
                 * @param {number} index - the table option step index
                 * @returns {Object} the step
                 */
                $scope.getStep = function (index) {
                    return $scope.steps[$scope.getStepIndex(index)];
                };

                /**
                 * Gets the stepper step index for the specified table option step index.
                 * @param {number} index - the table option step index
                 * @returns {number} the stepper step index
                 */
                $scope.getStepIndex = function (index) {
                    $scope.totalOptions = Math.max(index + 1, $scope.totalOptions);
                    return $scope.stepIndex + index;
                };

                /**
                 * Indicates if the specified step is selected.
                 * @param {number} index - the table option step index
                 * @returns {boolean} true if the step is selected
                 */
                $scope.isStepSelected = function (index) {
                    return $scope.selectedStepIndex === $scope.getStepIndex(index);
                };

                /**
                 * Indicates if the specified step has been visited.
                 * @param {number} index - the table option step index
                 * @returns {boolean} true if the step is visited
                 */
                $scope.isStepVisited = function (index) {
                    return $scope.steps[$scope.getStepIndex(index)].visited;
                };

                // Loads the table option template
                UiComponentsService.getTemplateTableOption($scope.type)
                    .then(function (tableOption) {
                        return (tableOption.stepperTemplateUrl !== null) ? $templateRequest(tableOption.stepperTemplateUrl) : null;
                    })
                    .then(function (html) {
                        if (html !== null) {
                            var template = angular.element(html);
                            $element.append(template);
                            $compile(template)($scope);
                        }
                    }, function () {
                        $mdDialog.show(
                            $mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title("Create Failed")
                                .textContent("The table option template could not be loaded.")
                                .ariaLabel("Failed to create feed")
                                .ok("Got it!")
                        );
                        StateService.FeedManager().Feed().navigateToFeeds();
                    });
            }
        };
    };

    angular.module(moduleName).directive("kyloTableOptionsStepper", ["$compile", "$mdDialog", "$templateRequest", "StateService", "UiComponentsService", kyloTableOptionsStepper]);
});

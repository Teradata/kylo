define(["angular", "feed-mgr/feeds/define-feed/module-name", 'kylo-utils/LazyLoadUtil'], function (angular, moduleName, lazyLoadUtil) {
    /**
     * Displays a table option stepper template.
     */
    var kyloTableOptionsStepper = function ($compile, $mdDialog, $templateRequest, $ocLazyLoad, $injector, StateService, UiComponentsService, FeedService) {
        return {
            restrict: "E",
            scope: {
                coreDataModel: "=",
                selectedStepIndex: "=",
                stepIndex: "=",
                steps: "=",
                type: "@",
                stepperTemplateType:'@?'
            },
            require: ['^thinkbigStepper'],
            link: function ($scope, $element, attrs, controllers) {

                var stepperController = controllers[0];

                if(angular.isUndefined($scope.stepperTemplateType)){
                    $scope.stepperTemplateType = 'stepper';
                }
                if(angular.isUndefined(  $scope.totalOptions)){
                    $scope.totalOptions = 0;
                }

                /**
                 * The table option metadata
                 * @type {null}
                 */
                $scope.tableOption = null;

                /**
                 *
                 * @type {null}
                 */
                var tableOptionInitializerPromise = null;
                /**
                 * Gets the object for the table option step at the specified index.
                 * @param {number} index - the table option step index
                 * @returns {Object} the step
                 */
                $scope.getStep = function (index) {
                    var step =  $scope.steps[$scope.getStepIndex(index)];
                    return step;
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
                        $scope.tableOption = tableOption;
                        //check to see if we are complete and should fire our initializer script for the stepper
                        //complete will happen after all the pre-steps and the feed steps have been successfully compiled and added to the stepper
                        var complete = UiComponentsService.completeStepperTemplateRender(tableOption.type)
                        if(complete && angular.isDefined(tableOption.initializeScript)){
                            tableOptionInitializerPromise =   $ocLazyLoad.load([tableOption.initializeScript]);
                        }
                        //Determine if we are loading pre-steps or feed steps
                        var property = 'stepperTemplateUrl';
                        if($scope.stepperTemplateType == 'pre-step') {
                            property = 'preStepperTemplateUrl';
                        }
                        return (tableOption[property] !== null) ? $templateRequest(tableOption[property]) : null;
                    })
                    .then(function (html) {
                        if (html !== null) {
                            var template = angular.element(html);
                            $element.append(template);
                            $compile(template)($scope);
                            if($scope.stepperTemplateType == 'pre-step' && angular.isDefined($scope.coreDataModel)) {
                                $scope.coreDataModel.renderTemporaryPreStep = false;
                            }

                            if(angular.isDefined(tableOptionInitializerPromise)){
                                tableOptionInitializerPromise.then(function(file) {

                                    var serviceName = $scope.tableOption.initializeServiceName;
                                    if(angular.isDefined(serviceName)) {
                                        var svc = $injector.get(serviceName);
                                        if (angular.isDefined(svc) && angular.isFunction(svc.initializeCreateFeed)) {
                                            var createFeedModel = FeedService.createFeedModel;
                                            svc.initializeCreateFeed($scope.tableOption, stepperController, createFeedModel);
                                        }
                                    }
                                });
                            }
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

                $element.on('$destroy',function(){

                });
            }
        };
    };

    angular.module(moduleName).directive("kyloTableOptionsStepper", ["$compile", "$mdDialog", "$templateRequest", "$ocLazyLoad", "$injector", "StateService", "UiComponentsService","FeedService", kyloTableOptionsStepper]);
});

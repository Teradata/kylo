import * as angular from 'angular';
const moduleName = require('feed-mgr/feeds/define-feed/module-name');

    /**
     * Displays a table option stepper template.
     */
    var kyloTableOptionsStepper = function ($compile:any, $mdDialog:any, $templateRequest:any, $ocLazyLoad:any, $injector:any, StateService:any, UiComponentsService:any, FeedService:any) {
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
            link:  ($scope:any, $element:any, attrs:any, controllers:any) => {

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
                var tableOptionInitializerPromise:any = null;
                /**
                 * Gets the object for the table option step at the specified index.
                 * @param {number} index - the table option step index
                 * @returns {Object} the step
                 */
                $scope.getStep = (index:any) => {
                    var step =  $scope.steps[$scope.getStepIndex(index)];
                    return step;
                };

                /**
                 * Gets the stepper step index for the specified table option step index.
                 * @param {number} index - the table option step index
                 * @returns {number} the stepper step index
                 */
                $scope.getStepIndex = (index:any) => {

                    $scope.totalOptions = Math.max(index + 1, $scope.totalOptions);
                    return $scope.stepIndex + index;
                };

                /**
                 * Indicates if the specified step is selected.
                 * @param {number} index - the table option step index
                 * @returns {boolean} true if the step is selected
                 */
                $scope.isStepSelected = (index:any) => {
                    return $scope.selectedStepIndex === $scope.getStepIndex(index);
                };

                /**
                 * Indicates if the specified step has been visited.
                 * @param {number} index - the table option step index
                 * @returns {boolean} true if the step is visited
                 */
                $scope.isStepVisited = (index:any) => {
                    return $scope.steps[$scope.getStepIndex(index)].visited;
                };

                // Loads the table option template
                UiComponentsService.getTemplateTableOption($scope.type)
                    .then( (tableOption:any) => {
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
                    .then((html:any) => {
                        if (html !== null) {
                            var template = angular.element(html);
                            $element.append(template);
                            $compile(template)($scope);
                            if($scope.stepperTemplateType == 'pre-step' && angular.isDefined($scope.coreDataModel)) {
                                $scope.coreDataModel.renderTemporaryPreStep = false;
                            }

                            if(angular.isDefined(tableOptionInitializerPromise)){
                                tableOptionInitializerPromise.then((file:any) => {

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
                    },  () => {
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

                $element.on('$destroy',() =>{

                });
            }
        };
    };

export class TableOptionsStepperDirective {

}
angular.module(moduleName).directive("kyloTableOptionsStepper", ["$compile", "$mdDialog", "$templateRequest", "$ocLazyLoad", "$injector", "StateService", "UiComponentsService","FeedService", kyloTableOptionsStepper]);

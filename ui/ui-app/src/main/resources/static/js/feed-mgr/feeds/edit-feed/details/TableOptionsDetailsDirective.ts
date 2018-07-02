define(["angular", "feed-mgr/feeds/edit-feed/module-name"], function (angular:any, moduleName:any) {
    /**
     * Displays a table option feed details template.
     */
    var kyloTableOptionsDetails = function ($compile:any, $mdDialog:any, $templateRequest:any
        , $injector:any,$ocLazyLoad:any, StateService:any, UiComponentsService:any) {
        return {
            restrict: "E",
            scope: {
                type: "@",
                stepperTemplateType:'@?',
                versions: '=?'
            },
            link: function ($scope:any, $element:any) {
                if (angular.isUndefined($scope.versions)) {
                    $scope.versions = false;
                }

                if(angular.isUndefined($scope.stepperTemplateType)){
                    $scope.stepperTemplateType = 'stepper';
                }

                /**
                 * The table option metadata
                 * @type {null}
                 */
                $scope.tableOption = null;

                // Loads the table option template
                UiComponentsService.getTemplateTableOption($scope.type)
                    .then( (tableOption:any) => {
                        $scope.tableOption = tableOption;

                        //Determine if we are loading pre-steps or feed steps
                        var property = 'feedDetailsTemplateUrl';
                        if($scope.stepperTemplateType == 'pre-step') {
                            property = 'preFeedDetailsTemplateUrl';
                        }
                        return (tableOption[property] !== null) ? $templateRequest(tableOption[property]) : null;
                    })
                    .then( (html:any) => {
                        if (html !== null) {
                            var template = angular.element(html);
                            $element.append(template);
                            $compile(template)($scope);
                        }

                    },  ()=> {
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

    angular.module(moduleName).directive("kyloTableOptionsDetails", ["$compile", "$mdDialog", "$templateRequest", "$injector","$ocLazyLoad","StateService", "UiComponentsService", kyloTableOptionsDetails]);
});

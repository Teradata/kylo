define(["angular", "feed-mgr/feeds/edit-feed/module-name"], function (angular, moduleName) {
    /**
     * Displays a table option feed details template.
     */
    var kyloTableOptionsDetails = function ($compile, $mdDialog, $templateRequest, StateService, UiComponentsService) {
        return {
            restrict: "E",
            scope: {
                type: "@"
            },
            link: function ($scope, $element) {
                // Loads the table option template
                UiComponentsService.getTemplateTableOption($scope.type)
                    .then(function (tableOption) {
                        return (tableOption.feedDetailsTemplateUrl !== null) ? $templateRequest(tableOption.feedDetailsTemplateUrl) : null;
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

    angular.module(moduleName).directive("kyloTableOptionsDetails", ["$compile", "$mdDialog", "$templateRequest", "StateService", "UiComponentsService", kyloTableOptionsDetails]);
});

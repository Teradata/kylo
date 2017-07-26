define(["angular", "/example-ui-feed-stepper-plugin-1.0/module-name.js"], function (angular, moduleName) {

    var ExampleUiFeedStepperSection = function () {
        return {
            controller: "ExampleUiFeedStepperSectionController",
            controllerAs: "vm",
            templateUrl: "/example-ui-feed-stepper-plugin-1.0/example-ui-feed-stepper-section.html"
        }
    };

    function ExampleUiFeedStepperSectionController($scope, $q, AccessControlService, FeedService) {
        var self = this;

        // Indicates if the model may be edited
        self.allowEdit = false;

        self.editableSection = false;
        self.editModel = {};

        self.model = FeedService.editFeedModel;
        $scope.$watch(function() {
            return FeedService.editFeedModel;
        }, function(newVal) {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        });

        // Copy model for editing
        this.onEdit = function() {
            self.editModel = {
                tableOption: angular.copy(self.model.tableOption)
            };
        };

        // Save changes to the model
        this.onSave = function(ev) {
            FeedService.showFeedSavingDialog(ev, "Saving...", self.model.feedName);

            var copy = angular.copy(FeedService.editFeedModel);
            copy.tableOption = self.editModel.tableOption;

            FeedService.saveFeedModel(copy).then(function() {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.tableOption = self.editModel.tableOption;
            }, function(response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response.data);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        };

        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function(access) {
            self.allowEdit = access;
        });
    }

    angular.module(moduleName, [])
        .controller("ExampleUiFeedStepperSectionController", ["$scope", "$q", "AccessControlService", "FeedService", ExampleUiFeedStepperSectionController])
        .directive("exampleUiFeedStepperSection", ExampleUiFeedStepperSection);
});

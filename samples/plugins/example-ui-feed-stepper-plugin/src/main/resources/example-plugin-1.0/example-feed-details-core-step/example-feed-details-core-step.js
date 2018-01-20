define(["angular", "/example-plugin-1.0/module-name"], function (angular, moduleName) {

    var directive = function () {
        return {
            controller: "ExampleFeedDetailsCoreStepController",
            controllerAs: "vm",
            templateUrl: "/example-plugin-1.0/example-feed-details-core-step/example-feed-details-core-step.html"
        }
    };

    function controller($scope, $q, AccessControlService, FeedService) {
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
                FeedService.buildErrorData(self.model.feedName, response);
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

    angular.module(moduleName)
        .controller("ExampleFeedDetailsCoreStepController", ["$scope", "$q", "AccessControlService", "FeedService", controller])
        .directive("exampleFeedDetailsCoreStep", directive);
});

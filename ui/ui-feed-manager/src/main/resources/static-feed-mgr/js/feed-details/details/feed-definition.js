(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/details/feed-definition.html',
            controller: "FeedDefinitionController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope, $q, AccessControlService, FeedService) {

        var self = this;

        /**
         * Indicates if the feed definitions may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        this.model = FeedService.editFeedModel;
        this.editableSection = false;

        $scope.$watch(function(){
            return FeedService.editFeedModel;
        },function(newVal) {
            //only update the model if it is not set yet
            if(self.model == null) {
                self.model = angular.copy(FeedService.editFeedModel);
            }
        })


        self.editModel = {};


        this.onEdit = function(){
            //copy the model
            var copy = FeedService.editFeedModel;
            self.editModel= {};
            self.editModel.feedName = copy.feedName;
            self.editModel.systemFeedName = copy.systemFeedName;
            self.editModel.description = copy.description;
            self.editModel.templateId = copy.templateId;
        }

        this.onCancel = function() {

        }

        this.onSave = function (ev) {
            //save changes to the model
            FeedService.showFeedSavingDialog(ev, "Saving...", self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);

            copy.feedName = self.editModel.feedName;
            copy.systemFeedName = self.editModel.systemFeedName;
            copy.description = self.editModel.description;
            copy.templateId = self.editModel.templateId;
            copy.userProperties = null;

            FeedService.saveFeedModel(copy).then(function (response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.feedName = self.editModel.feedName;
                self.model.systemFeedName = self.editModel.systemFeedName;
                self.model.description = self.editModel.description;
                self.model.templateId = self.editModel.templateId;
            }, function (response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response.data);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        };

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowEdit = AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions);
                });
    };


    angular.module(MODULE_FEED_MGR).controller('FeedDefinitionController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedDefinition', directive);

})();

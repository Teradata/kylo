/*
 * Copyright (c) 2015.
 */

/**
 * This Directive is wired in to the FeedStatusIndicatorDirective.
 * It uses the OverviewService to watch for changes and update after the Indicator updates
 */
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

    var controller =  function($scope, FeedService) {

        var self = this;

        this.model = FeedService.editFeedModel;


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
        this.onSave = function() {
            //save changes to the model
            self.model.feedName = self.editModel.feedName;
            self.model.systemFeedName = self.editModel.systemFeedName;
            self.model.description = self.editModel.description;
            self.model.templateId = self.editModel.templateId;
            FeedService.saveFeedModel(FeedService.editFeedModel);
        }


    };


    angular.module(MODULE_FEED_MGR).controller('FeedDefinitionController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedDefinition', directive);

})();

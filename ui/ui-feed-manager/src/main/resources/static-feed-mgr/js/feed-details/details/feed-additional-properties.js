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
            templateUrl: 'js/feed-details/details/feed-additional-properties.html',
            controller: "FeedAdditionalPropertiesController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope, FeedService,FeedTagService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.editModel = {};
        this.editableSection = false;

        this.feedTagService = FeedTagService;
        self.tagChips = {};
        self.tagChips.selectedItem = null;
        self.tagChips.searchText = null;
        this.isValid = true;

        this.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip }
        }

        $scope.$watch(function(){
            return FeedService.editFeedModel;
        },function(newVal) {
            //only update the model if it is not set yet
            if(self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        })


        this.onEdit = function(){
            //copy the model
            var tags = angular.copy(FeedService.editFeedModel.tags);
            if(tags == undefined || tags == null){
                tags = [];
            }
            self.editModel= {};
            self.editModel.dataOwner =self.model.dataOwner;
            self.editModel.tags = tags

        }

        this.onCancel = function() {

        }
        this.onSave = function() {
            //save changes to the model
            self.model.tags = self.editModel.tags;
            self.model.dataOwner = self.editModel.dataOwner;
            FeedService.saveFeedModel(FeedService.editFeedModel);
        }

        this.onSave = function (ev) {
            //save changes to the model
            FeedService.showFeedSavingDialog(ev, "Saving Feed " + self.model.feedName, self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);

            copy.tags = self.editModel.tags;
            copy.dataOwner = self.editModel.dataOwner;

            FeedService.saveFeedModel(copy).then(function (response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.tags = self.editModel.tags;
                self.model.dataOwner = self.editModel.dataOwner;
            }, function (response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response.data);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        }




    };


    angular.module(MODULE_FEED_MGR).controller('FeedAdditionalPropertiesController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedAdditionalProperties', directive);

})();

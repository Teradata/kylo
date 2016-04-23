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
            templateUrl: 'js/feed-details/details/feed-schedule.html',
            controller: "FeedScheduleController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope, FeedService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.editModel = {};


        $scope.$watch(function(){
            return FeedService.editFeedModel;
        },function(newVal) {
            //only update the model if it is not set yet
            if(self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        })


        this.onScheduleStrategyChange = function() {
            if(self.editModel.schedule.schedulingStrategy == 'CRON_DRIVEN') {
                if(self.editModel.schedule.schedulingPeriod !="* * * * * ?" ) {
                    self.editModel.schedule.schedulingPeriod = "* * * * * ?";
                }
            }
            else if(self.editModel.schedule.schedulingStrategy == 'TIMER_DRIVEN'){
                self.editModel.schedule.schedulingPeriod = "5 min";
            }
        };


        this.onEdit = function(){
            //copy the model
            self.editModel.schedule = angular.copy(FeedService.editFeedModel.schedule);
        }

        this.onCancel = function() {

        }
        this.onSave = function() {
            //save changes to the model
            self.model.schedule = self.editModel.schedule;
            FeedService.saveFeedModel(FeedService.editFeedModel);
            //save it
            /*
            FeedService.saveFeedModel(FeedService.editFeedModel).then(function(response){
                //TODO account for any warnings

            }, function(err){
                console.log('error!',err)
                self.showCompleteDialog(err);
            });
            */
        }


    };


    angular.module(MODULE_FEED_MGR).controller('FeedScheduleController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedSchedule', directive);

})();

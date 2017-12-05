define(["angular", "feed-mgr/feeds/module-name"], function (angular, moduleName) {

    var service = function (FeedService, StepperService) {


        var initializeCreateFeed = function(optionsMetadata,feedStepper, feedModel){

            //setup schedule strategy to be every 5 seconds
            feedModel.schedule.schedulingStrategy="TIMER_DRIVEN";
            feedModel.schedule.schedulingPeriod="25 sec";
            feedModel.schedule.schedulingStrategyTouched =true;

            // disable the schedule form to prevent users from editing
            feedModel.view.schedule.disabled=true

            //disable  the access control step.  users will bypass this step
            var accessControlStep = feedStepper.getStepByName('Access Control');
            if(accessControlStep != null){
                feedStepper.deactivateStep(accessControlStep.index)
            }

        }

        var initializeEditFeed = function(optionsMetadata,feedModel){

            //Prevent users from editing the Feed Definition / General Info section even if they own/can edit the feed
            //feedModel.view.generalInfo.disabled=true;

            //Prevent users from editing the Feed Details / NiFi properties section even if they own/can edit the feed
            //feedModel.view.feedDetails.disabled=true;

            //Prevent users from editing the data policies (standardizers/validators) section even if they own/can edit the feed
            //feedModel.view.dataPolicies.disabled=true;

            //Prevent users from editing the properties section even if they own/can edit the feed
            //feedModel.view.properties.disabled=true;

            //Prevent users from editing the schedule even if they own the feed.
            feedModel.view.schedule.disabled=true;

        }

        var data = {
            initializeCreateFeed:function(optionsMetadata,feedStepper, feedModel){
                  initializeCreateFeed(optionsMetadata,feedStepper,feedModel);
              },
             initializeEditFeed:function(optionsMetadata,feedModel) {
                 initializeEditFeed(optionsMetadata,feedModel);
             }
        }
        return data;
    };


    angular.module(moduleName)
        .factory("ExampleFeedStepperInitializerService", ["FeedService","StepperService", service])
});

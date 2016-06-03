angular.module(MODULE_FEED_MGR).factory('FeedCreationErrorService', function ($mdDialog) {


   function buildErrorMapAndSummaryMessage() {
       var count = 0;
       var errorMap = {"FATAL": [], "WARN": []};
       if (data.feedError.nifiFeed != null) {

           if (data.feedError.nifiFeed.errorMessages != null && data.feedError.nifiFeed.errorMessages.length > 0) {
               angular.forEach(data.feedError.nifiFeed.errorMessages, function (msg) {
                   errorMap['FATAL'].push({category: 'General', message: msg});
                   count++;
               })
           }

           if (data.feedError.nifiFeed.feedProcessGroup != null) {
               angular.forEach(data.feedError.nifiFeed.feedProcessGroup.errors, function (processor) {
                   if (processor.validationErrors) {
                       angular.forEach(processor.validationErrors, function (error) {
                           var copy = {};
                           angular.extend(copy, error);
                           angular.extend(copy, processor);
                           copy.validationErrors = null;
                           errorMap[error.severity].push(copy);
                           count++;
                       });
                   }
               });
           }
          if(errorMap['FATAL'].length ==0) {
              delete errorMap['FATAL'];
          }
           if(errorMap['WARN'].length ==0) {
               delete errorMap['WARN'];
           }
           data.feedError.feedErrorsData = errorMap;
           data.feedError.feedErrorsCount = count;

           if (data.feedError.feedErrorsCount > 0) {

               data.feedError.message = data.feedError.feedErrorsCount + " invalid items were found.  Please review and fix these items.";
               data.feedError.isValid = false;
           }
           else {
               data.feedError.isValid = true;
           }
       }
       else {
           data.feedError.message = 'Error creating feed.'
       }

   }
    function newErrorData() {
        return { isValid:false,
            hasErrors:false,
            feedName:'',
            nifiFeed:{},
            message : '',
            feedErrorsData : {},
            feedErrorsCount : 0
        };
    }

    var data = {
        feedError:{isValid:false,
            hasErrors:false,
            feedName:'',
            nifiFeed:{},
            message : '',
            feedErrorsData : {},
            feedErrorsCount : 0},
        buildErrorData: function(feedName,nifiFeed){
            this.feedError.feedName = feedName;
            this.feedError.nifiFeed = nifiFeed;
            buildErrorMapAndSummaryMessage();
            this.feedError.hasErrors = this.feedError.feedErrorsCount >0;
        },
        reset:function(){
            angular.extend(this.feedError,newErrorData());
        },
        hasErrors:function(){
            return this.feedError.hasErrors;
        },
        showErrorDialog : function() {

        $mdDialog.show({
            controller: 'FeedErrorDialogController',
            templateUrl: 'js/define-feed/feed-error-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose:false,
            fullscreen: true,
            locals : {

            }
        }).then(function(msg) {
                //respond to action in dialog if necessary... currently dont need to do anything
            }, function() {

            });
    }


    };
    return data;



});



(function () {


    var controller = function ($scope, $mdDialog, $mdToast, $http, StateService,CategoriesService, FeedCreationErrorService){
    var self = this;

        var errorData = FeedCreationErrorService.feedError;
    $scope.feedName = errorData.feedName;
    $scope.createdFeed = errorData.nifiFeed;
    $scope.isValid = errorData.isValid;
    $scope.message = errorData.message;
    $scope.feedErrorsData = errorData.feedErrorsData;
    $scope.feedErrorsCount = errorData.feedErrorsCount;

    $scope.fixErrors = function() {
        $mdDialog.hide('fixErrors');
    }


    $scope.hide = function() {
        $mdDialog.hide();
    };

    $scope.cancel = function() {
        $mdDialog.cancel();
    };


};

    angular.module(MODULE_FEED_MGR).controller('FeedErrorDialogController',controller);



}());


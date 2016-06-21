
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            controllerAs: 'vm',
            require:['thinkbigDefineFeedSchedule','^thinkbigStepper'],
            scope: {},
            templateUrl: 'js/define-feed/feed-details/define-feed-schedule.html',
            controller: "DefineFeedScheduleController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    }

    var controller =  function($scope, $http,$mdDialog,$mdToast,RestUrlService, FeedService, StateService,StepperService,   CategoriesService,BroadcastService,FeedCreationErrorService) {

        var self = this;

        BroadcastService.subscribe($scope,StepperService.ACTIVE_STEP_EVENT,onActiveStep)

        this.stepperController = null;
        this.stepNumber = parseInt(this.stepIndex)+1

        this.model = FeedService.createFeedModel;
        //if the Model doesnt support Preconditions dont allow it in the list
        var allScheduleStrategies = [{label:"Cron",value:"CRON_DRIVEN"},{label:"Timer",value:"TIMER_DRIVEN"},{label:"Event",value:"EVENT_DRIVEN"}];

        function updateScheduleStrategies(){
            self.scheduleStrategies = allScheduleStrategies;
            if(!self.model.allowPreconditions){
                self.scheduleStrategies = _.reject(allScheduleStrategies,function(strategy){
                    return strategy.value == 'EVENT_DRIVEN';
                });
            }
        }

        updateScheduleStrategies();

        function onActiveStep(event,index){
            if(index == parseInt(self.stepIndex)) {
                updateScheduleStrategies();
            }
        }



       this.onScheduleStrategyChange = function() {
            if(self.model.schedule.schedulingStrategy == 'CRON_DRIVEN') {
                if(self.model.schedule.schedulingPeriod !="* * * * * ?" ) {
                    self.model.schedule.schedulingPeriod = "* * * * * ?";
                }
            }
            else if(self.model.schedule.schedulingStrategy == 'TIMER_DRIVEN'){
                self.model.schedule.schedulingPeriod = "5 min";
            }
        };
        this.isValid = false;

        function showProgress(){
            if(self.stepperController) {
                self.stepperController.showProgress = true;
            }
        }

        function hideProgress(){
            if(self.stepperController) {
                self.stepperController.showProgress = false;
            }
        }





        this.createdFeed = null;
        this.feedErrorsData = [];
        this.feedErrorsCount = 0;
        this.cronExpressionValid = true;

        this.validateCronExpression = function(){
            if(self.model.schedule.schedulingStrategy == 'CRON_DRIVEN'){
                RestUrlService.validateCronExpression(self.model.schedule.schedulingPeriod).then(function(data){
                    self.cronExpressionValid = data.valid;
                });
            }
        }


        this.deletePrecondition = function($index){
            if(self.model.schedule.preconditions != null){
                self.model.schedule.preconditions.splice($index, 1);
            }
        }
        this.showPreconditionDialog = function() {
            $mdDialog.show({
                controller: 'FeedPreconditionsDialogController',
                templateUrl: 'js/define-feed/feed-details/feed-preconditions/define-feed-preconditions-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    feed:self.model
                }
            })
                .then(function(msg) {


                }, function() {

                });
        };



        this.createFeed = function(){
            showProgress();



            self.createdFeed = null;


            FeedService.saveFeedModel(self.model).then(function(response){
                self.createdFeed = response.data;
                CategoriesService.reload();
                StateService.navigateToDefineFeedComplete(self.createdFeed,null);

              //  self.showCompleteDialog();
            }, function(response){
                self.createdFeed = response.data;
                FeedCreationErrorService.buildErrorData(self.model.feedName,self.createdFeed);
                hideProgress();
                FeedCreationErrorService.showErrorDialog();
            });
        }





    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedScheduleController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDefineFeedSchedule', directive);



    angular.module(MODULE_FEED_MGR).directive('cronExpressionValidator', ['RestUrlService','$q','$http',function (RestUrlService,$q,$http) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, elm, attrs, ctrl) {

                 ctrl.$asyncValidators.cronExpression =function(modelValue,viewValue){
                     var deferred = $q.defer();
                     $http.get(RestUrlService.VALIDATE_CRON_EXPRESSION_URL,{params:{cronExpression:viewValue}}).then(function(response) {

                        if(response.data.valid == false){
                          deferred.reject("Invalid Cron Expression");
                        } else {
                            deferred.resolve()
                        }
                    });
                     return deferred.promise;

            }
        }
    }}]);


})();






(function () {

    var controller = function($scope, $mdDialog, $mdToast, $http, StateService,FeedService, feed){
        $scope.feed = feed;
        $scope.options = [];

        FeedService.getPossibleFeedPreconditions().then(function(response){

                  angular.forEach(response.data,function(opt){
                      $scope.options.push(opt);
                  });
        })

        var arr = feed.schedule.preconditions;

        if(arr != null && arr != undefined)
        {
            $scope.preconditions = angular.copy(arr);
        }
        var modeText = "Add";
        if($scope.preconditions != null && $scope.preconditions.length  && $scope.preconditions.length >0 ){
            modeText = "Edit";
        }

        $scope.title = modeText+" Precondition";


        $scope.pendingEdits = false;

        $scope.ruleType = null;
        $scope.editIndex;

        $scope.editRule;

        $scope.addText = 'ADD PRECONDITION';
        $scope.cancelText = 'CANCEL ADD';

        $scope.editMode = 'NEW';

        function _cancelEdit() {
            $scope.editMode='NEW';
            $scope.addText = 'ADD PRECONDITION';
            $scope.cancelText = 'CANCEL ADD';
            $scope.ruleType = null;
            $scope.editRule = null;
        }


        $scope.cancelEdit = function($event) {
            _cancelEdit();

        }

        $scope.onRuleTypeChange = function() {
            if ($scope.ruleType != null) {
                $scope.editRule = angular.copy($scope.ruleType );
            }
            else {
                $scope.editRule = null;
            }

        }



        $scope.addPolicy = function($event){

            if( $scope.preconditions == null) {
                $scope.preconditions = [];
            }

            if($scope.editMode == 'NEW') {
                $scope.preconditions.push($scope.editRule);
            }
            else if($scope.editMode == 'EDIT') {
                $scope.preconditions[$scope.editIndex] = $scope.editRule;
            }

            $scope.pendingEdits = true;
            feed.schedule.preconditions = $scope.preconditions;
            $mdDialog.hide('done');
        }




        $scope.hide = function($event) {
            _cancelEdit();
            $mdDialog.hide();
        };

        $scope.cancel = function($event) {
            _cancelEdit();
            $mdDialog.hide();
        };


    };

    angular.module(MODULE_FEED_MGR).controller('FeedPreconditionsDialogController',controller);



}());


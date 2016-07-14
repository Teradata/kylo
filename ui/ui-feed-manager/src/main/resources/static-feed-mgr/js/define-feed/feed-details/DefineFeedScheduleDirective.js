
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

        this.timerAmount = 5;
        this.timerUnits = "min";

        //if the Model doesnt support Preconditions dont allow it in the list
        var allScheduleStrategies = [{label: "Cron", value: "CRON_DRIVEN"}, {label: "Timer", value: "TIMER_DRIVEN"}, {label: "Trigger/Event", value: "TRIGGER_DRIVEN"}];

        function updateScheduleStrategies(){
            self.scheduleStrategies = allScheduleStrategies;
            if(!self.model.allowPreconditions){
                self.scheduleStrategies = _.reject(allScheduleStrategies,function(strategy){
                    return strategy.value == 'TRIGGER_DRIVEN';
                });
            }
        }

        var DEFAULT_CRON = "0 0 12 1/1 * ? *";

        function setTimerDriven() {
            self.model.schedule.schedulingStrategy = 'TIMER_DRIVEN';
            self.timerAmount = 5;
            self.timerUnits = "min";
            self.model.schedule.schedulingPeriod = "5 min";
        }

        function setCronDriven() {
            self.model.schedule.schedulingStrategy = 'CRON_DRIVEN'
            self.model.schedule.schedulingPeriod = DEFAULT_CRON;
        }

        function setDefaultScheduleStrategy() {
            if (self.model.inputProcessorType != '' && (self.model.schedule.schedulingStrategy.touched == false || self.model.schedule.schedulingStrategy.touched == undefined)) {
                if (self.model.inputProcessorType.indexOf("GetFile") >= 0) {
                    setTimerDriven();
                }
                else if (self.model.inputProcessorType.indexOf("GetTableData") >= 0) {
                    setCronDriven();
                }
            }
        }
        updateScheduleStrategies();

        function onActiveStep(event,index){
            if(index == parseInt(self.stepIndex)) {
                updateScheduleStrategies();
                setDefaultScheduleStrategy();
            }
        }

        this.timerChanged = function () {
            if (self.timerAmount < 0) {
                self.timerAmount = null;
            }
            if (self.timerAmount != null && (self.timerAmount == 0 || (self.timerAmount < 3 && self.timerUnits == 'sec'))) {
                self.showTimerAlert();
            }
            self.model.schedule.schedulingPeriod = self.timerAmount + " " + self.timerUnits;
            validate();

            //!warn if < 5 seconds
        }


       this.onScheduleStrategyChange = function() {
           self.model.schedule.schedulingStrategy.touched = true;
            if(self.model.schedule.schedulingStrategy == 'CRON_DRIVEN') {
                if (self.model.schedule.schedulingPeriod != DEFAULT_CRON) {
                    setCronDriven();
                }
            }
            else if(self.model.schedule.schedulingStrategy == 'TIMER_DRIVEN'){
                setTimerDriven();
            }
           validate();
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

        self.showTimerAlert = function (ev) {
            $mdDialog.show(
                $mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title('Warning. Rapid Timer')
                    .textContent('Warning.  You have this feed scheduled for a very fast timer.  Please ensure you want this feed scheduled this fast before you proceed.')
                    .ariaLabel('Warning Fast Timer')
                    .ok('Got it!')
                    .targetEvent(ev)
            );
        };


        this.createdFeed = null;
        this.feedErrorsData = [];
        this.feedErrorsCount = 0;

        function validate() {
            //cron expression validation is handled via the cron-expression validator
            var valid = (self.model.schedule.schedulingStrategy == 'CRON_DRIVEN') ||
                        (self.model.schedule.schedulingStrategy == 'TIMER_DRIVEN' && self.timerAmount != undefined && self.timerAmount != null) ||
                        (self.model.schedule.schedulingStrategy == 'TRIGGER_DRIVEN' && self.model.schedule.preconditions != null && self.model.schedule.preconditions.length > 0 );
            self.isValid = valid;
        }

        this.deletePrecondition = function($index){
            if(self.model.schedule.preconditions != null){
                self.model.schedule.preconditions.splice($index, 1);
            }
        }

        this.showPreconditionDialog = function (index) {
            if (index == undefined) {
                index = null;
            }
            $mdDialog.show({
                controller: 'FeedPreconditionsDialogController',
                templateUrl: 'js/define-feed/feed-details/feed-preconditions/define-feed-preconditions-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    feed: self.model,
                    index: index
                }
            })
                .then(function(msg) {
                    validate();

                }, function() {

                });
        };
        validate();


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

    var controller = function ($scope, $mdDialog, $mdToast, $http, StateService, FeedService, feed, index) {
        $scope.feed = feed;
        $scope.options = [];

        FeedService.getPossibleFeedPreconditions().then(function(response){
               angular.forEach(response.data,function(opt){
                      $scope.options.push(opt);
                  });
            ruleTypesAvailable();
        })

        var arr = feed.schedule.preconditions;

        if(arr != null && arr != undefined)
        {
            $scope.preconditions = angular.copy(arr);
        }

        function findRuleType(ruleName) {
            return _.find($scope.options, function (opt) {
                return opt.name == ruleName;
            });
        }

        function ruleTypesAvailable() {
            if ($scope.editRule != null) {
                $scope.ruleType = findRuleType($scope.editRule.name);
            }
        }


        $scope.pendingEdits = false;
        $scope.editRule;
        $scope.ruleType = null;
        $scope.editIndex = null;
        $scope.editMode = 'NEW';
        if (index != null) {
            $scope.editMode = 'EDIT';
            $scope.editIndex = index;
            $scope.editRule = $scope.preconditions[index];
        }
        var modeText = "Add";
        if ($scope.editMode == 'EDIT') {
            modeText = "Edit";
        }

        $scope.title = modeText + " Precondition";


        $scope.addText = 'ADD PRECONDITION';
        $scope.cancelText = 'CANCEL ADD';


        resetChips();

        function _cancelEdit() {
            $scope.editMode='NEW';
            $scope.addText = 'ADD PRECONDITION';
            $scope.cancelText = 'CANCEL ADD';
            $scope.ruleType = null;
            $scope.editRule = null;
            resetChips();
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
        $scope.validateRequiredChips = function (property) {
            var index = _.indexOf($scope.editRule.properties, property);
            if (property.required && property.values.length == 0) {
                //INVALID
                $scope.preconditionForm['property_' + index].$setValidity("required", false);
                $scope.preconditionForm['property_' + index].$setDirty(true);
                return false;
            }
            else {
                $scope.preconditionForm['property_' + index].$setValidity("required", true);
                return true;
            }
        }
        function resetChips(){
            $scope.editChips = {};
            $scope.editChips.selectedItem = null;
            $scope.editChips.searchText = null;
        }

        $scope.validateForm = function () {
            //loop through properties and determine if they are valid
            //
            var validForm = _.some($scope.editRule.properties, function (property) {
                var valid = true;
                var index = _.indexOf($scope.editRule.properties, property);
                if (property.type == 'feedChips' || property.type == 'chips') {
                    valid = $scope.validateRequiredChips(property);
                    property.invalid = !valid
                }
                else if (property.required && (property.value == '' || property.value == '')) {
                    valid = false;
                    $scope.preconditionForm['property_' + index].$setValidity("required", false);
                    $scope.preconditionForm['property_' + index].$setDirty(true);
                    property.invalid = true;
                }
                else {
                    property.invalid = false;
                }
                //sort circuit on truth value so return the opposite to stop the traversing
                return !valid;
            });
            return !validForm;
        }


        $scope.queryChipSearch = function(property,query){
            var options = property.selectableValues;
            var results = query ? options.filter(createFilterFor(query)) : [];
            return results;
        }


        function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(option) {
                return (angular.lowercase(option.value).indexOf(lowercaseQuery) >= 0);
            };
        }


        $scope.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip }
        }

        function buildDisplayString() {
            if ($scope.editRule != null) {
                var str = '';
                _.each($scope.editRule.properties, function (prop, idx) {
                    if (prop.type != 'currentFeed') {
                        //chain it to the display string
                        if (str != '') {
                            str += ';';
                        }
                        str += ' ' + prop.displayName;
                        var val = prop.value;
                        if ((val == null || val == undefined || val == '') && (prop.values != null && prop.values.length > 0)) {
                            val = _.map(prop.values, function (labelValue) {
                                return labelValue.value;
                            }).join(",");
                        }
                        str += ": " + val;
                    }
                });
                $scope.editRule.propertyValuesDisplayString = str;
            }
        }

        $scope.deletePrecondition = function ($event) {
            var index = $scope.editIndex;
            if ($scope.preconditions != null && index != null) {
                $scope.preconditions.splice(index, 1);
            }
            feed.schedule.preconditions = $scope.preconditions;
            $scope.pendingEdits = true;
            $mdDialog.hide('done');
        }

        $scope.addPolicy = function ($event) {

            var validForm = $scope.validateForm();
            if (validForm) {
                if ($scope.preconditions == null) {
                    $scope.preconditions = [];
                }
                buildDisplayString();

                $scope.editRule.ruleType = $scope.ruleType;
                if ($scope.editMode == 'NEW') {
                    $scope.preconditions.push($scope.editRule);
                }
                else if ($scope.editMode == 'EDIT') {
                    $scope.preconditions[$scope.editIndex] = $scope.editRule;

                }

                $scope.pendingEdits = true;
                feed.schedule.preconditions = $scope.preconditions;
                $mdDialog.hide('done');
            }
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


(function () {

    var directive = function () {
        return {
            restrict: "EA",
            require:['thinkbigRegisterSelectTemplate','^thinkbigStepper'],
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/register-template/select-template/select-template.html',
            controller: "RegisterSelectTemplateController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
            }

        };
    }

    var controller =  function($scope, $stateParams,$http,$mdDialog, $mdToast,$timeout,RestUrlService, RegisterTemplateService,StateService, AccessControlService) {

        var self = this;

        this.templates = [];
        this.model = RegisterTemplateService.model;
        this.stepNumber = parseInt(this.stepIndex)+1

        this.template = null;
        this.stepperController = null;


        this.registeredTemplateId = $stateParams.registeredTemplateId || null;
        this.nifiTemplateId = $stateParams.nifiTemplateId || null;

        this.isValid = this.registeredTemplateId !== null;

        /**
         * Error message to be displayed if {@code isValid} is false
         * @type {null}
         */
        this.errorMessage = null;


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

        function findRegisteredTemplateDtoWrapper(){
           return _.find(self.templates,function(template){

                return template.registeredTemplateId != null && template.registeredTemplateId != undefined && self.registeredTemplateId == template.registeredTemplateId;
            })
        }

        /**
         * Gets the templates for the select dropdown
         * @returns {HttpPromise}
         */
        this.getTemplates = function () {
           showProgress();
            var successFn = function (response) {
                   self.templates = response.data;
               hideProgress();
            }
            var errorFn = function (err) {

            }

            var promise = $http.get(RestUrlService.GET_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };



        this.changeTemplate = function(){
            showProgress();
                //Wait for the properties to come back before allowing hte user to go to the next step
                  RegisterTemplateService.loadTemplateWithProperties(null, self.nifiTemplateId).then(function(response) {
                      $timeout(function() {
                          hideProgress();
                      },10);
                      self.isValid = self.model.valid;
                  });
        }


        this.disableTemplate = function(){
            if( self.model.id) {
                RegisterTemplateService.disableTemplate( self.model.id)
            }
        }

        this.enableTemplate = function(){
            if( self.model.id) {
                RegisterTemplateService.enableTemplate( self.model.id)
            }
        }

        function deleteTemplateError(errorMsg){
            // Display error message
            var msg = "<p>The template cannot be deleted at this time.</p><p>";
            msg += angular.isString(errorMsg) ? _.escape(errorMsg) : "Please try again later.";
            msg += "</p>";

            $mdDialog.hide();
            $mdDialog.show(
                $mdDialog.alert()
                    .ariaLabel("Error deleting the template")
                    .clickOutsideToClose(true)
                    .htmlContent(msg)
                    .ok("Got it!")
                    .parent(document.body)
                    .title("Error deleting the template"));
        }

        this.deleteTemplate = function(){
            if( self.model.id) {

                RegisterTemplateService.deleteTemplate( self.model.id).then(function(response){
                    if(response.data && response.data.status =='success') {
                        self.model.state = "DELETED";

                        $mdToast.show(
                            $mdToast.simple()
                                .textContent('Successfully deleted the template ')
                                .hideDelay(3000)
                        );
                        RegisterTemplateService.resetModel();
                        StateService.navigateToRegisteredTemplates();
                    }
                    else {
                        deleteTemplateError(response.data.message)
                    }
                }, function(response){
                   deleteTemplateError(response.data.message)
                });

            }
        }

        /**
         * Displays a confirmation dialog for deleting the feed.
         */
        this.confirmDeleteTemplate = function() {
            var $dialogScope = $scope.$new();
            $dialogScope.dialog = $mdDialog;
            $dialogScope.vm = self;

            $mdDialog.show({
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                scope: $dialogScope,
                templateUrl: "js/register-template/template-delete-dialog.html"
            });
        };







      //  if(this.isValid) {
     //       this.changeTemplate();
       // }

        this.getTemplates();

        AccessControlService.getAllowedActions()
            .then(function(actionSet) {
                self.allowAdmin = AccessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, actionSet.actions);
                self.allowExport = AccessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
            });


    };


    angular.module(MODULE_FEED_MGR).controller('RegisterSelectTemplateController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigRegisterSelectTemplate', directive);

})();





function ControllerServiceNeededDialog($scope, $mdDialog,  $http, StateService, templateName,properties){
    var self = this;

    $scope.properties = properties;
    $scope.templateName = templateName;

    $scope.servicesNeeded = {};

    if(properties ) {
        angular.forEach(properties, function(property,i){
            var service = property.propertyDescriptor.identifiesControllerService;
            if($scope.servicesNeeded[service] == undefined){
                $scope.servicesNeeded[service] = [];
            }
            $scope.servicesNeeded[service].push({name:property.key, processor:property.processorName,description:property.propertyDescriptor.description});
        });
    }


    $scope.hide = function() {
        $mdDialog.hide();
    };

    $scope.cancel = function() {
        $mdDialog.cancel();
    };


};


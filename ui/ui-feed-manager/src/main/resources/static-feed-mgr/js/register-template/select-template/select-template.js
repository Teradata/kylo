/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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


        /**
         * Indicates if admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        /**
         * Indicates if edit operations are allowed.
         * @type {boolean}
         */
        self.allowEdit = false;


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

        function findSelectedTemplate(){
            if( self.nifiTemplateId != undefined ) {
                return _.find(self.templates, function (template) {
                    return template.id == self.nifiTemplateId;
                });
            }
            else {
                return null;
            }
        }

        /**
         * Gets the templates for the select dropdown
         * @returns {HttpPromise}
         */
        this.getTemplates = function () {
            showProgress();
            RegisterTemplateService.getTemplates().then(function(response){
                self.templates = response.data;
                hideProgress();
            });
        };



        this.changeTemplate = function(){
            showProgress();
                //Wait for the properties to come back before allowing hte user to go to the next step
                 var selectedTemplate = findSelectedTemplate();
            var templateName = null;
            if(selectedTemplate != null && selectedTemplate != undefined) {
                templateName = selectedTemplate.name;
            }
                  RegisterTemplateService.loadTemplateWithProperties(null, self.nifiTemplateId,templateName).then(function(response) {
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



        this.getTemplates();

        AccessControlService.getAllowedActions()
            .then(function(actionSet) {
                self.allowEdit = AccessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
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


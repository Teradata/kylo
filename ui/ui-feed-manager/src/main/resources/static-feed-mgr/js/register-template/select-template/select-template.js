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

    var controller =  function($scope, $stateParams,$http,$mdDialog,$timeout,RestUrlService, RegisterTemplateService) {

        var self = this;
        this.templates = [];
        this.model = RegisterTemplateService.model;
        this.stepNumber = parseInt(this.stepIndex)+1

        this.template = null;
        this.stepperController = null;


        this.registeredTemplateId = $stateParams.registeredTemplateId || null;
        this.nifiTemplateId = $stateParams.nifiTemplateId || null;
        this.templateSelectionDisabled = this.registeredTemplateId != null;
        this.templateCache = {};

        this.isValid = this.registeredTemplateId !== null;

        this.controllerServiceNeededProperties = [];

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

        this.showControllerServiceNeededDialog = function() {
            hideProgress();

            $mdDialog.show({
                controller: ControllerServiceNeededDialog,
                templateUrl: 'js/register-template/controller-service-needed-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    templateName:self.model.templateName,
                    properties:self.controllerServiceNeededProperties
                }
            })
                .then(function(msg) {
                    if(msg == 'fixErrors') {
                        //stay here and fix
                    }
                }, function() {

                });
        };


        function assignPropertyRenderType(property) {
            var allowableValues = property.propertyDescriptor.allowableValues;
            if( allowableValues !== undefined && allowableValues !== null && allowableValues.length >0 ){
                 if(allowableValues.length == 2){
                     var list = _.filter(allowableValues,function(value){
                         return (value.value.toLowerCase() == 'false' ||  value.value.toLowerCase() == 'true');
                     });
                     if(list != undefined && list.length == 2) {
                         property.renderTypes = RegisterTemplateService.trueFalseRenderTypes;
                     }
                }
                 if(property.renderTypes == undefined){
                    property.renderTypes = RegisterTemplateService.selectRenderType;
                }
                property.renderType = 'select';
            }
            else {
                property.renderTypes = RegisterTemplateService.propertyRenderTypes;
                property.renderType = 'text';
            }
        }



        function transformPropertiesToArray(properties) {
            var inputProperties = [];
            var additionalProperties = [];
            var inputProcessors  = [];
            var additionalProcessors = [];
            angular.forEach(properties, function (property, i) {
                if(property.processor == undefined){
                    property.processor = {};
                    property.processor.id = property.processorId;
                    property.processor.name = property.processorName;
                    property.processor.type = property.processorType;
                    property.processor.groupId = property.processGroupId;
                    property.processor.groupName = property.processGroupName;
                }

                if(property.selected == undefined){
                    property.selected = false;
                }
                if(property.propertyDescriptor.required == true && ( property.value =='' || property.value ==undefined)) {
                    property.selected = true;
                }

                assignPropertyRenderType(property)

                property.templateValue = property.value;
                property.userEditable = (property.userEditable == undefined || property.userEditable == null) ? true : property.userEditable ;

                if(property.inputProperty){
                    property.mentioId='inputProperty'+property.processorName+'_'+i;
                }
                else {
                    property.mentioId='processorProperty_'+property.processorName+'_'+i;
                }


                //If the Property needs a Controller Service, ensure there is at least 1 to choose from
                if(property.propertyDescriptor.required && property.propertyDescriptor.identifiesControllerService != null && property.propertyDescriptor.identifiesControllerService != '' && ( property.propertyDescriptor.allowableValues == null || property.propertyDescriptor.allowableValues.length ==0 )) {
                    self.isValid = false;
                    self.controllerServiceNeededProperties.push(property)
                }
             //       copyProperty.processor = {id: property.processor.id, name: property.processor.name};
                if(property.inputProperty) {
                    inputProperties.push(property);
                }
                else {
                    additionalProperties.push(property);
                }
            });

            //sort them by processor name and property key
            var inputPropertiesAndProcessors = RegisterTemplateService.sortPropertiesForDisplay(inputProperties);
            inputProperties = inputPropertiesAndProcessors.properties;
            inputProcessors = inputPropertiesAndProcessors.processors;

            var additionalPropertiesAndProcessors = RegisterTemplateService.sortPropertiesForDisplay(additionalProperties);
            additionalProperties = additionalPropertiesAndProcessors.properties;
            additionalProcessors = additionalPropertiesAndProcessors.processors;

            self.model.inputProperties = inputProperties;
            self.model.additionalProperties = additionalProperties;
            self.model.inputProcessors = inputProcessors;
            self.model.additionalProcessors = additionalProcessors;

        }





        this.getTemplateProperties = function() {
            if(self.stepperController) {
                self.stepperController.showProgress = true;
            }
            if(self.model.nifiTemplateId != null) {
                var successFn = function (response) {
                //    self.templateCache[self.model.templateId]= response;
                    var templateData = response.data;
                    $timeout(function() {
                        transformPropertiesToArray(templateData.properties);
                        if(self.stepperController) {
                            self.stepperController.showProgress = false;
                            self.isValid = true;
                        }
                    },10);

                    self.model.nifiTemplateId = templateData.nifiTemplateId;
                    self.model.templateName = templateData.templateName;
                    self.model.defineTable = templateData.defineTable;
                    self.model.allowPreconditions = templateData.allowPreconditions;
                    self.model.dataTransformation = templateData.dataTransformation;
                    self.model.description = templateData.description;
                    self.model.icon.title = templateData.icon;
                    self.model.icon.color = templateData.iconColor;
                    self.model.reusableTemplate = templateData.reusableTemplate;
                    self.model.reusableTemplateConnections = templateData.reusableTemplateConnections;
                    self.model.needsReusableTemplate = templateData.reusableTemplateConnections != undefined && templateData.reusableTemplateConnections.length>0;
                }
                var errorFn = function (err) {

                }
                     var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATE_URL(self.model.nifiTemplateId), {params: {allProperties: true}});
                    promise.then(successFn, errorFn);
                    return promise;
            }
            else {
                var deferred = $q.defer();
                self.properties = [];
                deferred.resolve(self.properties);
                return deferred.promise;
            }
        }

        this.changeTemplate = function(){
            self.isValid = false;
             if(self.registeredTemplateId != null) {
                RegisterTemplateService.resetModel();
                //get the templateId for the registeredTemplateId
                self.model.id = self.registeredTemplateId;
            }
            if(self.nifiTemplateId != null) {
                self.model.nifiTemplateId = self.nifiTemplateId;
            }
            if(self.model.nifiTemplateId != null) {
                self.controllerServiceNeededProperties = [];
                //Wait for the properties to come back before allowing hte user to go to the next step
                  self.getTemplateProperties().then(function(properties) {
                      self.isValid = true;
                  });

            }

        }


        if(this.isValid) {
            this.changeTemplate();
        }

        this.getTemplates();


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


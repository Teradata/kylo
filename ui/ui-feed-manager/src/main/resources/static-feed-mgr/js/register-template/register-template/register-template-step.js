(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/register-template/register-template/register-template-step.html',
            controller: "RegisterCompleteRegistrationController",
            link: function($scope, element, attrs, controller) {
            }
        };
    };

    function RegisterCompleteRegistrationController($scope, $http, $mdToast, $mdDialog, RestUrlService, StateService, RegisterTemplateService) {
        var self = this;

        /**
         * Order of the templates via the dndLists
         * @type {Array}
         */
        this.templateOrder = [];

            /**
         * the angular form
         */
        self.registerTemplateForm = {};

        /**
         * Indicates if the edit form is valid.
         * @type {boolean}
         */
        self.isValid = false;

        self.processorDatasourceDefinitions = [];

        /**
         * flag to tell when the system is loading datasources
         * @type {boolean}
         */
        self.loadingProcessorDatasources = true;

        var buildTemplateProcessorDatasourcesMap = function(){
            self.loadingProcessorDatasources = true;
            var assignedPortIds = [];
            _.each(self.model.reusableTemplateConnections,function(conn) {
                var inputPort = conn.inputPortDisplayName;
                var port = self.connectionMap[inputPort];
                assignedPortIds.push(port.id);
            });
            var selectedPortIds = '';
            if(assignedPortIds.length >0) {
              selectedPortIds = assignedPortIds.join(",");
            }

            RegisterTemplateService.getTemplateProcessorDatasourceDefinitions(self.model.nifiTemplateId,selectedPortIds ).then(function(response){
                var map = {};

                //merge in those already selected/saved on this template
                _.each(response.data,function(def) {
                    def.selectedDatasource = false;
                    if(self.model.registeredDatasourceDefinitions.length == 0) {
                        def.selectedDatasource = true;
                    }
                    else {

                   var matchingTypes = _.filter(self.model.registeredDatasourceDefinitions,function(ds) {
                        return (def.processorType == ds.processorType && ( ds.processorId == def.processorId || ds.processorName == def.processorName));
                    });
                    if(matchingTypes.length >0){
                        def.selectedDatasource = true;
                    }
                  }
                    });
                //sort with SOURCEs first
                self.processorDatasourceDefinitions = _.sortBy(response.data, function (def) {
                    if (def.datasourceDefinition.connectionType == 'SOURCE') {
                        return 1;
                    }
                    else {
                        return 2;
                    }
                });
                self.loadingProcessorDatasources = false;
            });
        };


        this.model = RegisterTemplateService.model;
        this.message = null;
        this.registrationSuccess = false;
        this.stepNumber = parseInt(this.stepIndex) + 1
        this.templateTableOption = this.model.templateTableOption;

        // setup the Stepper types
        if (this.templateTableOption == undefined) {

            if (this.model.defineTable) {
                this.templateTableOption = 'DEFINE_TABLE'
            }
            else if (this.model.dataTransformation) {
                this.templateTableOption = 'DATA_TRANSFORMATION'
            }
            else if (this.model.reusableTemplate) {
                self.templateTableOption = 'COMMON_REUSABLE_TEMPLATE'
            }
            else {
                this.templateTableOption = 'NO_TABLE'
            }

        }
        this.templateTableOptions =
                [{type: 'NO_TABLE', displayName: 'No table customization', description: 'User will not be given option to customize destination table'},
                    {type: 'DEFINE_TABLE', displayName: 'Customize destination table', description: 'Allow users to define and customize the destination data table.'}, {
                    type: 'DATA_TRANSFORMATION',
                    displayName: 'Data Transformation',
                    description: 'Users pick and choose different data tables and columns and apply functions to transform the data to their desired destination table'
                }]

        this.onTableOptionChange = function() {

            if (self.templateTableOption == 'DEFINE_TABLE') {
                self.model.defineTable = true;
                self.model.dataTransformation = false;
            }
            else if (self.templateTableOption == 'DATA_TRANSFORMATION') {
                self.model.defineTable = false;
                self.model.dataTransformation = true;
            }
            else if (self.templateTableOption == 'NO_TABLE') {
                self.model.defineTable = false;
                self.model.dataTransformation = false;
            }
        }

        self.connectionMap = {};
        self.inputPortList = [];
        if (self.model.needsReusableTemplate) {
            RegisterTemplateService.fetchRegisteredReusableFeedInputPorts().then(function(response) {
                // Update connectionMap and inputPortList
                self.inputPortList = [];
                if (response.data) {
                    angular.forEach(response.data, function(port, i) {
                        self.inputPortList.push({label: port.name, value: port.name});
                        self.connectionMap[port.name] = port;
                    });
                }

                // Check for invalid connections
                angular.forEach(self.model.reusableTemplateConnections, function(connection) {
                    //initially mark as valid
                    self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
                    if (!angular.isDefined(self.connectionMap[connection.inputPortDisplayName])) {
                        connection.inputPortDisplayName = null;
                        // self.$error["port-" + connection.feedOutputPortName] = true;
                        //mark as invalid
                        self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", false);
                    }
                });

                buildTemplateProcessorDatasourcesMap();
            });
        }
        else {
            buildTemplateProcessorDatasourcesMap();
        }


        self.onNeedsReusableTemplateConnectionChange = function(connection) {
            var port = self.connectionMap[connection.inputPortDisplayName];
            connection.reusableTemplateInputPortName = port.name;
            //mark as valid
            self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
            buildTemplateProcessorDatasourcesMap();
        };

        this.showIconPicker = function() {
            var iconModel = {icon: self.model.icon.title, iconColor: self.model.icon.color};
            iconModel.name = self.model.templateName;

            $mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/shared/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: iconModel
                }
            })
                    .then(function(msg) {
                        if (msg) {
                            self.model.icon.title = msg.icon;
                            self.model.icon.color = msg.color;
                        }

                    }, function() {

                    });
        };

        this.registerTemplate = function() {
            var successFn = function(response) {
                //toast created!!!
                var message = 'Template Registered with ' + response.data.properties.length + ' properties';
                self.message = message;
                self.registrationSuccess = true;
                $mdToast.show(
                        $mdToast.simple()
                                .textContent(message)
                                .hideDelay(3000)
                );
                self.showCompleteDialog()

            }
            var errorFn = function(err) {
                var message = 'Error Registering Template ' + err;
                self.message = message;
                self.registrationSuccess = false;
                $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000);
                self.showCompleteDialog()
            }

            //get all properties that are selected
            var savedTemplate = RegisterTemplateService.getModelForSave();
            //get template order
            var order = [];
            _.each(self.templateOrder,function(template) {
                order.push(template.id);
            });
            savedTemplate.templateOrder = order;

            var thisOrder = order.length -1;
            if(self.model.id != undefined) {
                 thisOrder = _.indexOf(order, self.model.id)
            }
            else {
                thisOrder = _.indexOf(order,'NEW');
            }
            savedTemplate.order = thisOrder

            //add in the datasources
           var selectedDatasourceDefinitions =  _.filter(self.processorDatasourceDefinitions,function(ds){
                return ds.selectedDatasource == true;
            })

            savedTemplate.registeredDatasourceDefinitions = selectedDatasourceDefinitions;


            var promise = $http({
                url: RestUrlService.REGISTER_TEMPLATE_URL(),
                method: "POST",
                data: angular.toJson(savedTemplate),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
            return promise;
        }

        $scope.$watch(function() {
            return self.model.nifiTemplateId;
        }, function(newVal) {
            if (newVal != null) {
                self.registrationSuccess = false;
            }
        });

        this.showCompleteDialog = function(ev) {

            $mdDialog.show({
                controller: RegistrationCompleteDialogController,
                templateUrl: 'js/register-template/register-template/register-template-complete.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    nifiTemplateId: self.model.nifiTemplateId,
                    templateName: self.model.templateName,
                    message: self.message,
                    registrationSuccess: self.registrationSuccess
                }
            })
                    .then(function(msg) {
                        if (msg == 'explore') {
                            StateService.navigateToFeeds();
                        }
                        if (msg == 'newTemplate') {
                            StateService.navigateToRegisterTemplate();
                        }
                        if (msg == 'newFeed') {
                            StateService.navigateToDefineFeed(self.model.nifiTemplateId);
                        }

                    }, function() {

                    });
        };



        //order list
        RegisterTemplateService.getRegisteredTemplates().then(function(response){

            //order by .order
            var templates = _.sortBy(response.data,'order');
            if(self.model.id ==null || self.model.id == undefined){
                //append to bottom
               templates.push({id:'NEW',name:self.model.templateName,currentTemplate:true});
            }
            else {
                var currentTemplate = _.filter(templates,function(template) {
                    return template.id == self.model.id;
                });
                if(currentTemplate && currentTemplate.length == 1){
                    currentTemplate[0].currentTemplate = true;
                }
            }
            self.templateOrder = templates;
        });


    }

    angular.module(MODULE_FEED_MGR).controller("RegisterCompleteRegistrationController", RegisterCompleteRegistrationController);
    angular.module(MODULE_FEED_MGR).directive("thinkbigRegisterCompleteRegistration", directive);
})();

function RegistrationCompleteDialogController($scope, $mdDialog, $mdToast, $http, StateService, nifiTemplateId, templateName, message, registrationSuccess) {
    $scope.nifiTemplateId = nifiTemplateId;
    $scope.templateName = templateName;
    $scope.message = message;
    $scope.registrationSuccess = registrationSuccess;

    $scope.exploreFeeds = function() {
        $mdDialog.hide('explore');
    };

    $scope.registerNewTemplate = function() {
        $mdDialog.hide('newTemplate');
    };

    $scope.defineNewFeed = function() {
        $mdDialog.hide('newFeed');
    };

    $scope.hide = function() {
        $mdDialog.hide();
    };

    $scope.cancel = function() {
        $mdDialog.cancel();
    };
}

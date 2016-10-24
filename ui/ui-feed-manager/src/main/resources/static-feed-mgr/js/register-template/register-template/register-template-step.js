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
         * the angular form
         */
        self.registerTemplateForm = {};

        /**
         * Indicates if the edit form is valid.
         * @type {boolean}
         */
        self.isValid = false;

        this.model = RegisterTemplateService.model;
        this.message = null;
        this.registrationSuccess = false;
        this.stepNumber = parseInt(this.stepIndex) + 1
        this.templateTableOption = this.model.templateTableOption;
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
            });
        }


        self.onNeedsReusableTemplateConnectionChange = function(connection) {
            var port = self.connectionMap[connection.inputPortDisplayName];
            connection.reusableTemplateInputPortName = port.name;
            //mark as valid
            self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
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

import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";


var directive = function () {
    return {
        restrict: "EA",
        bindToController: {
            stepIndex: '@'
        },
        scope: {},
        controllerAs: 'vm',
        templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-step.html',
        controller: "RegisterCompleteRegistrationController",
        link: function ($scope:any, element:any, attrs:any, controller:any) {
        }
    };
};

export class RegisterCompleteRegistrationController {

    templateOrder:any;
    registerTemplateForm:any;
    isValid:any;
    processorDatasourceDefinitions:any;
    loadingFlowData:any;
    model:any;
    message:any;
    registrationSuccess:any;
    stepNumber:any;
    connectionMap:any;
    inputPortList:any;
    onReusableTemplateConnectionChange:any;
    showIconPicker:any;
    registerTemplate:any;
    showErrorDialog:any;
    stepIndex:any;
    allowUserDefinedFailureProcessors:any;	
    processorType:any;

    constructor(private $scope:any, private $http:any, private $mdToast:any, private $mdDialog:any, private RestUrlService:any
        , private StateService:any, private RegisterTemplateService:any, private EntityAccessControlService:any) {

        /**
         * ref back to this controller
         * @type {RegisterCompleteRegistrationController}
         */
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

        /**
         * The Source/Destination Datasources assigned to this template
         * @type {Array}
         */
        self.processorDatasourceDefinitions = [];

        /**
         * flag to tell when the system is loading datasources
         * @type {boolean}
         */
        self.loadingFlowData = true;

        /**
         * The Template Model
         */
        this.model = RegisterTemplateService.model;

        this.message = null;

        /**
         * Flag indicating if the template succeeded registration
         * @type {boolean}
         */
        this.registrationSuccess = false;
        /**
         * The Step number
         * @type {number}
         */
        this.stepNumber = parseInt(this.stepIndex) + 1

        /**
         * A map of the port names to the port Object
         * used for the connections from the outputs to input ports
         * @type {{}}
         */
        self.connectionMap = {};

        /**
         * The available options in the list of possible inputPorts to connect to
         * @type {Array} of {label: port.name, value: port.name}
         */
        self.inputPortList = [];

        /**
         * Calls the server to get all the Datasources and the Flow processors and flow types
         * Caled initially when the page loads and then any time a user changes a input port connection
         */
        function buildTemplateFlowData() {
            self.loadingFlowData = true;
            var assignedPortIds:any = [];

            _.each(self.model.reusableTemplateConnections, function (conn:any) {
                var inputPort = conn.inputPortDisplayName;
                var port = self.connectionMap[inputPort];
                if (port != undefined) {
                    assignedPortIds.push(port.id);
                }
            });
            var selectedPortIds = '';
            if (assignedPortIds.length > 0) {
                selectedPortIds = assignedPortIds.join(",");
            }
            //only attempt to query if we have connections set
            var hasPortConnections = (self.model.reusableTemplateConnections == null || self.model.reusableTemplateConnections.length ==0 )|| (self.model.reusableTemplateConnections != null && self.model.reusableTemplateConnections.length >0 && assignedPortIds.length ==self.model.reusableTemplateConnections.length);
             if(hasPortConnections) {
                 RegisterTemplateService.getNiFiTemplateFlowInformation(self.model.nifiTemplateId, self.model.reusableTemplateConnections).then(function (response:any) {
                     var map = {};

                     if (response && response.data) {

                         var datasourceDefinitions = response.data.templateProcessorDatasourceDefinitions;
                         self.allowUserDefinedFailureProcessors = response.data.userDefinedFailureProcessors;

                         //merge in those already selected/saved on this template
                         _.each(datasourceDefinitions, function (def:any) {
                             def.selectedDatasource = false;
                             if (self.model.registeredDatasourceDefinitions.length == 0) {
                                 def.selectedDatasource = true;
                             }
                             else {

                                 var matchingTypes = _.filter(self.model.registeredDatasourceDefinitions, function (ds:any) {
                                     return (def.processorType == ds.processorType && ( ds.processorId == def.processorId || ds.processorName == def.processorName));
                                 });
                                 if (matchingTypes.length > 0) {
                                     def.selectedDatasource = true;
                                 }
                             }
                         });
                         //sort with SOURCE's first
                         self.processorDatasourceDefinitions = _.sortBy(datasourceDefinitions, function (def:any) {
                             if (def.datasourceDefinition.connectionType == 'SOURCE') {
                                 return 1;
                             }
                             else {
                                 return 2;
                             }
                         });

                     }
                     self.loadingFlowData = false;
                 });
             }
             else {
                 self.loadingFlowData = false;
             }

        };

        /**
         * Fetches the output/input ports and walks the flow to build the processor graph for the Data sources and the flow types
         */
        var initTemplateFlowData = function () {
            if (self.model.needsReusableTemplate) {
                RegisterTemplateService.fetchRegisteredReusableFeedInputPorts().then(function (response:any) {
                    // Update connectionMap and inputPortList
                    self.inputPortList = [];
                    if (response.data) {
                        angular.forEach(response.data, function (port, i) {
                            self.inputPortList.push({label: port.name, value: port.name, description:port.destinationProcessGroupName});
                            self.connectionMap[port.name] = port;
                        });
                    }

                    // Check for invalid connections
                    angular.forEach(self.model.reusableTemplateConnections, function (connection) {
                        //initially mark as valid
                        self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
                        if (!angular.isDefined(self.connectionMap[connection.inputPortDisplayName])) {
                            connection.inputPortDisplayName = null;
                            //mark as invalid
                            self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", false);
                        }
                    });

                    buildTemplateFlowData();
                });
            }
            else {
                buildTemplateFlowData();
            }
        }

        /**
         * Initialize the connections and flow data
         */
        initTemplateFlowData();

        /**
         * Called when the user changes the output port connections
         * @param connection
         */
        self.onReusableTemplateConnectionChange = function (connection:any) {
            var port = self.connectionMap[connection.inputPortDisplayName];
            connection.reusableTemplateInputPortName = port.name;
            //mark as valid
            self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
            buildTemplateFlowData();
        };

        /**
         * Called when the user clicks to change the icon
         */
        this.showIconPicker = function () {
            var iconModel:any = {icon: self.model.icon.title, iconColor: self.model.icon.color};
            iconModel.name = self.model.templateName;

            $mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/common/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: iconModel
                }
            }).then(function (msg:any) {
                if (msg) {
                    self.model.icon.title = msg.icon;
                    self.model.icon.color = msg.color;
                }

            }, function () {

            });
        };

        /**
         * Called when the user clicks Register
         * @returns {*}
         */
        this.registerTemplate = function () {

            showRegistrationInProgressDialog();
            var successFn = function (response:any) {
                $mdDialog.hide();
                var message = 'Template Registered with ' + response.data.properties.length + ' properties';
                self.registrationSuccess = true;

                $mdToast.show(
                    $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000)
                );
                StateService.FeedManager().Template().navigateToRegisterTemplateComplete(message, self.model, null);
            }
            var errorFn = function (response:any) {
                $mdDialog.hide();
                var message = 'Error Registering Template ' + response.data.message;
                self.registrationSuccess = false;
                $mdToast.show(
                    $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000)
                );
                showErrorDialog(message);
            }

            //get all properties that are selected
            var savedTemplate = RegisterTemplateService.getModelForSave();

            //prepare access control changes if any
            EntityAccessControlService.updateRoleMembershipsForSave(savedTemplate.roleMemberships);

            //get template order
            var order:any = [];
            _.each(self.templateOrder, function (template:any) {
                order.push(template.id);
            });
            savedTemplate.templateOrder = order;

            var thisOrder = order.length - 1;
            if (self.model.id != undefined) {
                thisOrder = _.indexOf(order, self.model.id)
            }
            else {
                thisOrder = _.indexOf(order, 'NEW');
            }
            savedTemplate.order = thisOrder

            //add in the datasources
            var selectedDatasourceDefinitions = _.filter(self.processorDatasourceDefinitions, function (ds:any) {
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

        $scope.$watch(function () {
            return self.model.nifiTemplateId;
        }, function (newVal:any) {
            if (newVal != null) {
                self.registrationSuccess = false;
            }
        });

        /**
         * Shows a dialog with a progress when the registration is in progress
         */
        var showRegistrationInProgressDialog = function () {
            //hide any dialogs
            $mdDialog.hide();
            $mdDialog.show({
                controller: ["$scope", "templateName",RegistrationInProgressDialogController],
                templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-inprogress-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    templateName: self.model.templateName
                }
            })

        }

        /**
         * Shows an Error dialog with a message
         * @param message
         */
        var showErrorDialog = function (message:any) {

            $mdDialog.show({
                controller: ["$scope","$mdDialog","nifiTemplateId","templateName","message",RegistrationErrorDialogController],
                templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-error-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                fullscreen: true,
                locals: {
                    nifiTemplateId: self.model.nifiTemplateId,
                    templateName: self.model.templateName,
                    message: message
                }
            });
        };


    }
    
}

    angular.module(moduleName).controller("RegisterCompleteRegistrationController",["$scope","$http","$mdToast","$mdDialog","RestUrlService","StateService","RegisterTemplateService", "EntityAccessControlService", RegisterCompleteRegistrationController]);
    angular.module(moduleName).controller("RegisterTemplateCompleteController", ["StateService",RegisterTemplateCompleteController]);
angular.module(moduleName).directive("thinkbigRegisterCompleteRegistration", directive);

function RegistrationErrorDialogController($scope:any, $mdDialog:any, nifiTemplateId:any, templateName:any, message:any) {
    $scope.nifiTemplateId = nifiTemplateId;
    $scope.templateName = templateName;
    $scope.message = message;

    $scope.gotIt = function () {
        $mdDialog.cancel();
    };
}

function RegistrationInProgressDialogController($scope:any, templateName:any) {

    $scope.templateName = templateName;
}

function RegisterTemplateCompleteController(StateService:any) {

    var self = this;


    this.gotIt = function () {
        StateService.FeedManager().Template().navigateToRegisteredTemplates();
    }

}


import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/templates/module-name');


export class RegisterTemplateController {

    registeredTemplateId:any;
    nifiTemplateId:any;
    model:any;
    allowAccessControl:any;
    allowAdmin:any;
    allowEdit:any;
    stepperController:any;
    cancelStepper:any;
    onStepperInitialized:any;
    loading:any;
    stepperUrl:any;

    constructor(private $scope:any,private $transition$:any, private $http:any,private $mdToast:any,private $q:any
        ,private RegisterTemplateService:any, private StateService:any, private AccessControlService:any) {

        var self = this;

        /**
         * Reference to the RegisteredTemplate Kylo id passed when editing a template
         * @type {null|*}
         */
        this.registeredTemplateId = $transition$.params().registeredTemplateId || null;

        /**
         * Reference to the NifiTemplate Id. Used if kylo id above is not present
         * @type {null|*}
         */
        this.nifiTemplateId = $transition$.params().nifiTemplateId || null;

        /**
         * The model being edited/created
         */
        this.model = RegisterTemplateService.model;

        this.allowAccessControl = false;

        this.allowAdmin = false;

        this.allowEdit = false;

        /**
         * The Stepper Controller set after initialized
         * @type {null}
         */
        this.stepperController = null;


        self.cancelStepper = function() {
            //or just reset the url
            RegisterTemplateService.resetModel();
            self.stepperUrl = null;
            StateService.FeedManager().Template().navigateToRegisteredTemplates();
        }

        self.onStepperInitialized = function(stepper:any){
            self.stepperController = stepper;
            if(!AccessControlService.isEntityAccessControlled()){
                //disable Access Control
                stepper.deactivateStep(3);
            }
            updateAccessControl();
        }

        function updateAccessControl(){
            if (!self.allowAccessControl && self.stepperController) {
                //deactivate the access control step
                self.stepperController.deactivateStep(3);
            }
            else if (self.stepperController){
                self.stepperController.activateStep(3);
            }
        }


        function init(){
            self.loading = true;
                //Wait for the properties to come back before allowing the user to go to the next step
                RegisterTemplateService.loadTemplateWithProperties(self.registeredTemplateId, self.nifiTemplateId).then(function(response:any) {
                    self.loading = false;
                    RegisterTemplateService.warnInvalidProcessorNames();
                    $q.when(RegisterTemplateService.checkTemplateAccess()).then(function(response:any) {
                      if(!response.isValid) {
                          //PREVENT access
                      }
                        self.allowAccessControl = response.allowAccessControl;
                        self.allowAdmin = response.allowAdmin;
                        self.allowEdit = response.allowEdit;
                         updateAccessControl();

                    });
                },function(err:any){
                    self.loading = false;
                    RegisterTemplateService.resetModel();
                    self.allowAccessControl = false;
                    self.allowAdmin = false;
                    self.allowEdit = false;
                    updateAccessControl();
                });
        }
        init();

    }

}
angular.module(moduleName).controller('RegisterTemplateController',["$scope","$transition$","$http","$mdToast","$q","RegisterTemplateService","StateService","AccessControlService",RegisterTemplateController]);

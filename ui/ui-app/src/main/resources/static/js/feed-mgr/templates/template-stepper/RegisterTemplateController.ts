import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../module-name";


export class RegisterTemplateController {

    registeredTemplateId:any;
    nifiTemplateId:any;
    model:any;
    allowAccessControl:any;
    allowAdmin:any;
    allowEdit:any;
    stepperController:any;
    loading:any;
    stepperUrl:any;

    constructor(private $scope:any,private $transition$:any, private $http:any,private $mdToast:any,private $q:any
        ,private RegisterTemplateService:any, private StateService:any, private AccessControlService:any, private BroadcastService:any) {
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
        this.init();
    }

    init=()=>{
            this.loading = true;
                //Wait for the properties to come back before allowing the user to go to the next step
                this.RegisterTemplateService.loadTemplateWithProperties(this.registeredTemplateId, this.nifiTemplateId).then((response:any)=>{
                    this.loading = false;
                    this.RegisterTemplateService.warnInvalidProcessorNames();
                    this.$q.when(this.RegisterTemplateService.checkTemplateAccess()).then((response:any)=> {
                      if(!response.isValid) {
                          //PREVENT access
                      }
                        this.allowAccessControl = response.allowAccessControl;
                        this.allowAdmin = response.allowAdmin;
                        this.allowEdit = response.allowEdit;
                         this.updateAccessControl();
                         this.BroadcastService.notify("REGISTERED_TEMPLATE_LOADED","LOADED");

                    });
                },(err:any)=>{
                    this.loading = false;
                    this.RegisterTemplateService.resetModel();
                    this.allowAccessControl = false;
                    this.allowAdmin = false;
                    this.allowEdit = false;
                    this.updateAccessControl();
                });
        }
        updateAccessControl=()=>{
            if (!this.allowAccessControl && this.stepperController) {
                //deactivate the access control step
                this.stepperController.deactivateStep(3);
            }
            else if (this.stepperController){
                this.stepperController.activateStep(3);
            }
        }
        cancelStepper= ()=> {
            //or just reset the url
            this.RegisterTemplateService.resetModel();
            this.stepperUrl = null;
            this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
        }

        onStepperInitialized = (stepper:any)=>{
            this.stepperController = stepper;
            if(!this.AccessControlService.isEntityAccessControlled()){
                //disable Access Control
                stepper.deactivateStep(3);
            }
            this.updateAccessControl();
        }

}
angular.module(moduleName).controller('RegisterTemplateController',["$scope","$transition$","$http","$mdToast","$q","RegisterTemplateService","StateService","AccessControlService","BroadcastService",RegisterTemplateController]);

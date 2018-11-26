import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";
import {AccessControlService} from '../../../../services/AccessControlService';
import {StateService} from '@uirouter/core';
import {RegisterTemplateServiceFactory} from '../../../services/RegisterTemplateServiceFactory';
import {BroadcastService} from '../../../../services/broadcast-service';
import {UiComponentsService} from '../../../services/UiComponentsService';


export class RegisterSelectTemplateController {


    templates: any = [];
    model: any;
    stepNumber: any;
    stepIndex: any;
    template: any = null;
    stepperController: any = null;
    registeredTemplateId: any;
    nifiTemplateId: any;
    isValid: any;
    isNew: boolean;

    /**
     * Error message to be displayed if {@code isValid} is false
     * @type {null}
     */
    errorMessage: any = null;
    /**
     * Indicates if admin operations are allowed.
     * @type {boolean}
     */
    allowAdmin: any = false;
    /**
     * Indicates if edit operations are allowed.
     * @type {boolean}
     */
    allowEdit: any = false;
    /**
     * Flag to indicate the template is loading
     * Used for PRogress
     * @type {boolean}
     */
    loadingTemplate: boolean = false;
    /**
     * Flag to indicate the select template list is loading
     * @type {boolean}
     */
    fetchingTemplateList: boolean = false;
    templateTableOptions: any;
    allowAccessControl: any;
    allowExport: any;
    templateNavigationLinks: any;

    static readonly $inject = ["$scope", "$http", "$mdDialog", "$mdToast", "$timeout", "$q", "$state", "RestUrlService", "RegisterTemplateService", "StateService",
        "AccessControlService", "EntityAccessControlService", "UiComponentsService", "AngularModuleExtensionService", "BroadcastService"];

    constructor(private $scope: any, private $http: angular.IHttpService, private $mdDialog: angular.material.IDialogService, private $mdToast: angular.material.IToastService, private $timeout: angular.ITimeoutService
        , private $q: angular.IQService, private $state: StateService, private RestUrlService: any, private registerTemplateService: RegisterTemplateServiceFactory, private StateService: any
        , private accessControlService: AccessControlService, private EntityAccesControlService: any, private uiComponentsService: UiComponentsService
        , private AngularModuleExtensionService: any, private broadcastService: BroadcastService) {


        this.model = this.registerTemplateService.model;

        this.registeredTemplateId = this.model.id;
        this.isNew = (this.model.nifiTemplateId == undefined);
        this.nifiTemplateId = this.model.nifiTemplateId;

        this.isValid = this.registeredTemplateId !== null;


        this.templateNavigationLinks = AngularModuleExtensionService.getTemplateNavigation();

        /**
         * The possible options to choose how this template should be displayed in the Feed Stepper
         * @type {Array.<TemplateTableOption>}
         */
        this.templateTableOptions = [{type: 'NO_TABLE', displayName: 'No table customization', description: 'User will not be given option to customize destination table'}];
        this.uiComponentsService.getTemplateTableOptions()
            .then((templateTableOptions: any) => {
                Array.prototype.push.apply(this.templateTableOptions, templateTableOptions);
            });

        /**
         * Get notified when a already registered template is selected and loaded from the previous screen
         */
        this.broadcastService.subscribe($scope, "REGISTERED_TEMPLATE_LOADED", () => this.onRegisteredTemplateLoaded());

        $scope.$watch(() => {
            return this.model.loading;
        }, (newVal: any) => {
            if (newVal === false) {
                this.initTemplateTableOptions();
                this.hideProgress();
            }
        });

        this.getTemplates();

        accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = accessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                this.allowAdmin = accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, actionSet.actions);
                this.allowExport = accessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
            });
    };

    // setup the Stepper types
    initTemplateTableOptions = () => {
        if (this.model.templateTableOption == null) {

            if (this.model.defineTable) {
                this.model.templateTableOption = 'DEFINE_TABLE'
            } else if (this.model.dataTransformation) {
                this.model.templateTableOption = 'DATA_TRANSFORMATION'
            } else if (this.model.reusableTemplate) {
                this.model.templateTableOption = 'COMMON_REUSABLE_TEMPLATE'
            } else {
                this.model.templateTableOption = 'NO_TABLE'
            }
        }
    };

    changeTemplate = () => {
        this.errorMessage = null;
        this.loadingTemplate = true;
        this.showProgress();
        //Wait for the properties to come back before allowing hte user to go to the next step
        var selectedTemplate = this.findSelectedTemplate();
        var templateName = null;
        if (selectedTemplate != null && selectedTemplate != undefined) {
            templateName = selectedTemplate.name;
        }
        this.registerTemplateService.loadTemplateWithProperties(null, this.nifiTemplateId, templateName).then((response: any) => {
            this.registerTemplateService.warnInvalidProcessorNames();
            this.$q.when(this.registerTemplateService.checkTemplateAccess()).then((accessResponse: any) => {
                this.isValid = accessResponse.isValid;
                this.allowAdmin = accessResponse.allowAdmin;
                this.allowEdit = accessResponse.allowEdit;
                this.allowAccessControl = accessResponse.allowAccessControl;
                if (!accessResponse.isValid) {
                    //PREVENT access
                    this.errorMessage = "Access Denied.  You are unable to edit the template. ";
                }
                else {
                    if (!this.allowAccessControl) {
                        //deactivate the access control step
                        this.stepperController.deactivateStep(3);
                    }
                    else {
                        this.stepperController.activateStep(3);
                    }
                }
                this.loadingTemplate = false;
                this.hideProgress();
            });


        }, (err: any) => {
            this.registerTemplateService.resetModel();
            this.errorMessage = (angular.isDefined(err.data) && angular.isDefined(err.data.message)) ? err.data.message : "An Error was found loading this template.  Please ensure you have access to edit this template."
            this.loadingTemplate = false;
            this.hideProgress();
        });
    }

    disableTemplate = () => {
        if (this.model.id) {
            this.registerTemplateService.disableTemplate(this.model.id)
        }
    }

    enableTemplate = () => {
        if (this.model.id) {
            this.registerTemplateService.enableTemplate(this.model.id)
        }
    }

    deleteTemplateError = (errorMsg: any) => {
        // Display error message
        var msg = "<p>The template cannot be deleted at this time.</p><p>";
        msg += angular.isString(errorMsg) ? _.escape(errorMsg) : "Please try again later.";
        msg += "</p>";

        this.$mdDialog.hide();
        this.$mdDialog.show(
            this.$mdDialog.alert()
                .ariaLabel("Error deleting the template")
                .clickOutsideToClose(true)
                .htmlContent(msg)
                .ok("Got it!")
                .parent(document.body)
                .title("Error deleting the template"));
    }

    deleteTemplate = () => {
        if (this.model.id) {

            this.registerTemplateService.deleteTemplate(this.model.id).then((response: any) => {
                if (response.data && response.data.status == 'success') {
                    this.model.state = "DELETED";

                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Successfully deleted the template ')
                            .hideDelay(3000)
                    );
                    this.registerTemplateService.resetModel();
                    this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
                }
                else {
                    this.deleteTemplateError(response.data.message)
                }
            }, (response: any) => {
                this.deleteTemplateError(response.data.message)
            });
        }
    }

    /**
     * Displays a confirmation dialog for deleting the feed.
     */
    confirmDeleteTemplate = () => {
        var $dialogScope = this.$scope.$new();
        $dialogScope.dialog = this.$mdDialog;
        $dialogScope.vm = this;

        this.$mdDialog.show({
            escapeToClose: false,
            fullscreen: true,
            parent: angular.element(document.body),
            scope: $dialogScope,
            templateUrl: "./template-delete-dialog.html"
        });
    };


    publishTemplate = (overwriteParam: boolean) => {
        if (this.model.id) {

            this.$http.get("/proxy/v1/repository/templates/publish/" + this.model.id + "?overwrite=" + overwriteParam).then((response: any) => {
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent('Successfully published template to repository.')
                        .hideDelay(3000)
                );
                this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
            }, (response: any) => {
                this.publishTemplateError(response.data.message)
            });

        }
    }

    publishTemplateError = (errorMsg: any) => {
        // Display error message
        var msg = "<p>Template could not be published.</p><p>";
        msg += angular.isString(errorMsg) ? _.escape(errorMsg) : "Please try again later.";
        msg += "</p>";

        this.$mdDialog.hide();
        this.$mdDialog.show(
            this.$mdDialog.alert()
                .ariaLabel("Error publishing the template to repository")
                .clickOutsideToClose(true)
                .htmlContent(msg)
                .ok("Got it!")
                .parent(document.body)
                .title("Error publishing the template to repository"));
    }

    /**
     * Called when the user changes the radio buttons
     */
    onTableOptionChange = () => {
        if (this.model.templateTableOption === 'DEFINE_TABLE') {
            this.model.defineTable = true;
            this.model.dataTransformation = false;
        } else if (this.model.templateTableOption === 'DATA_TRANSFORMATION') {
            this.model.defineTable = false;
            this.model.dataTransformation = true;
        } else {
            this.model.defineTable = false;
            this.model.dataTransformation = false;
        }
    };


    showProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = true;
        }
    }


    hideProgress() {
        if (this.stepperController && !this.isLoading()) {
            this.stepperController.showProgress = false;
        }
    }

    findSelectedTemplate() {
        if (this.nifiTemplateId != undefined) {
            return _.find(this.templates, (template: any) => {
                return template.id == this.nifiTemplateId;
            });
        }
        else {
            return null;
        }
    }

    isLoading = () => {
        return this.loadingTemplate || this.fetchingTemplateList || this.model.loading;
    }

    /**
     * Navigate the user to the state
     * @param link
     */
    templateNavigationLink = (link: any) => {
        var templateId = this.registeredTemplateId;
        var templateName = this.model.templateName;
        this.$state.go(link.sref, {templateId: templateId, templateName: templateName, model: this.model});
    }

    /**
     * Gets the templates for the select dropdown
     * @returns {HttpPromise}
     */
    getTemplates = () => {
        this.fetchingTemplateList = true;
        this.showProgress();
        this.registerTemplateService.getTemplates().then((response: any) => {
            this.templates = response.data;
            this.fetchingTemplateList = false;
            this.matchNiFiTemplateIdWithModel();
            this.hideProgress();
        });
    };

    /**
     * Ensure that the value for the select list matches the model(if a model is selected)
     */
    matchNiFiTemplateIdWithModel() {
        if (!this.isLoading() && this.model.nifiTemplateId != this.nifiTemplateId) {
            var matchingTemplate = this.templates.find((template: any) => {
                var found = angular.isDefined(template.templateDto) ? template.templateDto.id == this.model.nifiTemplateId : template.id == this.model.nifiTemplateId;
                if (!found) {
                    //check on template name
                    found = this.model.templateName == template.name;
                }
                return found;
            });
            if (angular.isDefined(matchingTemplate)) {
                this.nifiTemplateId = matchingTemplate.templateDto.id;
            }
        }
    }

    /**
     * Called either after the the template has been selected from the previous screen, or after the template select list is loaded
     */
    onRegisteredTemplateLoaded() {
        this.matchNiFiTemplateIdWithModel();
    }


    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        this.stepNumber = parseInt(this.stepIndex) + 1;
        if (this.isLoading()) {
            this.stepperController.showProgress = true;
        }
    }

}

angular.module(moduleName).component('thinkbigRegisterSelectTemplate', {
    bindings: {
        stepIndex: '@',
        nifiTemplateId: '=?',
        registeredTemplateId: "=?"
    },
    controllerAs: 'vm',
    templateUrl: './select-template.html',
    controller: RegisterSelectTemplateController,
    require: {
        stepperController: "^thinkbigStepper"
    }
});


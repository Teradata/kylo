import {HttpClient} from "@angular/common/http";
import {Component, EventEmitter, Input, OnInit, Output, ViewContainerRef} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TdDialogService} from '@covalent/core/dialogs';
import * as angular from 'angular';
import * as _ from "underscore";

import AccessControlService from '../../../../services/AccessControlService';
import AngularModuleExtensionService from '../../../../services/AngularModuleExtensionService';
import BroadcastService from '../../../../services/broadcast-service';
import StateService from '../../../../services/StateService';
import {RegisterTemplateServiceFactory} from '../../../services/RegisterTemplateServiceFactory';
import {RestUrlService} from '../../../services/RestUrlService';
import {UiComponentsService} from '../../../services/UiComponentsService';
import {EntityAccessControlService} from '../../../shared/entity-access-control/EntityAccessControlService';
import {TemplateDeleteDialog} from './template-delete-dialog.component';

@Component({
    selector: 'thinkbig-register-select-template',
    templateUrl: 'js/feed-mgr/templates/template-stepper/select-template/select-template.html'
})
export class RegisterSelectTemplateController implements OnInit {

    @Input() registeredTemplateId: string;
    @Input() nifiTemplateId: string;
    @Input() $new: any;

    @Output() onCancelStepper = new EventEmitter();

    templates: any = [];
    model: any;
    stepIndex: string;
    stepNumber: number;
    stepperController: any = null;
    template: any = null;
    isValid: boolean = false;

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

    @Input() formGroup: FormGroup;

    ngOnInit() {

        this.formGroup.addControl("template", new FormControl(null, Validators.required));

        this.model = this.registerTemplateService.model;

        this.registeredTemplateId = this.model.id;
        this.nifiTemplateId = this.model.nifiTemplateId;

        this.isValid = this.registeredTemplateId !== null;

        this.templateNavigationLinks = this.AngularModuleExtensionService.getTemplateNavigation();

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
        this.broadcastService.subscribe(null, "REGISTERED_TEMPLATE_LOADED", this.onRegisteredTemplateLoaded());

        // TODO: line should be removed once error in service response success function is fixed
        // this.initTemplateTableOptions();
        this.registerTemplateService.modelLoadingObserver.subscribe((loading: boolean) => {
            if (!loading) {
                this.initTemplateTableOptions();
            }
        });

        this.getTemplates();

        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                this.allowAdmin = this.accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, actionSet.actions);
                this.allowExport = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
            });

        this.stepNumber = parseInt(this.stepIndex) + 1;
        if (this.isLoading()) {
            this.stepperController.showProgress = true;
        }
    }

    constructor(private $state: StateService,
                private RestUrlService: RestUrlService,
                private registerTemplateService: RegisterTemplateServiceFactory,
                private StateService: StateService,
                private accessControlService: AccessControlService,
                private EntityAccesControlService: EntityAccessControlService,
                private uiComponentsService: UiComponentsService,
                private AngularModuleExtensionService: AngularModuleExtensionService,
                private broadcastService: BroadcastService,
                private dialog: MatDialog,
                private snackBar: MatSnackBar,
                private _dialogService: TdDialogService,
                private _viewContainerRef: ViewContainerRef,
                private http: HttpClient
                /*@Inject("$injector") private $injector: any*/) {
    }

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
            this.registerTemplateService.checkTemplateAccess().then((accessResponse: any) => {
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
                        // this.stepperController.deactivateStep(3);
                    }
                    else {
                        // this.stepperController.activateStep(3);
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

        this._dialogService.closeAll();
        this._dialogService.openAlert({
            message: msg,
            viewContainerRef: this._viewContainerRef,
            title: "Error deleting the template",
            closeButton: 'Got it!',
            ariaLabel: "Error deleting the template",
            closeOnNavigation: true,
            disableClose: false
        });
    }

    deleteTemplate = () => {
        if (this.model.id) {

            this.registerTemplateService.deleteTemplate(this.model.id).then((response: any) => {
                if (response.data && response.data.status == 'success') {
                    this.model.state = "DELETED";

                    this.snackBar.open("Successfully deleted the template ", "OK", {duration: 3000});
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

        let dialogRef = this.dialog.open(TemplateDeleteDialog, {
            data: {model: this.model},
            panelClass: "full-screen-dialog"
        });

        let dialogRefObserver = dialogRef.componentInstance.onDeleteTemplate.subscribe(() => {
            this.deleteTemplate();
            dialogRef.close();
        });

    };


    publishTemplate = (overwriteParam: boolean) => {
        if (this.model.id) {

            this.http.get("/proxy/v1/repository/templates/publish/" + this.model.id + "?overwrite=" + overwriteParam).toPromise().then((response: any) => {
                this.snackBar.open('Successfully published template to repository.', null, {duration: 3000});
                this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
            }, (response: any) => {
                this.publishTemplateError(response.data.message)
            });

        }
    }

    publishTemplateError = (errorMsg: any) => {
        // Display error message
        var msg = "Template could not be published. ";
        msg += angular.isString(errorMsg) ? _.escape(errorMsg) : "Please try again later.";

        this._dialogService.openAlert({
            ariaLabel: "Error publishing the template to repository",
            closeButton: "Got it!",
            message: msg,
            title: "Error publishing the template to repository"
        });
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
        // if (this.stepperController) {
        //     this.stepperController.showProgress = true;
        // }
    }


    hideProgress() {
        // if (this.stepperController && !this.isLoading()) {
        //     this.stepperController.showProgress = false;
        // }
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
            this.templates = response;
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
            var matchingTemplate = this.templates.find(function (template: any) {
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

    cancelStepper() {
        this.onCancelStepper.emit();
    }

}

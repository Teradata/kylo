import * as angular from 'angular';
import { moduleName } from '../module-name';
import * as _ from 'underscore';
import SlaEmailTemplateService from "./SlaEmailTemplateService";
import { IScope } from '@angular/upgrade/static/src/common/angular1';
import { Transition } from "@uirouter/core";
import AccessControlService from '../../../services/AccessControlService';
import { DefaultPaginationDataService } from '../../../services/PaginationDataService';
import { DefaultTableOptionsService } from '../../../services/TableOptionsService';
import AddButtonService from '../../../services/AddButtonService';

export class controller implements ng.IComponentController {

    $transition$: Transition;
    /**
* Indicates if templates are allowed to be edited.
* @type {boolean}
*/
    allowEdit: boolean = false;

    templates: any[] = [];
    loading: boolean = true;
    cardTitle: string = 'SLA Email Templates';

    //Pagination DAta
    pageName: string = "sla-email-templates";
    paginationData: any;
    paginationId: string = 'sla-email-templates';
    currentPage: any;
    viewType: any;
    sortOptions: any;
    filter: any;

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {

        this.paginationData = this.paginationDataService.paginationData(this.pageName);
        this.currentPage = this.paginationDataService.currentPage(this.pageName) || 1;
        this.viewType = this.paginationDataService.viewType(this.pageName);
        this.sortOptions = this.loadSortOptions();

        this.filter = this.paginationDataService.filter(this.pageName);

        // Register Add button
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions)) {
                    this.addButtonService.registerAddButton("sla-email-templates", () => {
                        this.slaEmailTemplateService.newTemplate();
                        this.StateService.FeedManager().Sla().navigateToNewEmailTemplate();
                    });
                }
            });

        this.paginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);

        this.$scope.$watch(() => {
            return this.viewType;
        }, (newVal: any) => {
            this.onViewTypeChange(newVal);
        });

        this.$scope.$watch(() => {
            return this.filter;
        }, (newVal: any) => {
            this.paginationDataService.filter(this.pageName, newVal)
        });
        this.getExistingTemplates();

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.accessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
            });
    }

    static readonly $inject = ["$scope", "$http", "$mdDialog", "$q",
        "AccessControlService", "DefaultPaginationDataService",
        "DefaultTableOptionsService", "AddButtonService", "StateService",
        "SlaEmailTemplateService"];


    constructor(private $scope: IScope,
        private $http: angular.IHttpService,
        private $mdDialog: angular.material.IDialogService,
        private $q: angular.IQService,
        private accessControlService: AccessControlService,
        private paginationDataService: DefaultPaginationDataService,
        private tableOptionsService: DefaultTableOptionsService,
        private addButtonService: AddButtonService,
        private StateService: any,
        private slaEmailTemplateService: SlaEmailTemplateService) {


    }

    /**
        * Build the possible Sorting Options
        * @returns {*[]}
        */
    loadSortOptions = () => {
        var options = { 'Template': 'name' };
        var sortOptions = this.tableOptionsService.newSortOptions(this.pageName, options, 'templateName', 'asc');
        this.tableOptionsService.initializeSortOption(this.pageName);
        return sortOptions;
    }
    onViewTypeChange = (viewType: any) => {
        this.paginationDataService.viewType(this.pageName, this.viewType);
    }

    onOrderChange = (order: any) => {
        this.paginationDataService.sort(this.pageName, order);
        this.tableOptionsService.setSortOption(this.pageName, order);
    };

    onPaginationChange = (page: any, limit: any) => {
        this.paginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption = (option: any) => {
        var sortString = this.tableOptionsService.toSortString(option);
        this.paginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.tableOptionsService.toggleSort(this.pageName, option);
        this.tableOptionsService.setSortOption(this.pageName, sortString);
    }



    /**
     * Displays the details of the specified template.
     *
     * @param event
     * @param template
     */
    editTemplate = (event: any, template: any) => {
        if (this.allowEdit && template != undefined) {
            this.slaEmailTemplateService.template = template;
            this.StateService.FeedManager().Sla().navigateToNewEmailTemplate(template.id);

        } else {
            this.slaEmailTemplateService.accessDeniedDialog();
        }
    };

    getExistingTemplates = () => {

        var successFn = (response: any) => {
            this.loading = false;
            this.templates = response.data;
        }
        var errorFn = (err: any) => {
            this.loading = false;
        }
        var promise = this.slaEmailTemplateService.getExistingTemplates()
        promise.then(successFn, errorFn);
        return promise;
    }
}

angular.module(moduleName)
    .component('slaEmailTemplatesController', {
        templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-templates.html',
        controller: controller,
        controllerAs: "vm"
    });

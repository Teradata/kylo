
import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "./module-name";
import {AddButtonService} from '../../services/AddButtonService';
import { DomainTypesService } from '../services/DomainTypesService';
import "./module-require";
/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME = "domain-types";
export class DomainTypesController {

    /**
    * List of domain types.
    * @type {DomainType[]}
    */
    domainTypes: any[] = [];
    /**
    * Indicates that the table data is being loaded.
    * @type {boolean}
    */
    loading: boolean = true;
    /**
    * Query for filtering categories.
    * @type {string}
    */
    searchQuery: string = "";

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        // Register Add button
        this.addButtonService.registerAddButton(PAGE_NAME, () => {
            this.StateService.FeedManager().DomainType().navigateToDomainTypeDetails();
        });

        // Fetch domain types
        this.domainTypesService.findAll()
            .then((domainTypes: any) => {
                this.domainTypes = domainTypes;
                this.loading = false;
            }, () => {
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent("Unable to load domain types.")
                        .hideDelay(3000)
                );
            });
    }

    static readonly $inject = ["AddButtonService", "DomainTypesService", "FeedFieldPolicyRuleService", "StateService", "$mdToast"];
    /**
     * Controller for the domain-types page.
     *
     * @constructor
     */
    constructor(private addButtonService: AddButtonService, private domainTypesService: DomainTypesService, private FeedFieldPolicyRuleService: any,
        private StateService: any, private $mdToast: angular.material.IToastService) {

    }
    /**
 * Navigates to the domain type details page for the specified domain type.
 */
    editDomainType = (domainType: any) => {
        this.StateService.FeedManager().DomainType().navigateToDomainTypeDetails(domainType.id);
    };

    /**
     * Gets a list of all field policies for the specified domain type.
     */
    getAllFieldPolicies = (domainType: any) => {
        var rules = this.FeedFieldPolicyRuleService.getAllPolicyRules(domainType.fieldPolicy);
        return (rules.length > 0) ? rules.map(_.property("name")).join(", ") : "No rules";
    };

    /**
     * Indicates if the specified domain type has any field policies.
     */
    hasFieldPolicies = (domainType: any) => {
        return (domainType.fieldPolicy.standardization.length > 0 || domainType.fieldPolicy.validation.length > 0);
    };

}
// Register the controller
const module = angular.module(moduleName).component("domainTypesController", {
    templateUrl: "./domain-types.html",
    controller: DomainTypesController,
    controllerAs: "vm"
});
export default module;
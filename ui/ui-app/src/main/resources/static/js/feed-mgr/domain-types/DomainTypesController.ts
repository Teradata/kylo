
import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";

/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME = "domain-types";
export class DomainTypesController {


    domainTypes:any;
    loading:any;
    searchQuery:any;
    editDomainType:any;
    getAllFieldPolicies:any;
    hasFieldPolicies:any;

    /**
     * Controller for the domain-types page.
     *
     * @constructor
     */
    constructor(private AddButtonService:any, private DomainTypesService:any, private FeedFieldPolicyRuleService:any, private StateService:any, private $mdToast:any) {
        var self = this;

        /**
         * List of domain types.
         * @type {DomainType[]}
         */
        self.domainTypes = [];

        /**
         * Indicates that the table data is being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Query for filtering categories.
         * @type {string}
         */
        self.searchQuery = "";

        /**
         * Navigates to the domain type details page for the specified domain type.
         */
        self.editDomainType = function (domainType:any) {
            StateService.FeedManager().DomainType().navigateToDomainTypeDetails(domainType.id);
        };

        /**
         * Gets a list of all field policies for the specified domain type.
         */
        self.getAllFieldPolicies = function (domainType:any) {
            var rules = FeedFieldPolicyRuleService.getAllPolicyRules(domainType.fieldPolicy);
            return (rules.length > 0) ? rules.map(_.property("name")).join(", ") : "No rules";
        };

        /**
         * Indicates if the specified domain type has any field policies.
         */
        self.hasFieldPolicies = function (domainType:any) {
            return (domainType.fieldPolicy.standardization.length > 0 || domainType.fieldPolicy.validation.length > 0);
        };

        // Register Add button
        AddButtonService.registerAddButton(PAGE_NAME, function () {
            StateService.FeedManager().DomainType().navigateToDomainTypeDetails();
        });

        // Fetch domain types
        DomainTypesService.findAll()
            .then(function (domainTypes:any) {
                self.domainTypes = domainTypes;
                self.loading = false;
            }, function () {
                $mdToast.show(
                    $mdToast.simple()
                        .textContent("Unable to load domain types.")
                        .hideDelay(3000)
                );
            });
    }

}
// Register the controller
angular.module(moduleName).controller("DomainTypesController", ["AddButtonService", "DomainTypesService", "FeedFieldPolicyRuleService", "StateService", "$mdToast", DomainTypesController]);

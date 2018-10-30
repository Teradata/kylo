import {Transition} from "@uirouter/core";
import * as angular from 'angular';
import {AccessControlService} from '../../services/AccessControlService';

const moduleName = require('./module-name');

import './module-require';

export class CategoryDetailsController {

    public $transition$: Transition;

    /**
    * Indicates if the category is currently being loaded.
    * @type {boolean} {@code true} if the category is being loaded, or {@code false} if it has finished loading
    */
    loadingCategory: boolean = true;
    showAccessControl: boolean = false;
    /**
    * Category data.
    * @type {CategoryModel}
    */
    model: any;

    /**
     * Manages the Category Details page for creating and editing categories.
     *
     * @param $scope the application model
     * @param $transition$ the URL parameters
     * @param CategoriesService the category service
     * @constructor
     */
    static readonly $inject = ["$scope", "$q", "CategoriesService", "AccessControlService"];
    constructor(private $scope: any, private $q: any, private CategoriesService: any, private accessControlService: AccessControlService) {


        this.model = {};
        $scope.$watch(
            () => {
                return CategoriesService.model
            },
            (newModel: any, oldModel: any) => {
                this.model = newModel;
                if (oldModel && oldModel.id == null && newModel.id != null) {
                    this.checkAccessControl();
                }
            },
            true
        );
    }

    ngOnInit() {
        // Load the list of categories
        if (this.CategoriesService.categories.length === 0) {
            this.CategoriesService.reload().then(() => this.onLoad());
        } else {
            this.onLoad();
        }
        this.checkAccessControl();
    }

    $onInit() {
        this.ngOnInit();
    }

    getIconColorStyle(iconColor: any) {
        return { 'fill': iconColor };
    };
    /**
    * Loads the category data once the list of categories has loaded.
    */
    onLoad() {
        console.log('CategoryDetailsController onLoad');
        if (angular.isString(this.$transition$.params().categoryId)) {
            this.model = this.CategoriesService.model = this.CategoriesService.findCategory(this.$transition$.params().categoryId);
            if (angular.isDefined(this.CategoriesService.model)) {
                this.CategoriesService.model.loadingRelatedFeeds = true;
                this.CategoriesService.populateRelatedFeeds(this.CategoriesService.model).then((category: any) => {
                    category.loadingRelatedFeeds = false;
                });
            }
            this.loadingCategory = false;
        } else {
            this.CategoriesService.getUserFields()
                .then((userFields: any) => {
                    this.CategoriesService.model = this.CategoriesService.newCategory();
                    this.CategoriesService.model.userProperties = userFields;
                    this.loadingCategory = false;
                });
        }
    };
    checkAccessControl() {
        if (this.accessControlService.isEntityAccessControlled()) {
            //Apply the entity access permissions... only showAccessControl if the user can change permissions
            this.$q.when(this.accessControlService.hasPermission(AccessControlService.CATEGORIES_ACCESS, this.model, AccessControlService.ENTITY_ACCESS.CATEGORY.CHANGE_CATEGORY_PERMISSIONS)).then(
                (access: any) => {
                    this.showAccessControl = access;
                });
        }
    }

}
const module = angular.module(moduleName).component('categoryDetailsController', {
    bindings: {
        $transition$: "<"
    },
    controller: CategoryDetailsController,
    controllerAs: "vm",
    templateUrl: './category-details.html'
});
export default module;
/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */ 

 
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('../module-name');

export class DefineFeedPropertiesController {

    stepIndex:any;
    stepNumber:number;
    model:any;
    feedTagService:any;
    tagChips:any = {};
    isValid:boolean = true;
    totalSteps:any;
    stepperController:{ totalSteps : number };



    /**
     * Sets the user fields for this feed.
     *
     * @param {Array} userProperties the user fields
     */
    setUserProperties(userProperties:any){
        // Convert old user properties to map
        var oldProperties : any = null;

        this.model.userProperties.forEach((property:any) => {
            if (angular.isString(property.value) && property.value.length > 0) {
                oldProperties[property.systemName] = property.value;
            }
        });

        // Set new user properties and copy values
        this.model.userProperties = angular.copy(userProperties);

        this.model.userProperties.forEach((property:any) => {
            if (angular.isDefined(oldProperties[property.systemName])) {
                property.value = oldProperties[property.systemName];
                delete oldProperties[property.systemName];
            }
        });

        // Copy remaining old properties
        oldProperties.forEach((value:any, key:any) => {
            this.model.userProperties.push({locked: false, systemName: key, value: value});
        });
    }

    
    transformChip(chip:any){
        // If it is an object, it's already a known chip
        if (angular.isObject(chip)) {
            return chip;
        }
        // Otherwise, create a new one
        return {name: chip}
    };

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }

    static readonly $inject = ["$scope","$http","$mdToast","RestUrlService","FeedTagService","FeedService"];

    constructor(private $scope:IScope, private $http:angular.IHttpService, private $mdToast:angular.material.IToastService, private RestUrlService :any, private FeedTagService:any, private FeedService:any) {

        this.model = FeedService.createFeedModel;
        this.feedTagService = FeedTagService;
        this.tagChips.selectedItem = null;
        this.tagChips.searchText = null;

        if(angular.isUndefined(this.model.tags)){
            this.model.tags = []
        }
        // Update user fields when category changes
        $scope.$watch(
                ()=> {return this.model.category.id},
                (categoryId:any) => {
                    if (categoryId !== null) {
                        FeedService.getUserFields(categoryId)
                                .then(() => this.setUserProperties);
                    }
                }
        );
    };
}

angular.module(moduleName).
    component("thinkbigDefineFeedProperties", {
        bindings: {
            stepIndex: '@'
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedPropertiesController,
        templateUrl: './define-feed-properties.html',
    });

import * as angular from 'angular';
import * as _ from "underscore";
import { FeedService } from '../../../services/FeedService';
import {CategoriesService} from '../../../services/CategoriesService';
const moduleName = require('../module-name');

export class DefineFeedGeneralInfoController {
        stepIndex: any;
        /**
         * The angular form
         * @type {{}}
         */
        defineFeedGeneralForm:any = {};
        templates:Array<any> = [];
        model:any;
        isValid:boolean = false;
        stepNumber:number;
        stepperController:any = null;
        // Contains existing system feed names for the current category
        existingFeedNames:any = {};
        
        categorySearchText:string = '';
        category:any;

        
        /**
         * are we populating the feed name list for validation
         * @type {boolean}
         */
        populatingExsitngFeedNames:boolean = false;

        totalSteps:any;

        feedNameWatch:any;
        templateIdWatch:any;
        
        searchTextChange = (text:string) => {
            //   $log.info('Text changed to ' + text);
        }
        categorySearchTextChanged:any = this.searchTextChange;
        selectedItemChange = (item:any) => {
            //only allow it if the category is there and the 'createFeed' flag is true
            if(item != null && item != undefined && item.createFeed) {
                this.model.category.name = item.name;
                this.model.category.id = item.id;
                this.model.category.systemName = item.systemName;
                this.setSecurityGroups(item.name);
                this.validateUniqueFeedName();
                
                if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['category']) {
                    this.defineFeedGeneralForm['category'].$setValidity('accessDenied', true);
                }
            }
            else {
                this.model.category.name = null;
                this.model.category.id = null;
                this.model.category.systemName = null;
                if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['feedName']) {
                    this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                }
                
                if(item && item.createFeed == false){
                    if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['category']) {
                        this.defineFeedGeneralForm['category'].$setValidity('accessDenied', false);
                    }
                }
                else {
                    if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['category']) {
                        this.defineFeedGeneralForm['category'].$setValidity('accessDenied', true);
                    }
                }
            }
        }
        
        categorySelectedItemChange:any = this.selectedItemChange;
        existingFeedNameKey = (categoryName:string, feedName:string) => {
            return categoryName + "." + feedName;
        }

   
        /**
        * updates the {@code existingFeedNames} object with the latest feed names from the server
        * @returns {promise}
        */
        populateExistingFeedNames = () => {
            if(!this.populatingExsitngFeedNames) {
                this.populatingExsitngFeedNames = true;
                this.feedService.getFeedNames().then();
                return this.$http.get(this.RestUrlService.OPS_MANAGER_FEED_NAMES).then((response:any) => {
                    this.existingFeedNames = {};
                    if (response.data != null && response.data != null) {
                        angular.forEach(response.data, (categoryAndFeed) => {
                            var categoryName = categoryAndFeed.substr(0, categoryAndFeed.indexOf('.'));
                            var feedName = categoryAndFeed.substr(categoryAndFeed.indexOf('.')+1)
                            this.existingFeedNames[categoryAndFeed] = feedName;
                        });
                        this.populatingExsitngFeedNames = false;
                    }
                }, () => {
                    this.populatingExsitngFeedNames = false;
                });
            }
        }

        _validate = () => {
            //validate to ensure the name is unique in this category
            if (this.model && this.model.category && this.existingFeedNames[this.existingFeedNameKey(this.model.category.systemName, this.model.systemFeedName)]) {
                if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['feedName']) {
                    this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', false);
                }
            }
            else {
                if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['feedName']) {
                    this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                }
            }
        }

        validateUniqueFeedName = () => {

            if (this.model && this.model.id && this.model.id.length > 0) {
                this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
            } else if (_.isEmpty(this.existingFeedNames)) {
                if(!this.populatingExsitngFeedNames) {
                    this.populateExistingFeedNames().then( () => {
                        this._validate();
                    });
                }
            } else {
                this._validate();
            }

        }
       
        validate = () => {
            var valid = this.isNotEmpty(this.model.category.name) && this.isNotEmpty(this.model.feedName) && this.isNotEmpty(this.model.templateId);
            this.isValid = valid;
        }
   
        setSecurityGroups = (newVal:any) => {
            if(newVal) {
                var category = this.categoriesService.findCategoryByName(newVal)
                if(category != null) {
                    var securityGroups = category.securityGroups;
                    this.model.securityGroups = securityGroups;
                }
            }
        }

        isNotEmpty = (item:any) => {
            return item != null && item != undefined && item != '';
        }
        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        getRegisteredTemplates =  () => {
            var successFn = (response:any) => {
                this.templates = response.data;
            }
            var errorFn = (err:any) => {

            }
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };
    $onDestroy(){
        this.ngOnDestroy(); 
    }
    ngOnDestroy(){
        this.feedNameWatch();
        this.templateIdWatch();
        this.model = null;
    }
    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }
    static readonly $inject = ["$scope","$log","$http","$mdToast","RestUrlService",
    "FeedService","CategoriesService"];

    constructor(private $scope:IScope,private $log:angular.ILogService, private $http:angular.IHttpService,
        private $mdToast:angular.material.IToastService,private RestUrlService:any, 
        private feedService:FeedService, private categoriesService:CategoriesService) {

        this.populateExistingFeedNames();
        
        this.model= this.feedService.createFeedModel;
        
        $scope.$watch(() =>{
            return this.model.id;
        },(newVal:any) => {
            if(newVal == null && (angular.isUndefined(this.model.cloned) || this.model.cloned == false)) {
                this.category = null;
            }
            else {
                this.category = this.model.category;
            }
        })

       this.feedNameWatch = $scope.$watch(() => {
            return this.model.feedName;
        },(newVal:any) => {
           this.feedService.getSystemName(newVal).then((response:any) => {
            this.model.systemFeedName = response.data;
               this.model.table.tableSchema.name = this.model.systemFeedName;
               this.validateUniqueFeedName();
               this.validate();
           });

        });

        $scope.$watch(() => {
            return this.model.category.name;
        },(newVal:any) => {

            this.validate();
        })

        this.templateIdWatch =  $scope.$watch(() =>{
            return this.model.templateId;
        },(newVal:any) => {
            this.validate();
        });
    };
}

angular.module(moduleName).
    component("thinkbigDefineFeedGeneralInfo", {
        bindings: {
            stepIndex: '@'
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedGeneralInfoController,
        templateUrl: './define-feed-general-info.html',
    });
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('../module-name');

export class DefineFeedAccessControlController {

    /**
         * Flag to indicate if hadoop groups are enabled or not
         * @type {boolean}
    */
    securityGroupsEnabled:any = false;
    /**
         * Hadoop security groups chips model
         * @type {{}}
    */
    securityGroupChips:any = {};
    /**
         * Service to access the Hadoop security groups
    */
    feedSecurityGroups:any;
    /**
         * The feed model
         * @type {*}
    */
    model:any;
    feedAccessControlForm:any = {};
    stepNumber:number;
    stepIndex:any;
    stepperController: { totalSteps : number };
    totalSteps:number;
    
    $onInit(){
        this.ngOnInit();
    }
    ngOnInit(){
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex)+1;
    }

    static readonly $inject = ["$scope","FeedService","FeedSecurityGroups"];

    constructor($scope:IScope,FeedService:any, FeedSecurityGroups:any) {

        this.model = FeedService.createFeedModel;
        
        this.feedSecurityGroups = FeedSecurityGroups;
        
        this.securityGroupChips.selectedItem = null;
        this.securityGroupChips.searchText = null;

        FeedSecurityGroups.isEnabled().then((isValid:any) =>{
            this.securityGroupsEnabled = isValid;
        });
    }
    transformChip(chip:any){
        // If it is an object, it's already a known chip
        if (angular.isObject(chip)) {
            return chip;
        }
        // Otherwise, create a new one
        return {name: chip}
    }
}
angular.module(moduleName).
    component("thinkbigDefineFeedAccessControl", {
        bindings: {
            stepIndex: '@'
        },
        require:{
            stepperController: "^thinkbigStepper"

        },
        controllerAs: 'vm',
        controller: DefineFeedAccessControlController,
        templateUrl: './define-feed-access-control.html',
    });
import * as angular from "angular";
import {moduleName} from "../module-name";
import {AddButtonService} from "../../services/AddButtonService";
import { TransitionService } from "@uirouter/core";

export default class AddButton implements ng.IComponentController {

    
    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        this.BroadcastService.subscribe(this.$scope, this.addButtonService.NEW_ADD_BUTTON_EVENT,this.updateShowState)
        this.BroadcastService.subscribe(this.$scope, this.addButtonService.HIDE_ADD_BUTTON_EVENT, this.hideButton)
        this.BroadcastService.subscribe(this.$scope, this.addButtonService.SHOW_ADD_BUTTON_EVENT, this.showButton)

        this.$transitions.onSuccess({},(transition: any) =>{
                        var toState = transition.to();
                        if(toState != undefined) {
                            var state = toState.name;
                            if(toState.name == 'home'){
                                state = 'feeds';
                            }
                            this.$scope.currentState = state;
                            this.updateShowState();
                        }
                    });    
    }

    static readonly $inject = ["$scope","$element", "$http","$rootScope","$transitions",
                    "StateService","AddButtonService","BroadcastService"];

    constructor(private $scope: IScope,
                private $element: JQuery,
                private $http: angular.IHttpService,
                private $rootScope: IScope,
                private $transitions: TransitionService,
                private StateService: any, 
                private addButtonService: AddButtonService,
                private BroadcastService: any) {
                    
                    this.$scope.currentState = '';
                    this.$scope.onClickAddButton= (event: any) =>{
                        this.addButtonService.onClick(this.$scope.currentState);
                    }

                    
                }

        isShowAddButton = () => {
            return this.addButtonService.isShowAddButton(this.$scope.currentState);
        }

        hideButton = () =>{
            this.$element.hide();
        }

        showButton = () =>{
            this.$element.show();
        }

        updateShowState = () => {
            if(this.isShowAddButton()){
                this.$element.show();
            }
            else {
                this.$element.hide();
            }
        }

}

angular.module(moduleName).component('addButton', {
    controller: AddButton,
    template: '<md-button class="md-fab md-fab-bottom-right kylo-add-button" aria-label="Add" ng-click="onClickAddButton($event)"><ng-md-icon icon="add"></ng-md-icon></md-button>'
});



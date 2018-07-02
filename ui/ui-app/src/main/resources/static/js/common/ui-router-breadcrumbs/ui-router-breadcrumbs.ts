import * as angular from "angular";
import {moduleName} from "../module-name";
import "@uirouter/angularjs";
import * as _ from 'underscore';
import {StateService, Transition} from "@uirouter/core";
/**
 * Config
 */
var templateUrl = 'js/common/ui-router-breadcrumbs/uiBreadcrumbs.tpl.html';

export default class RouterBreadcrumbs implements ng.IComponentController {

    breadcrumbs: any = [];
    lastBreadcrumbs: any = []; 

    displaynameProperty: any;
    abstractProxyProperty: any;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        this.$transitions.onSuccess({},(transition: any)=>{
            var toState = transition.to();
            var toParams = transition.params();
            if(toState.data !== undefined ) {
                if (toState.data.noBreadcrumb && toState.data.noBreadcrumb == true) {
                    //console.log('Skipping breadcrumb for ',toState)
                } else {
                    this.updateBreadcrumbs(toState, toParams);
                }
            }
        });
    }

    static readonly $inject = ["$interpolate","$state","$transitions","$element", "$attrs"];

    constructor(private $interpolate: angular.IInterpolateService,
                private $state: StateService,
                private $transitions: Transition,
                private $element: JQuery, 
                private $attrs: any) {}

    navigate(crumb: any) {
        this.$state.go(crumb.route,crumb.params);
    }

    updateLastBreadcrumbs(){
        this.lastBreadcrumbs = this.breadcrumbs.slice(Math.max(this.breadcrumbs.length - 2,0));
    }

    getBreadcrumbKey(state: any){
        return state.name;
    }

    getDisplayName(state: any) {
        return state.data.displayName || state.name;
    }

    isBreadcrumbRoot(state: any){
        return state.data.breadcrumbRoot && state.data.breadcrumbRoot == true;
    }

    addBreadcrumb(state: any, params: any){
        var breadcrumbKey = this.getBreadcrumbKey(state);
        var copyParams = {}
        if(params ) {
            angular.extend(copyParams, params);
        }

        copyParams = _.omit(copyParams, (value: any, key: any, object: any)=> {
            return key.startsWith("bcExclude_")
        });

        var displayName = this.getDisplayName(state);
        this.breadcrumbs.push({
            key:breadcrumbKey,
            displayName: displayName,
            route: state.name,
            params:copyParams
        });

        this.updateLastBreadcrumbs();
    }
    

    getBreadcrumbIndex(state: any){
        var breadcrumbKey = this.getBreadcrumbKey(state);
        var matchingState = _.find(this.breadcrumbs,(breadcrumb: any)=>{
            return breadcrumb.key == breadcrumbKey;
        });
        if(matchingState){
            return _.indexOf(this.breadcrumbs,matchingState)
        }
        return -1;
    }
    updateBreadcrumbs(state: any,params: any){
        var index = this.getBreadcrumbIndex(state);
        if(this.isBreadcrumbRoot(state)){
            index = 0;
        }
        if(index == -1){
            this.addBreadcrumb(state,params);
        }
        else {
            //back track until we get to this index and then replace it with the incoming one
            this.breadcrumbs =  this.breadcrumbs.slice(0,index);
            this.addBreadcrumb(state,params);
        }
    }
    /**
     * Resolve the displayName of the specified state. Take the property specified by the `displayname-property`
     * attribute and look up the corresponding property on the state's config object. The specified string can be interpolated against any resolved
     * properties on the state config object, by using the usual {{ }} syntax.
     * @param currentState
     * @returns {*}
     */
    getDisplayName1(currentState: any) {
        var interpolationContext;
        var propertyReference;
        var displayName;

        if (!this.displaynameProperty) {
            // if the displayname-property attribute was not specified, default to the state's name
            return currentState.name;
        }
        propertyReference = this.getObjectValue(this.displaynameProperty, currentState);

        if (propertyReference === false) {
            return false;
        } else if (typeof propertyReference === 'undefined') {
            return currentState.name;
        } else {
            // use the $interpolate service to handle any bindings in the propertyReference string.
            interpolationContext =  (typeof currentState.locals !== 'undefined') ? currentState.locals.globals : currentState;
            displayName = this.$interpolate(propertyReference)(interpolationContext);
            return displayName;
        }
    }

    /**
     * Given a string of the type 'object.property.property', traverse the given context (eg the current $state object) and return the
     * value found at that path.
     *
     * @param objectPath
     * @param context
     * @returns {*}
     */
    getObjectValue(objectPath: any, context: any) {
        var i;
        var propertyArray = objectPath.split('.');
        var propertyReference = context;

        for (i = 0; i < propertyArray.length; i ++) {
            if (angular.isDefined(propertyReference[propertyArray[i]])) {
                propertyReference = propertyReference[propertyArray[i]];
            } else {
                // if the specified property was not found, default to the state's name
                return undefined;
            }
        }
        return propertyReference;
    }

}

angular.module(moduleName).component("uiRouterBreadcrumbs",{
    controller: RouterBreadcrumbs,
    bindings: {
        displaynameProperty: '@',
        abstractProxyProperty: '@?'
    },
    templateUrl: ($element: any, $attrs: any)=> {
        return $attrs.templateUrl || templateUrl;
    }
});
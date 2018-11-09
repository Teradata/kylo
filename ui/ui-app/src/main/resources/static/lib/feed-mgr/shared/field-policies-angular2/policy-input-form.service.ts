import * as angular from 'angular';
import * as _ from "underscore";
import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {MatDialog} from "@angular/material/dialog";
import {TdDialogService} from "@covalent/core/dialogs";
import {FormGroup} from "@angular/forms";
import {FieldConfig} from "../../../dynamic-form/model/FieldConfig";
import {KyloFeed} from '../../model/feed/kylo-feed';


@Injectable()
export class PolicyInputFormService {

    private formKeyNumber:number = 0;

    /**
     * The default value that is supplied via java annotation if user wants the value to be defaulted to the current feed
     * see java class(PolicyPropertyTypes)
     * type {string}
     */
    private CURRENT_FEED_DEFAULT_VALUE :string = "#currentFeed";

    constructor(private http: HttpClient, private dialog: MatDialog,private _dialogService: TdDialogService) {

    }

    currentFeedValue(feed:KyloFeed) {
        return feed.category.systemName + "." + feed.systemFeedName;
    }
    attachCurrentFeedValues(data:any, feedName:string) {
        //set the currentFeed property value to be this.feed if it is not null
        let currentFeedProperties:any = [];
        _.each(data, (rules:any) =>{

            _.each(rules.properties, (prop:any) =>{
                if (prop.type == 'currentFeed' || prop.value == this.CURRENT_FEED_DEFAULT_VALUE) {
                    currentFeedProperties.push(prop);
                }
            });

        });
        _.each(currentFeedProperties, (prop:any) =>{
            this.attachCurrentFeedValueToProperty(prop, feedName);

        });

    }

    groupPolicyOptions(options:any, currentFeedName:any) {
        if (currentFeedName != null && currentFeedName != undefined) {
            this.attachCurrentFeedValues(options, currentFeedName);
        }

        var optionsArr:any = [];
        angular.forEach(options, (opt:any) =>{
            opt.groups = this.groupProperties(opt);
            optionsArr.push(opt);
        });
        return optionsArr;
    }
    isFeedProperty(property:any){
        return (property.type == 'currentFeed' || property.type == 'feedSelect' || property.type == 'feedChips');
    }
    getRuleNames(ruleArray:any){
        var properties = [];
        var names = _.map(ruleArray,function(rule:any){
            return rule.name;
        });
        return _.uniq(names);
    }

    /**
     * Return an array of the feed names for the sla
     * param sla
     * return {Array}
     */
    getFeedNames(ruleArray:any[]) {
        var names:any = [];
        _.each(ruleArray,(rule:any)=>{
            _.each(rule.properties,(property:any) => {
                if(this.isFeedProperty(property)) {
                    if(property.value != null && property.value != undefined) {
                        if(property.value != '#currentFeed') {
                            names.push(property.value)
                        }
                    }
                }
            });
        });
        return _.uniq(names);


    }
    /**
     * remove all feeds from the selectable values where the "feed:editDetails" property is false
     * param policies array of policies
     */
    stripNonEditableFeeds(policies:any){
        _.each(policies, function (rule:any) {
            _.each(rule.properties, function (prop:any) {
                if (prop.type == 'currentFeed' || prop.type == 'feedSelect' || prop.type == 'feedChips') {
                    prop.errorMessage =undefined;

                    var newSelectableValues =_.reject(prop.selectableValues,function(value:any){
                        if(value.properties == undefined || value.properties["feed:editDetails"] == undefined){
                            return false;
                        }
                        else {
                            return (value.properties["feed:editDetails"] == false);
                        }
                    });
                    prop.selectableValues = newSelectableValues;

                    if( prop.selectableValues.length ==0){
                        prop.errorMessage = "No feeds available.  You don't have access to modify any feeds.";
                    }
                }
            });

        });
    }



    validateRequiredChips(theForm:any, property:any) {
        if(property instanceof FieldConfig){
            let propertyValue = (<FieldConfig<any>>property).getModelValue();
            if(property.required && propertyValue != null && propertyValue.length == 0){
                //INVALID
             //   (<FormGroup>theForm).get(property.key).
             //   theForm[property.formKey].$setValidity("required", false);
             //   theForm[property.formKey].$setDirty(true);
                (theForm as FormGroup).get((property as FieldConfig<any>).key).setValue('');
                return false;
            }
            else {
                (theForm as FormGroup).get((property as FieldConfig<any>).key).setValue(propertyValue);
                return true;
            }
        }
        else  if (property.required && property.values && property.values.length == 0) {
            //INVALID
            theForm[property.formKey].$setValidity("required", false);
            theForm[property.formKey].$setDirty(true);
            return false;
        }
        else {
            theForm[property.formKey].$setValidity("required", true);
            return true;
        }
    }
    /**
     * Validate the form before adding/editing a Rule for an SLA
     * returns {boolean}
     */
    validateForm(theForm:any, ruleProperties:any, showErrorDialog:any) {
        if (showErrorDialog == undefined) {
            showErrorDialog = true;
        }
        //loop through properties and determine if they are valid
        //the following _.some routine returns true if the items are invalid
        var validForm = _.some(ruleProperties,  (property:any) =>{
            var valid = true;

            if (property.type == 'feedChips' || property.type == 'chips') {
                valid = this.validateRequiredChips(theForm, property);
                property.invalid = !valid
            }
            else if (property.required && (property.value == undefined || property.value == '' || property.value == null)) {
                valid = false;
                theForm[property.formKey].$setValidity("required", false);
                theForm[property.formKey].$setDirty(true);
                property.invalid = true;
            }
            else {
                property.invalid = false;
            }

            //sort circuit on truth value so return the opposite to stop the traversing
            return !valid;
        });

        validForm = !validForm && !theForm.invalid;

        if (!validForm && showErrorDialog) {
            this._dialogService.openAlert({
                message:'Some of the form fields are invalid.  Please fix all validation errors prior to saving',
                title:'Input Errors'
            })
        }

        return validForm;
    }
    queryChipSearch(property:any, query:any) {
        var options = property.selectableValues;
        var results = query ? options.filter(this.createFilterFor(query)) : [];
        return results;
    }
    transformChip(chip:any) {
        // If it is an object, it's already a known chip
        if (angular.isObject(chip)) {
            return chip;
        }
        // Otherwise, create a new one
        return {name: chip}
    }


    groupProperties(metric:any) {
        var group = _.groupBy(metric.properties, 'group');
        var groupedProperties:any = [];
        var index = 0;

        _.each(group, function (props:any, groupName:any) {
            var sortedProps = _.sortBy(props, 'groupOrder')
            var newGroup = {group: groupName, layout: groupName != '' ? 'row' : 'column', properties: sortedProps}
            groupedProperties.push(newGroup);
        });

        var allProps:any = [];
        _.each(groupedProperties, function (group:any) {
            _.each(group.properties, function (property:any) {
                //make the RegExp if it is supplied as a string
                if (property.pattern != null && property.pattern != undefined && property.pattern != "") {
                    try {
                        property.patternRegExp = new RegExp(property.pattern);
                    } catch (err) {

                    }
                }
                allProps.push(property);
            });
        });
        metric.properties = allProps;

        return groupedProperties;

    }

     updatePropertyIndex(rule:any) {
        _.each(rule.properties, (property:any) => {
            property.formKey = 'property_' + this.formKeyNumber++;
        });
    }

    private createFilterFor(query:any) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(option:any) {
            return (angular.lowercase(option.value).indexOf(lowercaseQuery) >= 0);
        };
    }

    private attachCurrentFeedValueToProperty(prop:any, feedName:any) {
        if (prop.value == undefined || prop.value == null || prop.value == this.CURRENT_FEED_DEFAULT_VALUE) {
            prop.value = feedName;
        }
    };



}


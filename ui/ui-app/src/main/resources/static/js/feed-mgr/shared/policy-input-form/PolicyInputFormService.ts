import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";;


export class PolicyInputFormService {

    formKeyNumber: number = 0;
    /**
     * The default value that is supplied via java annotation if user wants the value to be defaulted to the current feed
     * @see java class(PolicyPropertyTypes)
     * @type {string}
     */
    CURRENT_FEED_DEFAULT_VALUE: string = "#currentFeed";

    ngOnInit(): void {
        this.init();
    }

    $onInit(): void {
        this.ngOnInit();
    }

    static readonly $inject = ["$mdDialog"];
    constructor(private $mdDialog: angular.material.IDialogService) {

            this.init();
    }
    groupProperties(metric: any) {
        var group = _.groupBy(metric.properties, 'group');
        var groupedProperties: any = [];
        var index = 0;

        _.each(group, (props: any, groupName: any) => {
            var sortedProps = _.sortBy(props, 'groupOrder')
            var newGroup = { group: groupName, layout: groupName != '' ? 'row' : 'column', properties: sortedProps }
            groupedProperties.push(newGroup);
        });

        var allProps: any = [];
        _.each(groupedProperties, (group: any) => {
            _.each(group.properties, (property: any) => {
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

    updatePropertyIndex(rule: any) {
        _.each(rule.properties, (property: any) => {
            property.formKey = 'property_' + this.formKeyNumber++;
        });
    }

    createFilterFor(query: any) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(option: any) {
            return (angular.lowercase(option.value).indexOf(lowercaseQuery) >= 0);
        };
    }
    attachCurrentFeedValueToProperty = (prop: any, feedName: any) => {
        if (prop.value == undefined || prop.value == null || prop.value == this.CURRENT_FEED_DEFAULT_VALUE) {
            prop.value = feedName;
        }
    };
    currentFeedValue = (feed: any) => {
        return feed.category.systemName + "." + feed.systemFeedName;
    };
    attachCurrentFeedValues = (data: any, feedName: any) => {
        //set the currentFeed property value to be this.feed if it is not null
        var currentFeedProperties: any = [];
        _.each(data, (rules: any) => {

            _.each(rules.properties, (prop: any) => {
                if (prop.type == 'currentFeed' || prop.value == this.CURRENT_FEED_DEFAULT_VALUE) {
                    currentFeedProperties.push(prop);
                }
            });

        });
        _.each(currentFeedProperties, (prop: any) => {
            this.attachCurrentFeedValueToProperty(prop, feedName);

        });

    };

    groupPolicyOptions = (options: any, currentFeedName: any) => {
        if (currentFeedName != null && currentFeedName != undefined) {
            this.attachCurrentFeedValues(options, currentFeedName);
        }

        var optionsArr: any = [];
        angular.forEach(options, (opt: any) => {
            opt.groups = this.groupProperties(opt);
            optionsArr.push(opt);
        });
        return optionsArr;
    };
    isFeedProperty = (property: any) => {
        return (property.type == 'currentFeed' || property.type == 'feedSelect' || property.type == 'feedChips');
    };
    getRuleNames = (ruleArray: any) => {
        var properties = [];
        var names = _.map(ruleArray, (rule: any) => {
            return rule.name;
        });
        return _.uniq(names);
    };
    /**
     * Return an array of the feed names for the sla
     * @param sla
     * @return {Array}
     */
    getFeedNames = (ruleArray: any) => {
        var names: any = [];
        _.each(ruleArray, (rule: any) => {
            _.each(rule.properties, (property: any) => {
                if (this.isFeedProperty(property)) {
                    if (property.value != null && property.value != undefined) {
                        if (property.value != '#currentFeed') {
                            names.push(property.value)
                        }
                    }
                }
            });
        });
        return _.uniq(names);


    };
    /**
     * remove all feeds from the selectable values where the "feed:editDetails" property is false
     * @param policies array of policies
     */
    stripNonEditableFeeds = (policies: any) => {
        _.each(policies, (rule: any) => {

            _.each(rule.properties, (prop: any) => {
                if (prop.type == 'currentFeed' || prop.type == 'feedSelect' || prop.type == 'feedChips') {
                    prop.errorMessage = undefined;

                    var newSelectableValues = _.reject(prop.selectableValues, (value: any) => {
                        if (value.properties == undefined || value.properties["feed:editDetails"] == undefined) {
                            return false;
                        }
                        else {
                            return (value.properties["feed:editDetails"] == false);
                        }
                    });
                    prop.selectableValues = newSelectableValues;

                    if (prop.selectableValues.length == 0) {
                        prop.errorMessage = "No feeds available.  You don't have access to modify any feeds.";
                    }
                }
            });

        });
    };
    validateRequiredChips = (theForm: any, property: any) => {
        if (property.required && property.values.length == 0) {
            //INVALID
            theForm[property.formKey].$setValidity("required", false);
            theForm[property.formKey].$setDirty(true);
            return false;
        }
        else {
            theForm[property.formKey].$setValidity("required", true);
            return true;
        }
    };
    /**
     * Validate the form before adding/editing a Rule for an SLA
     * @returns {boolean}
     */
    validateForm = (theForm: any, ruleProperties: any, showErrorDialog: any) => {
        if (showErrorDialog == undefined) {
            showErrorDialog = true;
        }
        //loop through properties and determine if they are valid
        //the following _.some routine returns true if the items are invalid
        var validForm = _.some(ruleProperties, (property: any) => {
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

        validForm = !validForm && theForm.$valid;

        if (!validForm && showErrorDialog) {
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(true)
                    .title('Input Errors')
                    .textContent('Some of the form fields are invalid.  Please fix all validation errors prior to saving')
                    .ariaLabel('Alert Input Sla errors')
                    .ok('Got it!')
            );
        }

        return validForm;
    };
    queryChipSearch = (property: any, query: any) => {
        var options = property.selectableValues;
        var results = query ? options.filter(this.createFilterFor(query)) : [];
        return results;
    };
    transformChip = (chip: any) => {
        // If it is an object, it's already a known chip
        if (angular.isObject(chip)) {
            return chip;
        }
        // Otherwise, create a new one
        return { name: chip }
    };
    init = () => {

    }
}

angular.module(moduleName).service('PolicyInputFormService', PolicyInputFormService);

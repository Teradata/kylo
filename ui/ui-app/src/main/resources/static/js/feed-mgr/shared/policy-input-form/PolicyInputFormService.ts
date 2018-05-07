import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');


    angular.module(moduleName).factory('PolicyInputFormService', ["$http","$q","$mdToast","$mdDialog",function ($http:any, $q:any, $mdToast:any, $mdDialog:any) {

        var formKeyNumber = 0;

        function groupProperties(metric:any) {
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

        function updatePropertyIndex(rule:any) {
            _.each(rule.properties, function (property:any) {
                property.formKey = 'property_' + formKeyNumber++;
            });
        }

        function createFilterFor(query:any) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(option:any) {
                return (angular.lowercase(option.value).indexOf(lowercaseQuery) >= 0);
            };
        }

        /**
         * The default value that is supplied via java annotation if user wants the value to be defaulted to the current feed
         * @see java class(PolicyPropertyTypes)
         * @type {string}
         */
        var CURRENT_FEED_DEFAULT_VALUE = "#currentFeed";

        var attachCurrentFeedValueToProperty = function (prop:any, feedName:any) {
            if (prop.value == undefined || prop.value == null || prop.value == CURRENT_FEED_DEFAULT_VALUE) {
                prop.value = feedName;
            }
        };

        var data = {

            currentFeedValue: function (feed:any) {
                return feed.category.systemName + "." + feed.systemFeedName;
            },
            attachCurrentFeedValues: function (data:any, feedName:any) {
                //set the currentFeed property value to be this.feed if it is not null
                var currentFeedProperties:any = [];
                _.each(data, function (rules:any) {

                    _.each(rules.properties, function (prop:any) {
                        if (prop.type == 'currentFeed' || prop.value == CURRENT_FEED_DEFAULT_VALUE) {
                            currentFeedProperties.push(prop);
                        }
                    });

                });
                _.each(currentFeedProperties, function (prop:any) {
                    attachCurrentFeedValueToProperty(prop, feedName);

                });

            },

            groupPolicyOptions: function (options:any, currentFeedName:any) {
                if (currentFeedName != null && currentFeedName != undefined) {
                    data.attachCurrentFeedValues(options, currentFeedName);
                }

                var optionsArr:any = [];
                angular.forEach(options, function (opt:any) {
                    opt.groups = groupProperties(opt);
                    optionsArr.push(opt);
                });
                return optionsArr;
            },
            isFeedProperty: function(property:any){
                return (property.type == 'currentFeed' || property.type == 'feedSelect' || property.type == 'feedChips');
            },
            getRuleNames:function(ruleArray:any){
                var properties = [];
               var names = _.map(ruleArray,function(rule:any){
                    return rule.name;
                });
               return _.uniq(names);
            },
            /**
             * Return an array of the feed names for the sla
             * @param sla
             * @return {Array}
             */
            getFeedNames:function(ruleArray:any) {
                var names:any = [];
                _.each(ruleArray,function(rule:any){
                    _.each(rule.properties,function(property:any) {
                       if(data.isFeedProperty(property)) {
                           if(property.value != null && property.value != undefined) {
                               if(property.value != '#currentFeed') {
                                   names.push(property.value)
                               }
                           }
                       }
                });
            });
                return _.uniq(names);


            },
            /**
             * remove all feeds from the selectable values where the "feed:editDetails" property is false
             * @param policies array of policies
             */
            stripNonEditableFeeds:function(policies:any){
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
                                },
            updatePropertyIndex: function (rule:any) {
                updatePropertyIndex(rule);
            },
            groupProperties: function (rule:any) {
                return groupProperties(rule);
            },
            validateRequiredChips: function (theForm:any, property:any) {
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
            },
            /**
             * Validate the form before adding/editing a Rule for an SLA
             * @returns {boolean}
             */
            validateForm: function (theForm:any, ruleProperties:any, showErrorDialog:any) {
                if (showErrorDialog == undefined) {
                    showErrorDialog = true;
                }
                //loop through properties and determine if they are valid
                //the following _.some routine returns true if the items are invalid
                var validForm = _.some(ruleProperties, function (property:any) {
                    var valid = true;

                    if (property.type == 'feedChips' || property.type == 'chips') {
                        valid = data.validateRequiredChips(theForm, property);
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
                    $mdDialog.show(
                        $mdDialog.alert()
                            .parent(angular.element(document.body))
                            .clickOutsideToClose(true)
                            .title('Input Errors')
                            .textContent('Some of the form fields are invalid.  Please fix all validation errors prior to saving')
                            .ariaLabel('Alert Input Sla errors')
                            .ok('Got it!')
                    );
                }

                return validForm;
            },
            queryChipSearch: function (property:any, query:any) {
                var options = property.selectableValues;
                var results = query ? options.filter(createFilterFor(query)) : [];
                return results;
            },
            transformChip: function (chip:any) {
                // If it is an object, it's already a known chip
                if (angular.isObject(chip)) {
                    return chip;
                }
                // Otherwise, create a new one
                return {name: chip}
            },
            init: function () {

            }

        };
        data.init();
        return data;

    }]);

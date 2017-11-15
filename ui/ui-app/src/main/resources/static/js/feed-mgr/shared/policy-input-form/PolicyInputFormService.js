define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('PolicyInputFormService', ["$http","$q","$mdToast","$mdDialog",function ($http, $q, $mdToast, $mdDialog) {

        var formKeyNumber = 0;

        function groupProperties(metric) {
            var group = _.groupBy(metric.properties, 'group');
            var groupedProperties = [];
            var index = 0;

            _.each(group, function (props, groupName) {
                var sortedProps = _.sortBy(props, 'groupOrder')
                var newGroup = {group: groupName, layout: groupName != '' ? 'row' : 'column', properties: sortedProps}
                groupedProperties.push(newGroup);
            });

            var allProps = [];
            _.each(groupedProperties, function (group) {
                _.each(group.properties, function (property) {
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

        function updatePropertyIndex(rule) {
            _.each(rule.properties, function (property) {
                property.formKey = 'property_' + formKeyNumber++;
            });
        }

        function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(option) {
                return (angular.lowercase(option.value).indexOf(lowercaseQuery) >= 0);
            };
        }

        /**
         * The default value that is supplied via java annotation if user wants the value to be defaulted to the current feed
         * @see java class(PolicyPropertyTypes)
         * @type {string}
         */
        var CURRENT_FEED_DEFAULT_VALUE = "#currentFeed";

        var attachCurrentFeedValueToProperty = function (prop, feedName) {
            if (prop.value == undefined || prop.value == null || prop.value == CURRENT_FEED_DEFAULT_VALUE) {
                prop.value = feedName;
            }
        };

        var data = {

            currentFeedValue: function (feed) {
                return feed.category.systemName + "." + feed.systemFeedName;
            },
            attachCurrentFeedValues: function (data, feedName) {
                //set the currentFeed property value to be this.feed if it is not null
                var currentFeedProperties = [];
                _.each(data, function (rules) {

                    _.each(rules.properties, function (prop) {
                        if (prop.type == 'currentFeed' || prop.value == CURRENT_FEED_DEFAULT_VALUE) {
                            currentFeedProperties.push(prop);
                        }
                    });

                });
                _.each(currentFeedProperties, function (prop) {
                    attachCurrentFeedValueToProperty(prop, feedName);

                });

            },

            groupPolicyOptions: function (options, currentFeedName) {
                if (currentFeedName != null && currentFeedName != undefined) {
                    data.attachCurrentFeedValues(options, currentFeedName);
                }

                var optionsArr = [];
                angular.forEach(options, function (opt) {
                    opt.groups = groupProperties(opt);
                    optionsArr.push(opt);
                });
                return optionsArr;
            },
            isFeedProperty: function(property){
                return (property.type == 'currentFeed' || property.type == 'feedSelect' || property.type == 'feedChips');
            },
            getRuleNames:function(ruleArray){
                var properties = [];
               var names = _.map(ruleArray,function(rule){
                    return rule.name;
                });
               return _.uniq(names);
            },
            /**
             * Return an array of the feed names for the sla
             * @param sla
             * @return {Array}
             */
            getFeedNames:function(ruleArray) {
                var names = [];
                _.each(ruleArray,function(rule){
                    _.each(rule.properties,function(property) {
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
            stripNonEditableFeeds:function(policies){
                _.each(policies, function (rule) {

                    _.each(rule.properties, function (prop) {
                        if (prop.type == 'currentFeed' || prop.type == 'feedSelect' || prop.type == 'feedChips') {
                            prop.errorMessage =undefined;

                            var newSelectableValues =_.reject(prop.selectableValues,function(value){
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
            updatePropertyIndex: function (rule) {
                updatePropertyIndex(rule);
            },
            groupProperties: function (rule) {
                return groupProperties(rule);
            },
            validateRequiredChips: function (theForm, property) {
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
            validateForm: function (theForm, ruleProperties, showErrorDialog) {
                if (showErrorDialog == undefined) {
                    showErrorDialog = true;
                }
                //loop through properties and determine if they are valid
                //the following _.some routine returns true if the items are invalid
                var validForm = _.some(ruleProperties, function (property) {
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
            queryChipSearch: function (property, query) {
                var options = property.selectableValues;
                var results = query ? options.filter(createFilterFor(query)) : [];
                return results;
            },
            transformChip: function (chip) {
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
});
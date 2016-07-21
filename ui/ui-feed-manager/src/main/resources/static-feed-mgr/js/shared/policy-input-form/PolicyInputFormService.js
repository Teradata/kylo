angular.module(MODULE_FEED_MGR).factory('PolicyInputFormService', function ($http, $q, $mdToast, $mdDialog, RestUrlService) {

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
        //replace the metric.properties with these newly orderd props?
        var allProps = [];
        _.each(groupedProperties, function (group) {
            _.each(group.properties, function (property) {
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

    var data = {

        groupPolicyOptions: function (options) {

            var optionsArr = [];
            angular.forEach(options, function (opt) {
                opt.groups = groupProperties(opt);
                optionsArr.push(opt);
            });
            return optionsArr;
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

});
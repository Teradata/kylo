define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('FeedPropertyService', function () {

        /**
         * If the property is sensitive we should store off the long encrypted value and show just asterisks
         * @param property
         */
        this.initSensitivePropertyForEditing = function(property){
            var self = this;
            if(property.sensitive && !self.isMasked(property.value)){
                property.encryptedValue = property.value;
                property.value = '******';
            }
        };
        /**
         * Return true if every char in value == '*'
         */
        this.isMasked = function(value){
           return value != null && _.every(value.split(''),function(char){
                return char == '*';
            });
        };
        /**
         * If the property is sensitive and hasnt changed we should set it back to the encrypted value.
         * @param property
         */
        this.initSensitivePropertyForSaving=function(property){
            var self = this;
            if(property.sensitive){
                if(self.isMasked(property.value)){
                    property.value = property.encryptedValue;
                }
                //reset it
                delete property.encryptedValue;
            }
        };

        this.initSensitivePropertiesForEditing = function(properties) {
            var self = this;
            if(properties && properties.length) {
                _.each(properties, function (prop) {
                    self.initSensitivePropertyForEditing(prop);
                });
            }

        };

        /**
         * Sets the displayValue attribute for the incoming property
         * @param property a NiFiProperty
         */
        this.updateDisplayValue = function(property) {
            property.displayValue = property.value;
            if (property.key == "Source Database Connection" && property.propertyDescriptor != undefined && property.propertyDescriptor.allowableValues) {
                var descriptorOption = _.find(property.propertyDescriptor.allowableValues, function (option) {
                    return option.value == property.value;
                });
                if (descriptorOption != undefined && descriptorOption != null) {
                    property.displayValue = descriptorOption.displayName;
                }
            }
        }

        /**
         * Update the property display values for the list of processors
         * @param processors a list of processors that have a list of properties
         */
        this.updateDisplayValueForProcessors = function(processors){
            var self = this;
            if(processors && processors.length) {
                _.each(processors, function (processor) {
                    if (processor.properties) {
                        _.each(processor.properties, function (property) {
                            self.updateDisplayValue(property);
                        })
                    }
                });
            }
        }

    });
});
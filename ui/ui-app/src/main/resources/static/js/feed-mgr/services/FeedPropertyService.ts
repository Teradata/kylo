import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');


// export class FeedPropertyService {

    
    function FeedPropertyService() {

        /**
         * If the property is sensitive we should store off the long encrypted value and show just asterisks
         * @param property
         */
        this.initSensitivePropertyForEditing = function(property:any){
            var self = this;
            if(property.sensitive && !self.isMasked(property.value)){
                property.encryptedValue = property.value;
                property.value = '******';
            }
        };
        /**
         * Return true if every char in value == '*'
         */
        this.isMasked = function(value:any){
           return value != null && _.every(value.split(''),function(char){
                return char == '*';
            });
        };
        /**
         * If the property is sensitive and hasnt changed we should set it back to the encrypted value.
         * @param property
         */
        this.initSensitivePropertyForSaving=function(property:any){
            var self = this;
            if(property.sensitive){
                if(self.isMasked(property.value)){
                    property.value = property.encryptedValue;
                }
                //reset it
                delete property.encryptedValue;
            }
        };

        this.initSensitivePropertiesForEditing = function(properties:any) {
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
        this.updateDisplayValue = function(property:any) {
            property.displayValue = property.value;
            if (property.key == "Source Database Connection" && property.propertyDescriptor != undefined && property.propertyDescriptor.allowableValues) {
                var descriptorOption = _.find(property.propertyDescriptor.allowableValues, function (option:any) {
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
        this.updateDisplayValueForProcessors = function(processors:any){
            var self = this;
            if(processors && processors.length) {
                _.each(processors, function (processor:any) {
                    if (processor.properties) {
                        _.each(processor.properties, function (property) {
                            self.updateDisplayValue(property);
                        })
                    }
                });
            }
        }

    }
// }
angular.module(moduleName).service('FeedPropertyService', FeedPropertyService); 
define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var DefaultFeedPropertyService = /** @class */ (function () {
        function DefaultFeedPropertyService() {
        }
        /**
         * If the property is sensitive we should store off the long encrypted value and show just asterisks
         * @param property
         */
        DefaultFeedPropertyService.prototype.initSensitivePropertyForEditing = function (property) {
            if (property.sensitive && !this.isMasked(property.value)) {
                property.encryptedValue = property.value;
                property.value = '******';
            }
        };
        ;
        /**
         * Return true if every char in value == '*'
         */
        DefaultFeedPropertyService.prototype.isMasked = function (value) {
            return value != null && _.every(value.split(''), function (char) {
                return char == '*';
            });
        };
        ;
        /**
         * If the property is sensitive and hasnt changed we should set it back to the encrypted value.
         * @param property
         */
        DefaultFeedPropertyService.prototype.initSensitivePropertyForSaving = function (property) {
            var self = this;
            if (property.sensitive) {
                if (self.isMasked(property.value)) {
                    property.value = property.encryptedValue;
                }
                //reset it
                delete property.encryptedValue;
            }
        };
        ;
        DefaultFeedPropertyService.prototype.initSensitivePropertiesForEditing = function (properties) {
            var self = this;
            if (properties && properties.length) {
                _.each(properties, function (prop) {
                    self.initSensitivePropertyForEditing(prop);
                });
            }
        };
        ;
        /**
         * Sets the displayValue attribute for the incoming property
         * @param property a NiFiProperty
         */
        DefaultFeedPropertyService.prototype.updateDisplayValue = function (property) {
            property.displayValue = property.value;
            if (property.key == "Source Database Connection" && property.propertyDescriptor != undefined && property.propertyDescriptor.allowableValues) {
                var descriptorOption = _.find(property.propertyDescriptor.allowableValues, function (option) {
                    return option.value == property.value;
                });
                if (descriptorOption != undefined && descriptorOption != null) {
                    property.displayValue = descriptorOption.displayName;
                }
            }
        };
        /**
         * Update the property display values for the list of processors
         * @param processors a list of processors that have a list of properties
         */
        DefaultFeedPropertyService.prototype.updateDisplayValueForProcessors = function (processors) {
            var self = this;
            if (processors && processors.length) {
                _.each(processors, function (processor) {
                    if (processor.properties) {
                        _.each(processor.properties, function (property) {
                            self.updateDisplayValue(property);
                        });
                    }
                });
            }
        };
        return DefaultFeedPropertyService;
    }());
    exports.DefaultFeedPropertyService = DefaultFeedPropertyService;
    angular.module(moduleName).service('FeedPropertyService', DefaultFeedPropertyService);
});
//# sourceMappingURL=DefaultFeedPropertyService.js.map
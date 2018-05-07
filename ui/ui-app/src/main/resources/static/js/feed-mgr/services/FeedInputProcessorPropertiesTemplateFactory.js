define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    function FeedInputProcessorOptionsFactory(UiComponentsService) {
        var data = {
            setFeedProcessingTemplateUrl: function (processor, mode) {
                UiComponentsService.getProcessorTemplates().then(function (templates) {
                    var matchingTemplate = _.find(templates, function (processorTemplate) {
                        return _.find(processorTemplate.processorTypes, function (type) {
                            if (processorTemplate.processorDisplayName != null && processorTemplate.processorDisplayName != undefined && processorTemplate.processorDisplayName != "") {
                                return processor.type == type && processor.name == processorTemplate.processorDisplayName;
                            }
                            else {
                                return processor.type == type;
                            }
                        }) != null;
                    });
                    if (matchingTemplate != null) {
                        if (mode == 'create') {
                            processor.feedPropertiesUrl = matchingTemplate.stepperTemplateUrl;
                        }
                        else {
                            processor.feedPropertiesUrl = matchingTemplate.feedDetailsTemplateUrl;
                        }
                    }
                });
            }
        };
        return data;
    }
    angular.module(moduleName).factory('FeedInputProcessorOptionsFactory', ['UiComponentsService', FeedInputProcessorOptionsFactory]);
});
//# sourceMappingURL=FeedInputProcessorPropertiesTemplateFactory.js.map
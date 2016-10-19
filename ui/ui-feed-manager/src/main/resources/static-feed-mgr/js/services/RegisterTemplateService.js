/*
 * Copyright (c) 2016.
 */

/**
 *
 */
angular.module(MODULE_FEED_MGR).factory('RegisterTemplateService', function ($http, RestUrlService, FeedInputProcessorOptionsFactory, FeedDetailsProcessorRenderingHelper) {

  function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
  }

  var data = {
    /**
     * Properties that require custom Rendering, separate from the standard Nifi Property (key  value) rendering
     * This is used in conjunction with the method {@code this.isCustomPropertyRendering(key)} to determine how to render the property to the end user
     */
    customPropertyRendering: ["metadata.table.targetFormat", "metadata.table.feedFormat"],

    codemirrorTypes: null,
    propertyRenderTypes: [{type: 'text', 'label': 'Text'}, {type: 'number', 'label': 'Number', codemirror: false},
      {type: 'textarea', 'label': 'Textarea', codemirror: false}, {type: 'select', label: 'Select', codemirror: false},
      {type: 'checkbox-custom', 'label': 'Checkbox', codemirror: false}],
    trueFalseRenderTypes: [{type: 'checkbox-true-false', 'label': 'Checkbox', codemirror: false},
      {type: 'select', label: 'Select', codemirror: false}],
    selectRenderType: [{type: 'select', 'label': 'Select', codemirror: false},
      {type: 'radio', label: 'Radio Button', codemirror: false}],
    codeMirrorRenderTypes: [],
    configurationProperties: [],
    metadataProperties: [],
    propertyList: [],
    configurationPropertyMap: {},
    model: null,
    emptyModel: {
      id: null,
      nifiTemplateId: null,
      templateName: '',
      description: '',
      processors: [],
      inputProperties: [],
      additionalProperties: [],
      inputProcessors: [],
      nonInputProcessors: [],
      defineTable: true,
      allowPreconditions: false,
      dataTransformation: false,
      reusableTemplate: false,
      needsReusableTemplate: false,
      ports: [],
      reusableTemplateConnections:[],  //[{reusableTemplateFeedName:'', feedOutputPortName: '', reusableTemplateInputPortName: ''}]
      icon: {title: null, color: null}
    },
    newModel: function () {
      this.model = angular.copy(this.emptyModel);
    },
    resetModel: function () {
      angular.extend(this.model, this.emptyModel);
      this.model.icon = {title: null, color: null}
    },
    init: function () {
      this.newModel();
      this.fetchConfigurationProperties();
      this.fetchMetadataProperties();
      this.getCodeMirrorTypes();
    },
    getModelForSave: function () {
      return {
        properties: this.getSelectedProperties(),
        id: this.model.id,
        description: this.model.description,
        defineTable: this.model.defineTable,
        allowPreconditions: this.model.allowPreconditions,
        dataTransformation: this.model.dataTransformation,
        nifiTemplateId: this.model.nifiTemplateId,
        templateName: this.model.templateName,
        icon: this.model.icon.title,
        iconColor: this.model.icon.color,
        reusableTemplate: this.model.reusableTemplate,
        needsReusableTemplate: this.model.needsReusableTemplate,
        reusableTemplateConnections: this.model.reusableTemplateConnections
      }
    },
    newReusableConnectionInfo: function() {
      return [{reusableTemplateFeedName:'', feedOutputPortName: '', reusableTemplateInputPortName: ''}];
    },
    isSelectedProperty: function (property) {
      var selected = (property.selected || ( property.value != null && property.value != undefined && (property.value.includes("${metadata") || property.value.includes("${config."))) );
      if (selected) {
        property.selected = true;
      }
      return selected;
    },
    getSelectedProperties: function () {
      var self = this;
      var selectedProperties = [];

      angular.forEach(self.model.inputProperties, function (property) {
        if (data.isSelectedProperty(property)) {
          selectedProperties.push(property)
          if (property.processor && property.processor.topIndex != undefined) {
            delete property.processor.topIndex;
          }
        }
      });


      angular.forEach(self.model.additionalProperties, function (property) {
        if (data.isSelectedProperty(property)) {
          selectedProperties.push(property);
          if (property.processor && property.processor.topIndex != undefined) {
            delete property.processor.topIndex;
          }
        }
      });
      return selectedProperties;
    },
    sortPropertiesForDisplay: function (properties) {
      var propertiesAndProcessors = {properties: [], processors: []};

      //sort them by processor name and property key
      var arr = _.chain(properties).sortBy('key').sortBy('processorName').value();
      propertiesAndProcessors.properties = arr;
      //set the initial processor flag for the heading to print
      var lastProcessor = null;
      _.each(arr, function (property, i) {
        if (lastProcessor == null || property.processor.id != lastProcessor) {
          property.firstProperty = true;
          propertiesAndProcessors.processors.push(property.processor);
          property.processor.topIndex = i;
        }
        else {
          property.firstProperty = false;
        }
        lastProcessor = property.processor.id;
      });
      return propertiesAndProcessors;
    },
    fetchConfigurationProperties: function (successFn, errorFn) {

      var self = this;
      if (self.configurationProperties.length == 0) {
        var _successFn = function (response) {
          self.configurationProperties = response.data;
          angular.forEach(response.data, function (value, key) {
            self.propertyList.push({key: key, value: value, description: null, dataType: null, type: 'configuration'});
            self.configurationPropertyMap[key] = value;
          })
          if (successFn) {
            successFn(response);
          }
        }
        var _errorFn = function (err) {
          if (errorFn) {
            errorFn(err)
          }
        }

        var promise = $http.get(RestUrlService.CONFIGURATION_PROPERTIES_URL);
        promise.then(_successFn, _errorFn);
        return promise;
      }

    },
    fetchMetadataProperties: function (successFn, errorFn) {
      var self = this;
      if (self.metadataProperties.length == 0) {
        var _successFn = function (response) {
          self.metadataProperties = response.data;
          angular.forEach(response.data, function (annotatedProperty, i) {
            self.propertyList.push({
              key: annotatedProperty.name,
              value: '',
              dataType: annotatedProperty.dataType,
              description: annotatedProperty.description,
              type: 'metadata'
            });
          })
          if (successFn) {
            successFn(response);
          }
        }
        var _errorFn = function (err) {
          if (errorFn) {
            errorFn(err)
          }
        }

        var promise = $http.get(RestUrlService.METADATA_PROPERTY_NAMES_URL);
        promise.then(_successFn, _errorFn);
        return promise;
      }

    },
    fetchRegisteredReusableFeedInputPorts : function(){

    var successFn = function (response) {
      self.feedInputPortMap = response.data;
    }
    var errorFn = function (err) {

    }
    var promise = $http.get(RestUrlService.ALL_REUSABLE_FEED_INPUT_PORTS);
    promise.then(successFn, errorFn);
    return promise;

  },
    replaceAll: function (str, find, replace) {
      return str.replace(new RegExp(escapeRegExp(find), 'g'), replace);
    },
    deriveExpression: function (expression, configOnly) {
      var self = this;
      var replaced = false;
      if (expression != null && expression != '') {
        var variables = expression.match(/\$\{(.*?)\}/gi);
        if (variables && variables.length) {
          angular.forEach(variables, function (variable) {
            var varNameMatches = variable.match(/\$\{(.*)\}/);
            var varName = null;
            if (varNameMatches.length > 1) {
              varName = varNameMatches[1];
            }
            if (varName) {
              var value = self.configurationPropertyMap[varName];
              if (value) {
                expression = self.replaceAll(expression, variable, value);
                replaced = true;
              }

            }
          });
        }
      }
      if(configOnly == true && !replaced){
        expression = '';
      }
      return expression;
    },
    getCodeMirrorTypes: function () {
      var self = this;
      if (this.codemirrorTypes == null) {
        var successFn = function (response) {
          self.codemirrorTypes = response.data;
          angular.forEach(self.codemirrorTypes, function (label, type) {
            self.propertyRenderTypes.push({type: type, label: label, codemirror: true});
          });
        }
        var errorFn = function (err) {

        }
        var promise = $http.get(RestUrlService.CODE_MIRROR_TYPES_URL);
        promise.then(successFn, errorFn);
        return promise;
      }
      return $q.when(self.codemirrorTypes);
    },
    isRenderPropertyWithCodeMirror: function (property) {
      return this.codemirrorTypes[property.renderType] !== undefined;
    },
    /**
     * Feed Processors can setup separate Templates to have special rendering done for a processors properties.
     * @see /js/define-feed/feed-details/get-table-data-properties.
     *  This
     * @param key
     * @returns {boolean}
     */
    isCustomPropertyRendering: function (key) {
      var self = this;
      var custom = _.find(this.customPropertyRendering, function (customKey) {
        return key == customKey;
      });
      return custom !== undefined;
    },
    removeNonUserEditableProperties: function (processorArray, keepProcessorIfEmpty) {
      //only keep those that are userEditable:true
      var validProcessors = [];
      var processorsToRemove = [];
      //temp placeholder until Register Templates allows for user defined input processor selection

      _.each(processorArray, function (processor, i) {
        var validProperties = _.reject(processor.properties, function (property) {
          return !property.userEditable;
        });
        processor.allProperties = processor.properties;

        processor.properties = validProperties;
        if (validProperties != null && validProperties.length > 0) {
          validProcessors.push(processor);
        }
        if (FeedDetailsProcessorRenderingHelper.isGetTableDataProcessor(processor) || FeedDetailsProcessorRenderingHelper.isWatermarkProcessor(processor)) {
          processor.sortIndex = 0;
        }
        else {
          processor.sortIndex = i;
        }
      });
      var arr = null;

      if (keepProcessorIfEmpty != undefined && keepProcessorIfEmpty == true) {
        arr = processorArray;
      }
      else {
        arr = validProcessors;
      }
      // sort it
      return _.sortBy(arr, 'sortIndex');

    },
    /**
     * Setup the inputProcessor and nonInputProcessor and their properties on the registeredTemplate object
     * used in Feed creation and feed details to render the nifi input fields
     * @param template
     */
    initializeProperties: function (template, mode, feedProperties) {
      //get the saved properties

      var savedProperties = {};
      if (feedProperties) {
        _.each(feedProperties, function (property) {
          if (property.userEditable && property.templateProperty) {
            savedProperties[property.templateProperty.idKey] = property;
          }
        });
      }

      function setRenderTemplateForProcessor(processor) {
        if (processor.feedPropertiesUrl == undefined) {
          processor.feedPropertiesUrl = null;
        }
        if (processor.feedPropertiesUrl == null) {
          processor.feedPropertiesUrl = FeedInputProcessorOptionsFactory.templateForProcessor(processor, mode);

        }
      }

      function updateProperties(processor, properties) {

        _.each(properties, function (property) {
          //set the value if its saved
          if (savedProperties[property.idKey] != undefined) {
            property.value == savedProperties[property.idKey]
          }
          //mark as not selected
          property.selected = false;

          property.value = data.deriveExpression(property.value, false);
          property.renderWithCodeMirror = data.isRenderPropertyWithCodeMirror(property);

          //if it is a custom render property then dont allow the default editing.
          //the other fields are coded to look for these specific properties
          //otherwise check to see if it is editable
          if (data.isCustomPropertyRendering(property.key)) {
            property.customProperty = true;
            property.userEditable = false;
          } else if (property.userEditable == true) {
            processor.userEditable = true;
          }
          property.displayValue = property.value;
          if (property.key == "Source Database Connection" && property.propertyDescriptor != undefined && property.propertyDescriptor.allowableValues) {
            var descriptorOption = _.find(property.propertyDescriptor.allowableValues, function (option) {
              return option.value == property.value;
            });
            if (descriptorOption != undefined && descriptorOption != null) {
              property.displayValue = descriptorOption.displayName;
            }
          }

        })

      }

      _.each(template.inputProcessors, function (processor) {
        //ensure the processorId attr is set
        processor.processorId = processor.id
        updateProperties(processor, processor.properties)
        setRenderTemplateForProcessor(processor);
      });
      _.each(template.nonInputProcessors, function (processor) {
        //ensure the processorId attr is set
        processor.processorId = processor.id
        updateProperties(processor, processor.properties)
        setRenderTemplateForProcessor(processor);
      });

    }

  };
  data.init();
  return data;

});
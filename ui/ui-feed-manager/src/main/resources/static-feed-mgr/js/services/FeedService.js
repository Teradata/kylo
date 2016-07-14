''/*
 * Copyright (c) 2016.
 */

/**
 *
 */
angular.module(MODULE_FEED_MGR).factory('FeedService', function ($http, $q,$mdToast,$mdDialog, RestUrlService, VisualQueryService,FeedCreationErrorService) {


    function trim(str) {
        return str.replace(/^\s+|\s+$/g, "");
    }

    function toCamel(str){
        return str.replace(/(\-[a-z])/g, function($1){return $1.toUpperCase().replace('-','');});
    }

    function toDash(str){
        return str.replace(/([A-Z])/g, function($1){return "-"+$1.toLowerCase();});
    }

    function spacesToUnderscore(str){
        return str.replace(/\s+/g, '_');
    }

    function toUnderscore(str){
        return str.replace(/(?:^|\.?)([A-Z])/g, function (x,y){return "_" + y.toLowerCase()}).replace(/^_/, "")
         //return str.replace(/([A-Z])/g, "_$1").replace(/^_/,'').toLowerCase();
    }



    var data = {

        customPropertyRendering:["metadata.table.targetFormat","metadata.table.feedFormat"],

        createFeedModel : {},
        editFeedModel : {},
        DEFAULT_CRON: "0 0 12 1/1 * ? *",
        getNewCreateFeedModel : function(){
            return  {id:null,versionName:null,templateId:'',feedName:'',description:null,systemFeedName:'',inputProcessorType:'',inputProcessor:null,nonInputProcessors:[],properties:[], schedule:{schedulingPeriod:data.DEFAULT_CRON,schedulingStrategy:'CRON_DRIVEN', concurrentTasks:1},defineTable:false,allowPreconditions:false,dataTransformationFeed:false,table:{tableSchema:{name:null,fields:[]},sourceTableSchema:{name:null,fields:[]},method:'MANUAL',existingTableName:null,targetMergeStrategy:'DEDUPE_AND_MERGE',feedFormat:"ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' STORED AS TEXTFILE",targetFormat:null,fieldPolicies:[],partitions:[],options:{compress:false,compressionFormat:null,auditLogging:true,encrypt:false,trackHistory:false}, securityGroups:[], sourceTableIncrementalDateField:null}, category:{id:null,name:null},  dataOwner:'',tags:[], reusableFeed:false, dataTransformation:{chartViewModel:null,dataTransformScript:null,sql:null,states:[]}};
        },
        newCreateFeed:function(){
            this.createFeedModel = this.getNewCreateFeedModel();
            VisualQueryService.resetModel();
            FeedCreationErrorService.reset();
        },
        updateFeed: function(feedModel){
            var self = this;
            angular.extend(this.editFeedModel,feedModel);

            //set the field name to the policy name attribute
            if(this.editFeedModel.table != null && this.editFeedModel.table.fieldPolicies != null) {
                angular.forEach(this.editFeedModel.table.fieldPolicies, function (policy, i) {
                    var field = self.editFeedModel.table.tableSchema.fields[i];
                    policy.name = field.name;
                    policy.dataType = field.dataType;
                    policy.nullable = field.nullable;
                    policy.primaryKey = field.primaryKey;
                });
            }

        },
        showFeedErrorsDialog:function(){
            return FeedCreationErrorService.showErrorDialog();
        },
        buildErrorData:function(name,nifiFeed){
          FeedCreationErrorService.buildErrorData(name,nifiFeed);
        },
        hasFeedCreationErrors: function() {
            return FeedCreationErrorService.hasErrors();
        },
        isCustomPropertyRendering:function(key){
            var self = this;
           var custom = _.find(this.customPropertyRendering,function(customKey){
                return key == customKey;
            });
            return custom !== undefined;
        },
        findPropertyForProcessor: function(model,processorName,propertyKey){
        var property =  _.find(model.inputProcessor.properties,function(property){
            //return property.key = 'Source Database Connection';
            return property.key == key;
        });

        if(property == undefined) {
            for (processorId in model.nonInputProcessors) {
                var processor = null;
                var aProcessor = model[processorId];
                if (processorName != undefined && processorName != null) {
                    if (aProcessor.processorName == processorName) {
                        processor = aProcessor;
                    }
                }
                else {
                    processor = aProcessor;
                }
                if (processor != null) {
                    property = _.find(processor.properties, function (property) {
                        return property.key == propertyKey;
                    });
                }
                if (property != undefined) {
                    break;
                }
            }
        }
            return property;

        },
        resetFeed : function(){
          angular.extend(this.createFeedModel,this.getNewCreateFeedModel());
            VisualQueryService.resetModel();
            FeedCreationErrorService.reset();
        },

        newTableFieldDefinition: function() {
        return  {name:'',description:'',dataType:'',primaryKey:false,nullable:false,sampleValues:[],selectedSampleValue:''};
        },
        newTableFieldPolicy:function(fieldName) {
            return {name:fieldName||'', partition:null,profile:true,standardization:null,validation:null};
        },
        setTableFields:function(fields){
            var self =this;
            this.createFeedModel.table.tableSchema.fields = [];
            this.createFeedModel.table.fieldPolicies = [];
          angular.forEach(fields,function(field){
              self.createFeedModel.table.fieldPolicies.push(self.newTableFieldPolicy(field.name))
          });
            self.createFeedModel.table.tableSchema.fields = fields;
        },
        syncTableFieldPolicyNames :function() {
            var self = this;;
        angular.forEach(self.createFeedModel.table.tableSchema.fields,function(columnDef,index){
            //update the the policy
            var inArray = index < self.createFeedModel.table.tableSchema.fields.length && index >=0;
            if(inArray) {
                var name = self.createFeedModel.table.tableSchema.fields[index].name;
                if (name != undefined) {
                    self.createFeedModel.table.fieldPolicies[index].name = name;
                }
            }
        });
        //remove any extra columns in the policies
        while(self.createFeedModel.table.fieldPolicies.length > self.createFeedModel.table.tableSchema.fields.length){
            self.createFeedModel.table.fieldPolicies.splice(self.createFeedModel.table.tableSchema.fields.length,1);
        }
        },
        clearTableData:function(){

            this.createFeedModel.table.method = 'MANUAL';
            this.createFeedModel.table.tableSchema.fields = [];
            this.createFeedModel.table.fieldPolicies = [];
            this.createFeedModel.table.existingTableName = null;
        },
        updateEditModelStateIcon: function(){
            if(this.editFeedModel.state == 'ENABLED') {
                this.editFeedModel.stateIcon = 'check_circle'
            }
            else {
                this.editFeedModel.stateIcon = 'block'
            }
        },
        init:function(){
            this.newCreateFeed();
        },
        prepareModelForSave:function(model){
            var properties = [];

            if(model.inputProcessor != null) {
                angular.forEach(model.inputProcessor.properties, function (property) {
                    properties.push(property);
                });
            }

            angular.forEach(model.nonInputProcessors,function(processor){
                angular.forEach(processor.properties,function(property){
                    properties.push(property);
                });
            });
            model.properties = properties;

            // this.createFeedModel.table.tableSchema.fields = this.createFeedModel.table.columnDefinitions;
         //   this.createFeedModel.table.fieldPolicies = this.createFeedModel.table.columnPolicies;
        },
        showFeedSavingDialog:function(ev,message,feedName){
            $mdDialog.show({
                controller: 'FeedSavingDialogController',
                templateUrl: 'js/feed-details/details/feed-saving-dialog.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    message:message,
                    feedName:feedName
                }
            })
                .then(function(answer) {
                    //do something with result
                }, function() {
                    //cancelled the dialog
                });
        },
        hideFeedSavingDialog:function(){
            $mdDialog.hide();
        },
        saveFeedModel:function(model){
            var self = this;
            self.prepareModelForSave(model);
            var deferred = $q.defer();
            var successFn = function (response) {
                var invalidCount = 0;

                if(response.data && response.data.success){

                    //update the feed versionId and internal id upon save
                    model.id = response.data.feedMetadata.id;
                    model.versionName = response.data.feedMetadata.versionName;

                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Saved the Feed, version '+model.versionName)
                            .hideDelay(3000)
                    );
                    deferred.resolve(response);
                }
                else {
                    deferred.reject(response);
                }

            }
            var errorFn = function (err) {
               deferred.reject(err);
            }


            var promise = $http({
                url: RestUrlService.CREATE_FEED_FROM_TEMPLATE_URL,
                method: "POST",
                data: angular.toJson(model),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);


            return deferred.promise;
        },
        getSystemName: function (feedName) {

            return $http.get(RestUrlService.GET_SYSTEM_NAME, {params: {name: feedName}});
            /*
            var controlChars = ["\"","'","!","@","#","$","%","^","&","*","(",")"];
            var systemName = feedName;
            //remove control chars
            if(systemName == undefined) {
                systemName = '';
            }
            for(var i =0; i<controlChars.length; i++){
                systemName = systemName.split(controlChars[i]).join("");
            }
            systemName = trim(systemName);
            systemName = toCamel(systemName);
            systemName = toUnderscore(systemName);
            systemName = spacesToUnderscore(systemName);
            systemName = systemName.split("__").join("_");
            return systemName;
             */

         },
        getColumnDefinitionByName:function(name) {
        return _.find(this.createFeedModel.table.tableSchema.fields,function(columnDef) {
            return columnDef.name == name;
        });
      },
        getFeedNames: function(){

        var successFn = function (response) {
        return response.data;
        }
        var errorFn = function (err) {

        }
        var promise = $http.get(RestUrlService.GET_FEED_NAMES_URL);
        promise.then(successFn, errorFn);
        return promise;

    },
        getPossibleFeedPreconditions: function(){

            var successFn = function (response) {
                return response.data;
            }
            var errorFn = function (err) {
                console.log('ERROR ',err)
            }
            var promise = $http.get(RestUrlService.GET_POSSIBLE_FEED_PRECONDITIONS_URL);
            promise.then(successFn, errorFn);
            return promise;

        }

};
    data.init();
return data;

});

(function () {


    var controller = function ($scope, $mdDialog, message,feedName){
        var self = this;

        $scope.feedName = feedName;
        $scope.message = message;


        $scope.hide = function() {
            $mdDialog.hide();
        };

        $scope.cancel = function() {
            $mdDialog.cancel();
        };


    };

    angular.module(MODULE_FEED_MGR).controller('FeedSavingDialogController',controller);



}());

''/*
 * Copyright (c) 2016.
 */

/**
 *
 */
angular.module(MODULE_FEED_MGR).factory('FeedService', function ($http, $q,$mdToast, RestUrlService, VisualQueryService) {


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

        createFeedModel : {},
        editFeedModel : {},
        getNewCreateFeedModel : function(){
            return  {id:null,version:null,templateId:'',feedName:'',description:null,systemFeedName:'',inputProcessorType:'',inputProcessor:null,nonInputProcessors:[],properties:[], schedule:{schedulingPeriod:'* * * * * ?',schedulingStrategy:'CRON_DRIVEN', concurrentTasks:1},defineTable:false,allowPreconditions:false,dataTransformationFeed:false,table:{tableSchema:{name:null,fields:[]},sourceTableSchema:{name:null,fields:[]},method:'MANUAL',existingTableName:null,tableType:'DELTA',recordFormat:'DELIMITED',fieldPolicies:[],partitions:[],options:{compress:false,compressionFormat:null,auditLogging:true,encrypt:false,trackHistory:false}, securityGroups:[], incrementalDateField:null}, category:{id:null,name:null},  dataOwner:'',tags:[], reusableFeed:false, dataTransformation:{visualQuery:{sql:null,selectedColumnsAndTablesJson:null,chartViewModelJson:null},dataTransformScript:null,formulas:[],state:'NEW'}};
        },
        newCreateFeed:function(){
            this.createFeedModel = this.getNewCreateFeedModel();
            VisualQueryService.resetModel();
        },
        updateFeed: function(feedModel){
            var self = this;
            angular.extend(this.editFeedModel,feedModel);

            //set the field name to the policy name attribute
            if(this.editFeedModel.table != null && this.editFeedModel.table.fieldPolicies != null) {
                angular.forEach(this.editFeedModel.table.fieldPolicies, function (policy, i) {
                    policy.name = self.editFeedModel.table.tableSchema.fields[i].name
                });
            }

        },
        resetFeed : function(){
          angular.extend(this.createFeedModel,this.getNewCreateFeedModel());
            VisualQueryService.resetModel();
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

        saveFeedModel:function(model){
            var self = this;
            self.prepareModelForSave(model);
            var deferred = $q.defer();
            var successFn = function (response) {
                var invalidCount = 0;

                if(response.data && response.data.success){

                    //update the feed versionId and internal id upon save
                    model.id = response.data.feedMetadata.id;
                    model.version = response.data.feedMetadata.version;

                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Saved the Feed, version '+model.version)
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

         },
        getColumnDefinitionByName:function(name) {
        return _.find(this.createFeedModel.table.columnDefinitions,function(columnDef) {
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
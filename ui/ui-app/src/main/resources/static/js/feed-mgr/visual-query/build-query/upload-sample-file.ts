import * as angular from "angular";
import * as _ from "underscore";
import {Input, OnDestroy, OnInit} from "@angular/core";

import {QueryEngine,SampleFile} from "../wrangler/query-engine";
import {SparkConstants} from "../services/spark/spark-constants";
import {FeedDataTransformation} from "../../model/feed-data-transformation";

const moduleName: string = require("feed-mgr/visual-query/module-name");

export class UploadSampleFile implements  OnInit {

    sampleFile: File;

    uploadBtnDisabled:boolean;

    stepperController : any;

    schemaParser:any;

    @Input()
    isValid: boolean;

    @Input()
    sampleFileChanged: boolean;

    @Input()
    model: FeedDataTransformation;
    /**
     * Query engine for determining capabilities.
     */
    @Input()
    engine: QueryEngine<any>;

    public onFileUploaded:() => any;

    static readonly $inject = ["$scope", "$http", "$timeout","$mdToast", "$mdDialog", "FileUpload","RestUrlService"];

    constructor(private $scope:any, private $http:any, private $timeout:any, private $mdToast:any,private $mdDialog:any,private fileUpload:any, private restUrlService:any){


        $scope.$watch(() =>{
            return this.sampleFile
        },(newVal:File, oldVal:File) => {
            if((angular.isUndefined(newVal) || newVal == null) || angular.isUndefined(this.model.sampleFile) || (angular.isDefined(newVal) && angular.isDefined(oldVal) && newVal.name != oldVal.name)){
                this.isValid = false;
            }
            else {
                this.isValid = true;
            }
        })
    }

    $onInit () {
        this.ngOnInit();
    }

    ngOnInit(){

        if(angular.isDefined(this.model.sampleFile) && this.model.sampleFile != null){
            this.sampleFile = this.model.sampleFile.localFileObject;
            this.schemaParser = this.model.sampleFile.schemaParser;
        }
    }


    showProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = true;
        }
    }

    hideProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = false;
        }
    }


    uploadSampleFile() {
        this.model.sampleFile = null;
        this.model.sampleFileChanged = true;
        this.isValid = false;
        this.uploadBtnDisabled = true;
        this.showProgress();
        let file = this.sampleFile;
        let params = {};
        if (this.schemaParser) {
            //limit is set to -1.  -1 or null will imply no limit.
            //limits are taken care of by the 'sample' option which defaults to 1000 limit
            params = {parser: JSON.stringify(this.schemaParser),dataFrameVariable:SparkConstants.DATA_FRAME_VARIABLE,limit:-1};
        }

        let uploadUrl = this.restUrlService.UPLOAD_SPARK_SAMPLE_FILE;
        var successFn =  (response:any) =>{
            var responseData = response.data;
            this.hideProgress();
            this.uploadBtnDisabled = false;
            let fileLocation :string = responseData.fileLocation;
            let script:string = responseData.script;
            let serverSampleFile = <SampleFile>{fileLocation:fileLocation,script:script,localFileName:file.name, localFileObject:file,schemaParser:angular.copy(this.schemaParser)};
            this.engine.setSampleFile(serverSampleFile);
            this.isValid = true;
            this.model.sampleFile = serverSampleFile;
            if(angular.isDefined(this.onFileUploaded)){
                this.onFileUploaded();
            }
        };
        var errorFn =  (data:any) => {
            this.model.sampleFile = null;
            this.hideProgress();
            this.uploadBtnDisabled = false;
            angular.element('#upload-sample-file-btn').removeClass('md-primary');
            angular.element('#uploadButton').addClass('md-primary');
        };

        this.fileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
    };


}


angular.module(moduleName).component("kyloUploadSampleFile", {
    bindings: {
        model:"=",
        engine: "=",
        isValid: "=",
        onFileUploaded: "&?"
    },
    controller: UploadSampleFile,
    controllerAs: "vm",
    require: {
        stepperController: "^thinkbigStepper"
    },
    templateUrl: "js/feed-mgr/visual-query/build-query/upload-sample-file.html"
});

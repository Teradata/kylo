import * as angular from "angular";
import * as _ from "underscore";
import {Input, OnDestroy, OnInit} from "@angular/core";

import {QueryEngine} from "../wrangler/query-engine";
import {SampleFile} from "../wrangler/query-engine";
import {SparkConstants} from "../services/spark/spark-constants";
import {FeedDataTransformation} from "../../model/feed-data-transformation";

const moduleName: string = require("feed-mgr/visual-query/module-name");

export class UploadSampleFile implements  OnInit {

    sampleFile: any;

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

    static readonly $inject = ["$scope", "$http", "$timeout","$mdToast", "$mdDialog", "FileUpload","RestUrlService"];

    constructor(private $scope:any, private $http:any, private $timeout:any, private $mdToast:any,private $mdDialog:any,private fileUpload:any, private restUrlService:any){

    }

    $onInit () {
this.ngOnInit();
    }

    ngOnInit(){

        if(angular.isDefined(this.model.sampleFile)){
            this.isValid = true;
            this.sampleFile = this.model.sampleFile;
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
            params = {parser: JSON.stringify(this.schemaParser),dataFrameVariable:SparkConstants.DATA_FRAME_VARIABLE,limit:100};
        }
        let uploadUrl = this.restUrlService.UPLOAD_SPARK_SAMPLE_FILE;
        var successFn =  (response:any) =>{
            // console.log("loaded schema");
            var responseData = response.data;
            this.hideProgress();
            this.uploadBtnDisabled = false;
            let fileLocation :string = responseData.fileLocation;
            let script:string = responseData.script;
            this.engine.setSampleFile({originalFileName:file,fileLocation:fileLocation,script:script});
            this.isValid = true;
            this.model.sampleFile = file;
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
        isValid: "="
    },
    controller: UploadSampleFile,
    controllerAs: "vm",
    require: {
        stepperController: "^thinkbigStepper"
    },
    templateUrl: "js/feed-mgr/visual-query/build-query/upload-sample-file.html"
});

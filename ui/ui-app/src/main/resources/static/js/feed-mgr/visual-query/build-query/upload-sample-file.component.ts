import * as angular from "angular";
import * as _ from "underscore";
import {Component, EventEmitter, Injector, Input, OnDestroy, OnInit, Output} from "@angular/core";

import {QueryEngine,SampleFile} from "../wrangler/query-engine";
import {SparkConstants} from "../services/spark/spark-constants";
import {FeedDataTransformation} from "../../model/feed-data-transformation";

import {moduleName} from "../module-name";
import {HttpClient} from "@angular/common/http";
import {FileUploadStatus} from "../../catalog/datasource/upload/models/file-upload";
import {TdLoadingService} from "@covalent/core/loading";
import {RestUrlConstants} from "../../services/RestUrlConstants";
import {CloneUtil} from "../../../common/utils/clone-util";
import FileUpload from "../../../services/FileUploadService";


@Component({
    selector:'upload-sample-file',
    templateUrl:'js/feed-mgr/visual-query/build-query/upload-sample-file.component.html'
})
export class UploadSampleFileComponent implements  OnInit {

    /**
     * The file to upload
     */
    sampleFile: File;

    uploadBtnDisabled:boolean;

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

    uploading:boolean = false;

    @Output()
    public onFileUploaded:EventEmitter<any> = new EventEmitter<any>();

    /**
     * The upload service
     */
    private fileUpload:FileUpload;

    constructor(private http:HttpClient, private $$angularInjector: Injector,     private _loadingService:TdLoadingService,) {
        this.fileUpload = this.$$angularInjector.get("FileUpload");

    }

    ngOnInit(){

        if(this.model.sampleFile != undefined && this.model.sampleFile != null){
            this.sampleFile = this.model.sampleFile.localFileObject;
            this.schemaParser = this.model.sampleFile.schemaParser;
        }
    }


    showProgress() {
       this.uploading  = true;
    }

    hideProgress() {
    this.uploading  = false;
    }

     private validate()
    {
        if (angular.isUndefined(this.model.sampleFile ) || this.model.sampleFile  == null) {
            this.isValid = false;
        }
        else {
            this.isValid = true;
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

        let uploadUrl = RestUrlConstants.UPLOAD_SPARK_SAMPLE_FILE;
        var successFn =  (response:any) =>{
            var responseData = response.data;
            this.hideProgress();
            this.uploadBtnDisabled = false;
            let fileLocation :string = responseData.fileLocation;
            let script:string = responseData.script;
            let schemaParser = CloneUtil.deepCopy(this.schemaParser)
            let serverSampleFile = <SampleFile>{fileLocation:fileLocation,script:script,localFileName:file.name, localFileObject:file,schemaParser:schemaParser};
            this.engine.setSampleFile(serverSampleFile);
            this.isValid = true;
            this.model.sampleFile = serverSampleFile;
            this.validate();
            if(!_.isUndefined(this.onFileUploaded)){
                this.onFileUploaded.emit(responseData);
            }

        };
        var errorFn =  (data:any) => {
            this.model.sampleFile = null;
            this.hideProgress();
            this.uploadBtnDisabled = false;
        };

        this.fileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
    }


}


import {HttpClient} from "@angular/common/http";
import {Component, EventEmitter, Inject, Input, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import * as _ from "underscore";

import {CloneUtil} from "../../../common/utils/clone-util";
import {FileUpload} from "../../../services/FileUploadService";
import {FeedDataTransformation} from "../../model/feed-data-transformation";
import {RestUrlConstants} from "../../services/RestUrlConstants";
import {SparkConstants} from "../services/spark/spark-constants";
import {QueryEngine, SampleFile} from "../wrangler/query-engine";

@Component({
    selector: 'upload-sample-file',
    templateUrl: './upload-sample-file.component.html'
})
export class UploadSampleFileComponent implements OnInit {

    /**
     * The file on the model to be passed in and uploaded for schema detection
     */
    sampleFile: File;

    uploadBtnDisabled: boolean;

    schemaParser: any;

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

    policyForm: FormGroup;


    uploading: boolean = false;

    @Output()
    public onFileUploaded: EventEmitter<any> = new EventEmitter<any>();

    constructor(private http: HttpClient, @Inject("FileUpload") private fileUpload: FileUpload) {
        this.policyForm = new FormGroup({});
    }

    ngOnInit() {
        if (this.model.sampleFile != undefined && this.model.sampleFile != null) {
            this.sampleFile = this.model.sampleFile.localFileObject;
            this.schemaParser = this.model.sampleFile.schemaParser;
        }
    }

    showProgress() {
        this.uploading = true;
    }

    hideProgress() {
        this.uploading = false;
    }

    private validate() {
        this.isValid = (this.model.sampleFile != null);
    }

    uploadSampleFile(file: File) {
        this.model.sampleFile = null;
        this.model.sampleFileChanged = true;
        this.isValid = false;
        this.uploadBtnDisabled = true;
        this.showProgress();
        let params = {};
        if (this.schemaParser) {
            //limit is set to -1.  -1 or null will imply no limit.
            //limits are taken care of by the 'sample' option which defaults to 1000 limit
            params = {parser: JSON.stringify(this.schemaParser), dataFrameVariable: SparkConstants.DATA_FRAME_VARIABLE, limit: -1};
        }

        let uploadUrl = RestUrlConstants.UPLOAD_SPARK_SAMPLE_FILE;
        const successFn = (response: any) => {
            const responseData = response.data;
            this.hideProgress();
            this.uploadBtnDisabled = false;
            let fileLocation: string = responseData.fileLocation;
            let script: string = responseData.script;
            let schemaParser = CloneUtil.deepCopy(this.schemaParser);
            let serverSampleFile = <SampleFile>{fileLocation: fileLocation, script: script, localFileName: file.name, localFileObject: file, schemaParser: schemaParser};
            this.engine.setSampleFile(serverSampleFile);
            this.isValid = true;
            this.model.sampleFile = serverSampleFile;
            this.validate();
            if (!_.isUndefined(this.onFileUploaded)) {
                this.onFileUploaded.emit(responseData);
            }

        };
        const errorFn = () => {
            this.model.sampleFile = null;
            this.hideProgress();
            this.uploadBtnDisabled = false;
        };

        this.fileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
    }
}


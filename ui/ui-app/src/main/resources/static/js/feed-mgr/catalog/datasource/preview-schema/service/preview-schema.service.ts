import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {TransformRequest} from "../../../../visual-query/wrangler/model/transform-request";
import {TransformResponse} from "../../../../visual-query/wrangler/model/transform-response";
import {PageSpec} from "../../../../visual-query/wrangler/query-engine";
import {SchemaParser} from "../../../../model/field-policy";
import {SampleFile} from "../../../../model/feed-data-transformation";
import {TableViewModel} from "../model/table-view-model";
import {PreviewDataSet} from "../model/preview-data-set";
import {PreviewDataSetRequest} from "../model/preview-data-set-request"
import {PreviewFileDataSet} from "../model/preview-file-data-set";
import {PreviewJdbcDataSet} from "../model/preview-jdbc-data-set";
import {Subject} from "rxjs/Subject";
import {AbstractSchemaTransformService} from "./abstract-schema-transform-service";
import {TransformResponseTableBuilder} from "./transform-response-table-builder";



@Injectable()
export class PreviewSchemaService  extends AbstractSchemaTransformService{




    constructor(http: HttpClient, transformResponeTableBuilder:TransformResponseTableBuilder) {
        super(http,transformResponeTableBuilder)
    }

    /**
     *
     * Previews data abd populates the previewDataSet object
     * @param {PreviewDataSet} previewDataSet
     * @param {DataSetPreviewRequest} previewRequest
     */
    preview(previewDataSet: PreviewDataSet, previewRequest: PreviewDataSetRequest):Observable<PreviewDataSet>{

        if (!previewDataSet.hasPreview()) {
            //Show Progress Bar
            previewDataSet.loading = true;

            this._transform(previewRequest,"/proxy/v1/spark/shell/preview").subscribe((data: TransformResponse) => {
                let preview = this.transformResponeTableBuilder.buildTable(data);
                previewDataSet.finishedLoading()
                previewDataSet.clearPreviewError()
                previewDataSet.schema = preview.columns;
                previewDataSet.preview =preview;
                this.previewDataSetSource.next(previewDataSet)
            }, error1 => {
                previewDataSet.finishedLoading()
                previewDataSet.previewError("Error previewing the data " + error1);
                this.previewDataSetSource.error(previewDataSet)
            })
            return this.previewedDataSet$;
        }
        else {
            return Observable.of(previewDataSet);
        }
    }




}
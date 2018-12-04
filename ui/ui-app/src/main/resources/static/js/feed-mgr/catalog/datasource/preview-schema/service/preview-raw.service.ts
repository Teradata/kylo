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
import {PreviewJdbcDataSet} from "..//model/preview-jdbc-data-set";
import {Subject} from "rxjs/Subject";
import {AbstractSchemaTransformService} from "./abstract-schema-transform-service";
import {TransformResponseTableBuilder} from "./transform-response-table-builder";



@Injectable()
export class PreviewRawService  extends AbstractSchemaTransformService{

    constructor(http: HttpClient, transformResponeTableBuilder:TransformResponseTableBuilder) {
        super(http,transformResponeTableBuilder)
    }

    /**
     * Transform the first file in the dataset
     * @param {PreviewFileDataSet} previewDataSet
     * @return {Observable<TransformResponse>}
     */
    transformRaw(previewDataSet: PreviewFileDataSet):Observable<TransformResponse> {
        let firstFile = previewDataSet.files[0].filePath;
        let sparkScript = "var df = sqlContext.read.format(\"text\").load(\""+firstFile+"\")";
        let sparkScriptWithLimit = this.limitSparkScript(sparkScript);
        return  this.transform(sparkScriptWithLimit);
    }

    /**
     * Preview the data with inline RAW script
     * NOTE its preferred to use the previewAsTextOrBinary method in the preview-schema.service.ts
     * @param {PreviewFileDataSet} previewDataSet
     * @return {Observable<PreviewDataSet>}
     */
    preview(previewDataSet: PreviewFileDataSet) :Observable<PreviewDataSet>{

        if(previewDataSet.raw == undefined && previewDataSet.allowsRawView) {
            let previewDataSetSource = new Subject<PreviewDataSet>()
            let previewedDataSet$ = previewDataSetSource.asObservable();

            previewDataSet.start(true)

           this.transformRaw(previewDataSet).subscribe((data: TransformResponse) => {
                let preview = this.transformResponeTableBuilder.buildTable(data);
                previewDataSet.success(preview,true)
                previewDataSetSource.next(previewDataSet)
            }, error1 => {
               previewDataSet.error(true,"Error previewing data "+error1)
                previewDataSet.rawLoading = false;
                previewDataSet.rawError("Error previewing the raw data " + error1)
                previewDataSetSource.error(previewDataSet)
            });
            return previewedDataSet$;
        }
        else {
            return Observable.of(previewDataSet);
        }
    }

    limitSparkScript(sparkScript:string) {
        //LIVY doesnt like the trailing df variable.
        //Spark Shell needs it
        let appendTrailingDf = true;
        let sparkScriptWithLimit = "import org.apache.spark.sql._\n" + sparkScript + "\ndf=df.limit(20)\n";
        if(appendTrailingDf) {
            sparkScriptWithLimit+="df";
        }
        return sparkScriptWithLimit;
    }



    transform(script:string) :Observable<TransformResponse>{
        let request: TransformRequest = {
            script:script,
            pageSpec:new PageSpec({firstRow : 0,numRows : 20, firstCol : 0, numCols : 100}),
            doProfile:false,
            doValidate:false
        }
        return this._transform(request,"/proxy/v1/spark/shell/transform/");
    }



}
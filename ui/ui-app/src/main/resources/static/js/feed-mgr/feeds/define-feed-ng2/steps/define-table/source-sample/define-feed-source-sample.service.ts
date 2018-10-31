import {DataSource} from "../../../../../catalog/api/models/datasource";
import {Injectable} from "@angular/core";
import {PreviewDataSet} from "../../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../../model/feed/feed-constants";
import {DefineFeedService} from "../../../services/define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedLoadingService} from "../../../services/feed-loading-service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Feed} from "../../../../../model/feed/feed.model";
import {Step} from "../../../../../model/feed/feed-step.model";
import {HttpClient} from "@angular/common/http";
import {SparkDataSet} from "../../../../../model/spark-data-set.model";
import {PreviewFileDataSet} from "../../../../../catalog/datasource/preview-schema/model/preview-file-data-set";
import {Observable} from "rxjs/Observable";
import {RestUrlConstants} from "../../../../../services/RestUrlConstants";


@Injectable()
export class DefineFeedSourceSampleService {
    constructor(private http:HttpClient){

    }

    public parseSchema(previewDataSet:PreviewFileDataSet) : Observable<any>{

        let ds:SparkDataSet = previewDataSet.toSparkDataSet();
        let schemaParser = previewDataSet.schemaParser;
        let request = {dataSet:ds,schemaParser:schemaParser};
        let url = RestUrlConstants.SCHEMA_DISCOVERY_PARSE_DATA_SET;
        return <Observable<any>> this.http.post(url,request,{ headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }});
    }

    public parseTableSettings(previewDataSet:PreviewFileDataSet) : Observable<any>{

        let ds:SparkDataSet = previewDataSet.toSparkDataSet();
        let schemaParser = previewDataSet.schemaParser;
        let request = {dataSet:ds,schemaParser:schemaParser};
        let url = RestUrlConstants.SCHEMA_DISCOVERY_TABLE_SETTINGS_DATA_SET;
        return <Observable<any>> this.http.post(url,request,{ headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }});
    }



}
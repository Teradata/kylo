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
import {DataSource} from "../../../api/models/datasource";
import {PreviewDatasetCollectionService} from "../../../api/services/preview-dataset-collection.service";



@Injectable()
export class PreviewSchemaService  extends AbstractSchemaTransformService{


    cache:  {[key: string]: PreviewDataSet} = {}

    public cacheKey(datasourceId:string, datasetKey:string){
        return datasourceId+"_"+datasetKey;
    }




    constructor(http: HttpClient, transformResponeTableBuilder:TransformResponseTableBuilder,  private previewDatasetCollectionService : PreviewDatasetCollectionService) {
        super(http,transformResponeTableBuilder)
    }

    public updateDataSetsWithCachedPreview(datasets:PreviewDataSet[]){
        datasets.filter(dataset => !dataset.hasPreview()).forEach(dataset => {
            let cachedPreview = this.cache[this.cacheKey(dataset.dataSource.id, dataset.key)];
            if(cachedPreview){
                dataset.clearPreviewError();
                dataset.preview = cachedPreview.preview;
                dataset.schema = cachedPreview.schema;
                console.log("Updated dataset",dataset," with cached preview")
            }
        })
    }

    /**
     *
     * Previews data abd populates the previewDataSet object
     * @param {PreviewDataSet} previewDataSet
     * @param {DataSetPreviewRequest} previewRequest
     */
    preview(previewDataSet: PreviewDataSet, previewRequest: PreviewDataSetRequest, collect:boolean = false):Observable<PreviewDataSet>{

if (!previewDataSet.hasPreview()) {

            //Show Progress Bar
            previewDataSet.loading = true;

            let previewDataSetSource = new Subject<PreviewDataSet>()
            let previewedDataSet$ = previewDataSetSource.asObservable();
            if(!previewRequest.hasPreviewPath()){
                previewDataSet.applyPreviewRequestProperties(previewRequest);
            }

            this._transform(previewRequest,"/proxy/v1/spark/shell/preview").subscribe((data: TransformResponse) => {
                let preview = this.transformResponeTableBuilder.buildTable(data);
                previewDataSet.finishedLoading()
                previewDataSet.clearPreviewError()
                previewDataSet.schema = preview.columns;
                previewDataSet.preview =preview;

                this.cache[this.cacheKey(previewDataSet.dataSource.id, previewDataSet.key)] = previewDataSet;
                if(collect){
                    this.addToCollection(previewDataSet);
                }
                previewDataSetSource.next(previewDataSet)
            }, error1 => {
                previewDataSet.finishedLoading()
                previewDataSet.previewError("Error previewing the data " + error1);
                previewDataSetSource.error(previewDataSet)
            })
            return previewedDataSet$;
        }
        else {
            if(collect){
                this.previewDatasetCollectionService.addDataSet(previewDataSet);
            }
            return Observable.of(previewDataSet);
        }
    }


    /**
     *  this.selectedDataSet.previewError("unable to preview dataset ");
     if(this.selectedDataSet.allowsRawView) {
                    this.loadRawData();
                }
     */

    /**
     * add the dataset
     * @param {PreviewDataSet} dataset
     */
    addToCollection(dataset: PreviewDataSet){
        this.previewDatasetCollectionService.addDataSet(dataset);
    }




}
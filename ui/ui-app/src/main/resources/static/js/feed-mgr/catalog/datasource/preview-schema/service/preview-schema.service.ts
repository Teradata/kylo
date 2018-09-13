import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import 'rxjs/operator/filter';
import 'rxjs/operators/defaultIfEmpty';
import {map, startWith, flatMap, defaultIfEmpty,filter,share, first} from 'rxjs/operators';
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {TransformRequest} from "../../../../visual-query/wrangler/model/transform-request";
import {TransformResponse} from "../../../../visual-query/wrangler/model/transform-response";
import {PageSpec} from "../../../../visual-query/wrangler/query-engine";
import {SchemaParser, SchemaParserType} from "../../../../model/field-policy";
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
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {SparkDataSet} from "../../../../model/spark-data-set.model";
import {PreviewRawService} from "./preview-raw.service";
import {FieldPolicyOptionsService} from "../../../../shared/field-policies-angular2/field-policy-options.service";
import {CloneUtil} from "../../../../../common/utils/clone-util";
import * as _ from "underscore"
import {ReplaySubject} from "rxjs/ReplaySubject";


@Injectable()
export class PreviewSchemaService  extends AbstractSchemaTransformService{


    cache:  {[key: string]: PreviewDataSet} = {}

    public cacheKey(datasourceId:string, datasetKey:string){
        return datasourceId+"_"+datasetKey;
    }




    constructor(http: HttpClient, transformResponeTableBuilder:TransformResponseTableBuilder,  private previewDatasetCollectionService : PreviewDatasetCollectionService,
                private previewRawService:PreviewRawService,
                private _fieldPolicyOptionsService:FieldPolicyOptionsService) {
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
     * Return the Kylo Spark Text file parser
     * @return {Observable<any>}
     */
    getTextParser(): Observable<SchemaParser> {
        let subject = new ReplaySubject<SchemaParser>(1)
        let observable$ = subject.asObservable();

        let parsers = this._fieldPolicyOptionsService.getSparkSchemaParsers().subscribe((parsers:SchemaParser[]) => {
            let parser = parsers.find((parser:SchemaParser) => parser.name == SchemaParserType.TEXT_BINARY);
            if(parser){
                subject.next(parser);
            }
            else {
                subject.next(undefined);
            }
        });
        //const parser =  parsers.pipe(first((parser:SchemaParser) => parser.name == SchemaParserType.TEXT_BINARY));
        //return parser;
        return observable$;
    }


    /**
     * preview data as either text or binary
     * @param {PreviewDataSet} previewDataSet
     * @param {boolean} binary
     * @param {boolean} rawData
     * @return {Observable<PreviewDataSet>}
     */
    previewAsTextOrBinary(previewDataSet: PreviewDataSet, binary:boolean, rawData:boolean):Observable<PreviewDataSet>{
        let previewDataSetSource = new Subject<PreviewDataSet>()
        let previewedDataSet$ = previewDataSetSource.asObservable();

        let hasPreview = rawData ? previewDataSet.hasRaw() : previewDataSet.hasPreview();
        if(!hasPreview) {

            this.getTextParser().pipe(defaultIfEmpty({}))
                .subscribe((txtParser: SchemaParser) => {
                if(_.isEmpty(txtParser)){
                    //Txt parser not supported.. default to old preview code
                    this.previewRawService.preview(<PreviewFileDataSet>previewDataSet).subscribe((res: PreviewDataSet) => {
                        previewDataSet.success(res.preview,rawData);
                        previewDataSetSource.error(previewDataSet)
                        previewDataSetSource.complete();
                    }, (error1: any) => {
                        previewDataSet.error(rawData,"Error previewing data "+error1);
                        previewDataSetSource.error(previewDataSet)
                        previewDataSetSource.complete();
                    });
                }
                else {
                    let request2 = new PreviewDataSetRequest();
                    request2.dataSource = previewDataSet.dataSource;
                   let parser = CloneUtil.deepCopy(txtParser);
                    request2.schemaParser = parser;
                        (<PreviewFileDataSet>previewDataSet).schemaParser = parser;

                        let binaryProperty = request2.schemaParser.properties.find(p => p.name == "Binary");
                        if(binaryProperty){
                            binaryProperty.value = binary? "true" :"false";
                        }

                     this.preview(previewDataSet,request2,rawData).subscribe(dataset => {
                         previewDataSetSource.next(dataset)
                         previewDataSetSource.complete();
                     },
                     error1 => {
                         previewDataSet.error(rawData,"Error loading raw data")
                         previewDataSetSource.error(previewDataSet)
                         previewDataSetSource.complete();
                     });
                }
            });
        }
         return previewedDataSet$;;
    }

    /**
     * Preview a dataset given a request
     *
     * @param {PreviewDataSet} previewDataSet the dataset to preview
     * @param {PreviewDataSetRequest} previewRequest  the request to send to the server
     * @param {boolean} rawData are you trying to show the raw data?  default false
     * @param {boolean} collect do you want to add this dataset to the collection bag ? default false
     * @return {Observable<PreviewDataSet>}
     */
    preview(previewDataSet: PreviewDataSet, previewRequest: PreviewDataSetRequest, rawData:boolean=false, collect:boolean = false):Observable<PreviewDataSet>{

        let hasPreview = rawData ? previewDataSet.hasRaw() : previewDataSet.hasPreview();
        if(!hasPreview) {
            //set the spark options
            if(!previewDataSet.hasSparkOptions() && previewRequest.schemaParser)
            {
                previewDataSet.sparkOptions = this.getSchemaParserSparkOptions(previewRequest.schemaParser);
            }

            if(!previewDataSet.hasSparkOptions() && previewDataSet instanceof PreviewFileDataSet){
                previewDataSet.sparkOptions = this.getSchemaParserSparkOptions((<PreviewFileDataSet>previewDataSet).schemaParser);
            }

            let previewDataSetSource = new ReplaySubject<PreviewDataSet>(1)
            let previewedDataSet$ = previewDataSetSource.asObservable();
            if(!previewRequest.hasPreviewPath()){
                previewDataSet.applyPreviewRequestProperties(previewRequest);
                previewDataSet.applySparkOptions(previewRequest);
            }

            //Call spark shell to transform the data
            this._transform(previewRequest,"/proxy/v1/spark/shell/preview").subscribe((data: TransformResponse) => {
                let preview = this.transformResponeTableBuilder.buildTable(data);
                previewDataSet.success(preview,rawData);

                if(previewDataSet.dataSource == undefined && previewRequest.dataSource != undefined){
                    previewDataSet.dataSource = previewRequest.dataSource;
                }
                if(!rawData) {
                    this.cache[this.cacheKey(previewDataSet.dataSource.id, previewDataSet.key)] = previewDataSet;
                    if (collect) {
                        this.addToCollection(previewDataSet);
                    }
                }
                previewDataSetSource.next(previewDataSet)
                previewDataSetSource.complete();
            }, error1 => {
                //if Errored out with a preview (not when viewing the raw), attempt to preview the data with the generic Text format parser

                //indicate the error on the preview pane.
                previewDataSet.error(rawData,"Error previewing the data " + error1);

                //attempt to read the data using raw text
                if(!rawData && previewDataSet instanceof PreviewFileDataSet && (!previewRequest.schemaParser || (previewRequest.schemaParser && previewRequest.schemaParser.name != SchemaParserType.TEXT_BINARY))) {
                    //preview the data as text
                    this.previewAsTextOrBinary(previewDataSet,false,rawData).subscribe((dataset:PreviewDataSet) => {
                        previewDataSetSource.next(previewDataSet)
                        previewDataSetSource.complete();
                    },
                        error2 => {
                            previewDataSetSource.error(previewDataSet)
                            previewDataSetSource.complete();
                        })

                }
                else if(!rawData) {
                    previewDataSet.error(rawData,"Unable to preview the data " + error1);
                    previewDataSetSource.error(previewDataSet)
                    previewDataSetSource.complete();
                }

            });

        return previewedDataSet$;
        }
        else {
            if(!rawData && collect){
                this.previewDatasetCollectionService.addDataSet(previewDataSet);
            }
            return Observable.of(previewDataSet);
        }



    }


    /**
     * add the dataset
     * @param {PreviewDataSet} dataset
     */
    addToCollection(dataset: PreviewDataSet){
        this.previewDatasetCollectionService.addDataSet(dataset);
    }


    getSchemaParserSparkOptions(schemaParser:SchemaParser){
        let sparkOptions : { [key: string]: string } = {};
        if(schemaParser){
            if(schemaParser.properties){
                schemaParser.properties.forEach(policy => {
                    let value = policy.value;
                    if(policy.additionalProperties) {
                        let options = policy.additionalProperties.filter(p => "spark.option" == p.label).forEach(lv => {
                            sparkOptions[lv.value] = value
                        });
                    }
                })
            }
            sparkOptions["format"] = schemaParser.sparkFormat;
        }
        return sparkOptions;
    }

}
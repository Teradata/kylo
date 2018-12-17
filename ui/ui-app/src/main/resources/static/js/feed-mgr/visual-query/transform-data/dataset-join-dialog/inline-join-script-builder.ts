import {JoinData} from "./join-preview-stepper-step";
import {ResTarget, SqlBuilderUtil, VisualQueryService} from "../../../services/VisualQueryService";
import {FeedDataTransformation} from "../../../model/feed-data-transformation";
import {DATASET_PROVIDER, SparkQueryParser} from "../../services/spark/spark-query-parser";
import {SparkConstants} from "../../services/spark/spark-constants";
import {JoinDataset} from "../../wrangler/model/join-dataset.model";
import {QueryResultColumn} from "../../wrangler";

export class InlineJoinScriptBuilder {

    /**
     * Map of the colum names to ensure unique select names
     */
    names :{ [s: string]: string; }= {};

    constructor(private visualQueryService:VisualQueryService, private model:FeedDataTransformation, private currentColumns:QueryResultColumn[], private joinData:JoinData){

    }

    private uniqueName = (name:string,cnt?:number):string => {
        if(cnt == undefined){
            cnt =0;
        }
        if(this.names[name] != undefined){
            cnt++;
            name+=cnt;
            return this.uniqueName(name,cnt);
        }
        this.names[name]= name;
        return name;
    }

    private getDataFrameColumns(df:string):ResTarget[] {
       return this.currentColumns.map(col => {
           let fields: string[] = [];
           fields.push(df)
           fields.push(col.hiveColumnLabel);
           this.names[col.hiveColumnLabel]=col.hiveColumnLabel;
           this.names[col.displayName]=col.displayName;
           let t: ResTarget = {description: "", val: {fields: fields}, name:col.displayName};
           return t;
       })
    }

    private getJoinSelectColumns(joinDf:string):ResTarget[] {
       return  this.joinData.joinDataSet.schema.map(field => {
            let fields: string[] = [];
            fields.push(joinDf)
            fields.push(field.name);
            let name = this.joinData.ds.getTableName() + "_" +field.name;
            name = name.replace(/[^a-zA-Z0-9_\s\)\(-]*/g,'');
            name = name.replace(" ","_")
            name = name.replace(".","_")
            name = this.uniqueName(name);
            let t: ResTarget = {description: "", val: {fields: fields}, name:name};
            return t;
        });
    }


    build():JoinDataset{

        let dsProvider = DATASET_PROVIDER;
        let dsId = this.joinData.ds.id;

        let dfField = this.joinData.dataFrameField;
        let joinField = this.joinData.joinField;
        let parser = new SparkQueryParser(this.visualQueryService)
        let joinType = parser.parseJoinType(SqlBuilderUtil.getJoinType(this.joinData.joinType));
        let isNewJoinDf = this.model.inlineJoinDataSets == null || this.model.inlineJoinDataSets == undefined || this.model.inlineJoinDataSets[dsId] == undefined;
        let joinDf = this.model.getJoinDataFrameId(dsId);
        let df = SparkConstants.DATA_FRAME_VARIABLE;


        let dfSelect = this.getDataFrameColumns(df);
        let joinSelect = this.getJoinSelectColumns(joinDf);

        let targetSelect:ResTarget[] = dfSelect.concat(joinSelect);
        let joinSelectString = parser.joinSelect(targetSelect);

        //get the new name in the select if we renamed it
        let joinScript = "";
        if(isNewJoinDf) {
            joinScript = `val ${joinDf} = ${dsProvider}.read("${dsId}").alias("${joinDf}")
                    `
        }
        joinScript += `
                    ${df} = ${df}.join(${joinDf},${df}.col("${dfField}").equalTo(${joinDf}.col("${joinField}")),"${joinType}")${joinSelectString}
                    
                `

        let joinDataset: JoinDataset = {datasetId:dsId,dataframeId:1,joinScript:joinScript, joinField:joinField, dfField:dfField};
        return joinDataset;
    }





}
import {JoinData, JoinMode, UnionField} from "./join-preview-stepper-step";
import {ResTarget, SqlBuilderUtil, VisualQueryService} from "../../../services/VisualQueryService";
import {FeedDataTransformation} from "../../../model/feed-data-transformation";
import {DATASET_PROVIDER, SparkQueryParser} from "../../services/spark/spark-query-parser";
import {SparkConstants} from "../../services/spark/spark-constants";
import {JoinDataset} from "../../wrangler/model/join-dataset.model";
import {QueryResultColumn} from "../../wrangler";

export class UnionMatchModel {
    matchedCols: { [s: string]: UnionField; } = {};
    unmatchedCols: UnionField[] = [];
    unionModel:UnionField[];

}

export class InlineJoinScriptBuilder {

    /**
     * Map of the colum names to ensure unique select names
     */
    names :{ [s: string]: string; }= {};

    constructor(private visualQueryService:VisualQueryService, private model:FeedDataTransformation, private currentColumns:QueryResultColumn[], private joinData:JoinData){

    }

    private uniqueName = (name:string, tableName:string,cnt?:number):string => {
        if(cnt == undefined){
            cnt =0;
        }
        if(this.names[name] != undefined){
            if(cnt ==0){
                name = tableName+"_"+name;
                cnt++;
            }
            else {
                cnt++;
                name += cnt;
            }
            return this.uniqueName(name,tableName,cnt);
        }
        this.names[name]= name;
        return name;
    }

    private getDataFrameColumns(df:string):ResTarget[] {
       return this.currentColumns.map(col => this.queryResultColumnToResTarget(col,df));
    }

    private queryResultColumnToResTarget(col:QueryResultColumn,df:string){
        let fields: string[] = [];
        fields.push(df)
        fields.push(col.hiveColumnLabel);
        this.names[col.hiveColumnLabel]=col.hiveColumnLabel;
        this.names[col.displayName]=col.displayName;
        let t: ResTarget = {description: null, val: {fields: fields}, name:col.displayName};
        return t;
    }

    private fieldToResTarget(field:string,df:string, nullColumn :boolean = false, uniqueName:boolean = true) {
        let fields: string[] = [];
        if (!nullColumn) {
            fields.push(df)
            fields.push(field);
        }
        let name = field;
        if (uniqueName) {

        name = name.replace(/[^a-zA-Z0-9_\s\)\(-]*/g, '');
        name = name.replace(" ", "_")
        name = name.replace(".", "_")
        let cnt = 0;
        if (SparkConstants.RESERVED_COLUMN_NAMES.indexOf(name) >= 0) {
            name = this.joinData.ds.getTableName() + "_" + name;
            cnt = 1;
        }
        name = this.uniqueName(name, this.joinData.ds.getTableName(), cnt);
        }
        let t: ResTarget = {description: null, val: {fields: fields}, name:name};
        return t;
    }


    private getJoinSelectColumns(joinDf:string):ResTarget[] {
        return  this.joinData.joinFields.map(field => this.fieldToResTarget(field,joinDf));
    }

    private getUnionMatchModel(): UnionMatchModel{
        let matchedCols: { [s: string]: UnionField; } = {};
        let unmatchedCols: UnionField[] = [];
        this.joinData.unionModel.filter(unionField => !unionField.removed).forEach(unionField => {
            if (UnionField.UNMATCHED_VALUE == unionField.dataFrameField) {
                unmatchedCols.push(unionField);
            }
            matchedCols[unionField.dataFrameField] = unionField;

        })
        return {matchedCols:matchedCols,unmatchedCols:unmatchedCols,unionModel:this.joinData.unionModel};

    }

    private getUnionDataFrameColumns(df:string):ResTarget[] {
        //first get the current columns
        let cols: ResTarget[] = this.getDataFrameColumns(df);

        let matchModel = this.getUnionMatchModel();

        //add in additional "no match" columns from the union model as null values
        let nullCols = matchModel.unmatchedCols.map(unionField => {
            let target = this.fieldToResTarget(unionField.joinFieldName, df, true);
            //update the joinFieldName in case it got renamed
            unionField.joinFieldName = target.name;
            return target;
        });
        //join the columns
        cols = cols.concat(nullCols)
        return cols;
    }

    /**
     * get the matching union select from the joining dataframe
     * @param joinDf the name of the joining dataframe
     * @param cols the entire select columns including those unmatched from the joining data frame
     */
    private getUnionJoinColumns(joinDf:string,cols:ResTarget[]) {

        let matchModel = this.getUnionMatchModel();


        //get the matching columns for the union
        return cols.map(target => {
            //find match in joiningDF, or make null if its not there
            if(target.val.fields.length == 0){
               //case when joining dataframe has a new column not in the current dataframe
               // select the column from the joining dataframe
                let name = target.name;
                let unionField = matchModel.unmatchedCols.find(unionField => unionField.joinFieldName == name);
                return this.fieldToResTarget(unionField.joinField.name,joinDf)
            }
            else {
                //select the matching field from the union DF
                let unionField = matchModel.matchedCols[target.val.fields[1]];
                if(unionField != undefined) {
                    //when the data matches select it
                    return this.fieldToResTarget(unionField.joinField.name, joinDf)
                }
                else {
                    //its null
                    return this.fieldToResTarget(target.name, joinDf, true,false)
                }
            }

        });


    }

    build():JoinDataset{

        let dsProvider = DATASET_PROVIDER;
        let dsId = this.joinData.ds.id;
        let parser = new SparkQueryParser(this.visualQueryService)
        let isNewJoinDf = this.model.inlineJoinDataSets == null || this.model.inlineJoinDataSets == undefined || this.model.inlineJoinDataSets[dsId] == undefined;
        let joinDf = this.model.getJoinDataFrameId(dsId);
        let df = SparkConstants.DATA_FRAME_VARIABLE;



        //get the new name in the select if we renamed it
        let joinScript = "";
        let joinDataFrameVarScript = "";
        joinDataFrameVarScript = `val ${joinDf} = ${dsProvider}.read("${dsId}").alias("${joinDf}")                    `
        let joinField = ''
        let dfField = '';

        if(this.joinData.joinOrUnion == JoinMode.JOIN) {
            dfField = this.joinData.dataFrameField;
            let joinType = parser.parseJoinType(SqlBuilderUtil.getJoinType(this.joinData.joinType));
            joinField = this.joinData.joinField;

            let dfSelect = this.getDataFrameColumns(df);
            let joinSelect = this.getJoinSelectColumns(joinDf);

            let targetSelect:ResTarget[] = dfSelect.concat(joinSelect);
            let joinSelectString = parser.joinSelect(targetSelect);
            joinScript += `
                    ${df} = ${df}.join(${joinDf},${df}.col("${dfField}").equalTo(${joinDf}.col("${joinField}")),"${joinType}")${joinSelectString}
                                        
                `
        }
        else  if(this.joinData.joinOrUnion == JoinMode.UNION) {

            let dfCols = this.getUnionDataFrameColumns(df)
            let joinCols =    this.getUnionJoinColumns(joinDf, dfCols)
            let select1 = parser.joinSelect(dfCols,false);
            let select2 = parser.joinSelect(joinCols,false);
            joinScript += `
                    ${df} = ${df}${select1}.unionAll(${joinDf}${select2})                                        
                    `
        }

        let joinDataset: JoinDataset = {datasetId:dsId,dataframeId:joinDf,joinScript:joinScript, joinDataFrameVarScript:joinDataFrameVarScript,joinField:joinField, dfField:dfField};
        return joinDataset;
    }





}
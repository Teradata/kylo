import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {TransformRequest, TransformResponse} from "../../../visual-query/wrangler";
import {PageSpec} from "../../../visual-query/wrangler/query-engine";
import {SchemaParser} from "../../../model/field-policy";
import {SampleFile} from "../../../model/feed-data-transformation";

export interface UpdatedParserWithScript {
    updatedScript:string;
    results:TransformResponse;
}

@Injectable()
export class FilePreviewService  {

    /**
     * Array of the selected file paths that have been detected
     */
   private result:TransformResponse;


    constructor(private http: HttpClient) {

    }

    updateParserSettings(files:string[],schemaParser:SchemaParser) : Observable<UpdatedParserWithScript>{
        let request :any = {
        files:files, parserDescriptor:JSON.stringify(schemaParser),dataFrameVariable:"df"
        }
        let observable = new Observable<UpdatedParserWithScript>((observer) => {
            this.http.post("/proxy/v1/schema-discovery/spark/files-list", request)
                .subscribe((data: SampleFile) => {
                    console.log('script', data)
                        let script = data.script;
                        script = "import org.apache.spark.sql._\n"+script+"\ndf=df.limit(20)\n df";

                    this.transform(script).subscribe((result) => {
                        let returnResults :UpdatedParserWithScript = {updatedScript:script,results:result};
                        observer.next(returnResults)
                    },error1 => observer.error({updatedScript:{script:''},result:error1}))

                },error1 => observer.error({updatedScript:{script:''},result:error1}));
        });
        return observable;

        // call /spark/files-list, {files:this.selectedFilePaths, parserDescriptor:this.selectedParser,dataFrameVariable:"df"} .then  preview()

        //1. call rest endpoint  generateSparkScriptForSchemaParser(this.selectedParser, this.selectedFilePaths).subscribe(script) =>  then execute the script

    }


    transform(script:string) :Observable<TransformResponse>{

        let observable = new Observable<TransformResponse>((observer) => {

            let request: TransformRequest = {
                script:script,
                pageSpec:new PageSpec(),
                doProfile:false,
                doValidate:false
            }
            let statusCheckTime = 300

            let formatDetected = (data: TransformResponse):void => {
                console.log('FORMAT DETECTED ', data,);
                this.result = data;
                observer.next(data);
            }

            let formatError = (data: TransformResponse) :void=> {
                console.log('FORMAT ERROR ', data);
                observer.error(data)
            }

            formatError.bind(this)
            formatDetected.bind(this)


            let checkProgress = (data: TransformResponse) : void => {

                if (data.status == "PENDING") {
                    setTimeout(() => fetchProgress(data.table), statusCheckTime)
                }
                else if (data.status == "SUCCESS") {
                    formatDetected(data);
                }
                else if (data.status == "ERROR") {
                    formatError(data);
                }
            }

            let fetchProgress = (id: string) => {
                console.log('PROGRESS FOR ', id)
                this.http.get("/proxy/v1/spark/shell/transform/" + id).subscribe((data: any) => {
                    checkProgress(data);
                })
            }


            this.http.post("/proxy/v1/spark/shell/transform", request)
                .subscribe((data: any) => {
                    console.log('DATA', data)
                    checkProgress(data);
                });




        })
        return observable;

    }


}
import {PreviewDataSetRequest} from "../model/preview-data-set-request";
import {Observable} from "rxjs/Observable";
import {TransformRequest} from "../../../../visual-query/wrangler/model/transform-request";
import {TransformResponse} from "../../../../visual-query/wrangler/model/transform-response";
import {Subject} from "rxjs/Subject";
import {PreviewDataSet} from "../model/preview-data-set";
import {TransformResponseTableBuilder} from "./transform-response-table-builder";
import {HttpClient} from "@angular/common/http";


export abstract class AbstractSchemaTransformService {



    constructor(protected http: HttpClient, protected transformResponeTableBuilder:TransformResponseTableBuilder) {

    }


    protected _transform(request:TransformRequest | PreviewDataSetRequest, url:string) :Observable<TransformResponse>{

        let observable = new Observable<TransformResponse>((observer) => {

            let statusCheckTime = 300

            let formatDetected = (data: TransformResponse):void => {

               // this.result = data;
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

                this.http.get("/proxy/v1/spark/shell/transform/" + id).subscribe((data: any) => {
                    checkProgress(data);
                })
            }


            this.http.post(url, request)
                .subscribe((data: any) => {

                    checkProgress(data);
                }, error1 => {
                    console.log("Preview Error",error1);
                    formatError(error1)
                });




        })
        return observable;

    }
}
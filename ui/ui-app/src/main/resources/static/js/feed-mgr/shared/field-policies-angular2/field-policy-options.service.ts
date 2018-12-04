import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import 'rxjs/add/observable/forkJoin';
import {EmptyObservable} from 'rxjs/observable/EmptyObservable';
import {SchemaParser} from "../../model/field-policy";

export class OptionMetadata{
    name:string;
    url:string;
    title:string;
}

@Injectable()
export class FieldPolicyOptionsService {


    private sparkParsers : SchemaParser[];
    private sparkParserObservable:Observable<SchemaParser[]>;

    private metadata : Map<string,OptionMetadata> = new Map<string, OptionMetadata>();

    private cache : Map<string,any> = new Map<string, any>();

    private observables : Map<string,Observable<any>> = new Map<string,Observable<any>>();


    private static STANDARDIZTION_VALIDATION:string = 'standardization-validation';
    private static SPARK_PARSERS:string ='sparkSchemaParser';
    public static VALIDATION:string ='validation';
    public static STANDARDIZATION:string ='standardization';
    private static SCHEMA_PARSER:string = 'schemaParser';




    constructor(private http: HttpClient) {

        this.metadata.set(FieldPolicyOptionsService.SPARK_PARSERS,{name:FieldPolicyOptionsService.SPARK_PARSERS,url:"/proxy/v1/schema-discovery/spark-file-parsers",title:'Supported Parsers'})
        this.metadata.set(FieldPolicyOptionsService.SCHEMA_PARSER,{name:FieldPolicyOptionsService.SPARK_PARSERS,url:"/proxy/v1/schema-discovery/file-parsers",title:'Supported Parsers'})
        this.metadata.set(FieldPolicyOptionsService.VALIDATION,{name:FieldPolicyOptionsService.SPARK_PARSERS,url:"/proxy/v1/field-policies/validation",title:'Validation Policies'})
        this.metadata.set(FieldPolicyOptionsService.STANDARDIZATION,{name:FieldPolicyOptionsService.SPARK_PARSERS,url:"/proxy/v1/field-policies/standardization",title:'Standardization Policies'})


    }


     getStandardizationOptions(forceRefresh ?:boolean): Observable<any> {
        return this._getOptionsForType(FieldPolicyOptionsService.STANDARDIZATION,forceRefresh);
    }
     getValidationOptions(forceRefresh ?:boolean): Observable<any> {
        return this._getOptionsForType(FieldPolicyOptionsService.VALIDATION,forceRefresh);
    }

     getParserOptions(forceRefresh ?:boolean): Observable<SchemaParser[]> {
        return this._getOptionsForType(FieldPolicyOptionsService.SCHEMA_PARSER,forceRefresh);
    }

    /**
     * Gets the list of available spark parsers.  Look in the cache if its already called
     */
    getSparkSchemaParsers(forceRefresh ?:boolean): Observable<SchemaParser[]> {
        return this._getOptionsForType(FieldPolicyOptionsService.SPARK_PARSERS,forceRefresh);
    }



    getTitleForType(type:string) {
        let data = this.metadata.get(type)
        if(data){
            return data.title;
        }
        else {
            return "N/A"
        }

    }

    getStandardizersAndValidators() : Observable<any[]>{
        return this.getOptionsForType(FieldPolicyOptionsService.STANDARDIZTION_VALIDATION);
    }

    getOptionsForType(type:string) : Observable<any[]>{

        if (type == FieldPolicyOptionsService.STANDARDIZTION_VALIDATION) {
           return Observable.forkJoin(this.getStandardizationOptions(),this.getValidationOptions())
        }
        if (type == FieldPolicyOptionsService.STANDARDIZATION) {
            return this.getStandardizationOptions();
        }
        else if (type == FieldPolicyOptionsService.VALIDATION) {
            return this.getValidationOptions();
        }
        else if (type == FieldPolicyOptionsService.SCHEMA_PARSER) {
            return this.getParserOptions();
        }
        else if (type == FieldPolicyOptionsService.SPARK_PARSERS) {
            return this.getSparkSchemaParsers();
        }
        else {
            return new EmptyObservable();
        }
    }


    private _getOptionsForType( type:string,forceRefresh ?:boolean,): Observable<any[]> {
        let data = this.metadata.get(type)
        if(data) {

            if (this.cache.has(type) && !forceRefresh) {
                return Observable.of(this.cache.get(type));
            }
            else if (this.observables.has(type)) {
                return this.observables.get(type);
            } else {
                //fetch it
               let o :Observable<any> = this.http.get(data.url).map((response: any) => {
                    //remove observable ref
                    this.observables.delete(type)
                    this.cache.set(type, response);
                    return response;
                }).share();
                this.observables.set(type, o);
                return o;
            }
        }
        else {
            return new EmptyObservable();
        }
    }
}

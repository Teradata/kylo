import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {TemplateMetadata} from "../api/model/model";
import {catchError} from "rxjs/operators";
import {Import} from "../../../feed-mgr/services/ImportComponentOptionTypes";

const httpOptions = {
    headers: new HttpHeaders({
        'Content-Type': 'application/json'
    })
};

@Injectable()
export class TemplateService {

    constructor(private http: HttpClient) {}

    getTemplates(): Observable<TemplateMetadata[]> {
        return this.http.get<TemplateMetadata[]>("/proxy/v1/marketplace/templates");
    }

    importTemplate(fileName: string, params?: any): Observable<Object> {
        let fd: FormData = new FormData();
        fd.append('fileName', fileName);
        if(params){
            //add params to form data
            Object.keys(params).map((key) => { fd.append(key, params[key])});
        }
        return this.http.post("/proxy/v1/marketplace/templates/import", fd,httpOptions)
            .pipe(
                catchError(this.handleError)
            )

    }

    private handleError(error: HttpErrorResponse) {
        if (error.error instanceof ErrorEvent) {
            // A client-side or network error occurred. Handle it accordingly.
            console.error('Marketplace import error:', error.error.message);
            return Observable.throw('Marketplace import error:' +error.error.message);
        } else {
            // The backend returned an unsuccessful response code.
            // The response body may contain clues as to what went wrong,
            console.error('Marketplace import error:', error.error);
            return Observable.throw('Marketplace import error:' +error.error.message);
        }
    };
}
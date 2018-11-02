import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {TemplateMetadata, TemplateRepository} from "./model";
import {catchError, filter, mergeMap, take} from "rxjs/operators";
import 'rxjs/add/observable/throw';
import {from} from "rxjs/observable/from";

const httpOptions = {
    headers: new HttpHeaders({
        'Content-Type': 'application/json'
    })
};

@Injectable()
export class TemplateService {

    constructor(private http: HttpClient) {
    }

    getTemplates(): Observable<TemplateMetadata> {

        return this.getRepositories().pipe(
            mergeMap(repos => from(repos)),
            mergeMap(r => this.getTemplatesInRepository(r)),
            mergeMap(templates => from(templates)),
            filter(template => (template as any).updateAvailable),
            take(1))
            .catch(err => Observable.throw(err));
    }

    getTemplatesInRepository(repository: TemplateRepository): Observable<any> {
        return this.http.get("/proxy/v1/repository/"+repository.type+"/"+repository.name+"/templates")
            .map((response) => {
                return response;
            }).pipe(catchError((error) => Observable.throw(error.error)));
    }

    getTemplatePage(start: any,limit: any, sort: any): Observable<any> {
        var params = {start: start, limit: limit, sort: sort};
        return this.http.get("/proxy/v1/repository/template-page")
            .map((response) => {
                return response;
            }).pipe(catchError((error) => Observable.throw(error.error)));
    }

    getRepositories(): Observable<TemplateRepository[]> {
        return this.http.get("/proxy/v1/repository")
            .map((response) => {
                return response;
            }).pipe(catchError((error) => Observable.throw(error.error)));
    }

    downloadTemplate(template: TemplateMetadata): Observable<Object> {
        return this.http
            .get("/proxy/v1/repository/"+ template.repository.type+ "/" +template.repository.name+
                "/"+ template.fileName +"/templates/download/"
                , {responseType: "blob"});
    }

    private handleError(error: HttpErrorResponse) {
        if (error.error instanceof ErrorEvent) {
            // A client-side or network error occurred. Handle it accordingly.
            console.error('Repository import error:', error.error.message);
            return Observable.throw('Repository import error:' + error.error.message);
        } else {
            // The backend returned an unsuccessful response code.
            // The response body may contain clues as to what went wrong,
            console.error('Repository import error:', error.error);
            return Observable.throw('Repository import error:' + error.error.message);
        }
    };

    publishTemplate(request: any) {
        return this.http.post("/proxy/v1/repository/templates/publish/",
            JSON.stringify(request),
            {headers: {'Content-Type': 'application/json'}});
    }
}
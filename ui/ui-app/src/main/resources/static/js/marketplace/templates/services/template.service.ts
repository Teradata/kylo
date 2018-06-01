import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import { TemplateMetadata } from "../api/model/model";
import {catchError} from "rxjs/operators";
import {ImportStatus} from "../api/model/model";

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

    importTemplate(fileNames: string[]): Observable<ImportStatus> {
        return this.http.post("/proxy/v1/marketplace/templates/import", fileNames, httpOptions)
            .pipe(
                catchError(this.handleError)
            )

    }

    private handleError(error: HttpErrorResponse) {
        if (error.error instanceof ErrorEvent) {
            // A client-side or network error occurred. Handle it accordingly.
            console.error('An error occurred:', error.error.message);
        } else {
            // The backend returned an unsuccessful response code.
            // The response body may contain clues as to what went wrong,
            console.error(
                'Backend returned code ${error.status}, ' +
                'body was: ${error.error}');
        }
        // return an observable with a user-facing error message
        return Observable.throw(
            'Something bad happened; please try again later.');
    };
}
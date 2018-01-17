import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { SERVER_API_URL } from '../../app.constants';

@Injectable()
export class ConfigService  {
    constructor(private http: Http) { }

    setPath(path: String): Observable<any> {
        console.log('set path');
        return this.http.post(SERVER_API_URL + 'api/configuration', {uri: path}).map((res: Response) => res.json());
    }

    loadChecks(): Observable<any> {
        return this.http.get(SERVER_API_URL + 'api/inspection').map((res: Response) => res.json());
    }

    executeCheck(configurationId: String, inspectionId: String) {
        console.log('executeCheck conf.id=' + configurationId + ', check.id=' + inspectionId);
        return this.http.get(SERVER_API_URL + 'api/configuration/' + configurationId + '/' + inspectionId).map((res: Response) => res.json());
    }

}

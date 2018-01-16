import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { SERVER_API_URL } from '../../app.constants';

@Injectable()
export class ConfigService  {
    constructor(private http: Http) { }

    setPath(path: String): Observable<any> {
        console.log('set path');
        return this.http.post(SERVER_API_URL + 'api/config', {uri: path}).map((res: Response) => res.json());
    }

    loadChecks(): Observable<any> {
        return this.http.get(SERVER_API_URL + 'api/check').map((res: Response) => res.json());
    }

    executeCheck(configurationId: String, checkId: String) {
        console.log('executeCheck conf.id=' + configurationId + ', check.id=' + checkId);
        return this.http.get(SERVER_API_URL + 'api/config/' + configurationId + '/' + checkId).map((res: Response) => res.json());
    }

}

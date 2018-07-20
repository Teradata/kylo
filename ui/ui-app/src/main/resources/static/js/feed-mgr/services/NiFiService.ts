import {Injectable} from "@angular/core";
import {RestUrlConstants} from "./RestUrlConstants";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {NiFiClusterStatus} from "../model/nifi-cluster-status";
import "rxjs/add/observable/of";

@Injectable()
export class NiFiService {

    private nifiClusterStatus: NiFiClusterStatus;
    private observable: Observable<any>;

    constructor(private http: HttpClient) {
    }

    /**
     * Get NiFi cluster status. Uses cache.
     * @returns {Observable<NiFiClusterStatus>}
     */
    getNiFiClusterStatus(): Observable<NiFiClusterStatus> {
        if (this.nifiClusterStatus) {
            //console.log("returned cached value");
            return Observable.of(this.nifiClusterStatus);
        } else if (this.observable) {
            //console.log("returning request in progress observable");
            return this.observable;
        } else {
            //console.log("sending new request");
            this.observable = this.http.get<NiFiClusterStatus>(RestUrlConstants.NIFI_STATUS).map(response => {
                this.observable = null;
                this.nifiClusterStatus = new NiFiClusterStatus(response.version, response.clustered);
                return this.nifiClusterStatus;
            }).share();
            return this.observable;
        }
    }
}
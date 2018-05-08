import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {ArrayObservable} from "rxjs/observable/ArrayObservable";
import {ErrorObservable} from "rxjs/observable/ErrorObservable";

import {Connector} from "../models/connector";
import {DataSet} from "../models/dataset";
import {connectors} from "./data";

// TODO testing only
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

@Injectable()
export class CatalogService {

    private dataSets: {[k: string]: DataSet} = {};

    constructor(private http: HttpClient) {
    }

    createDataSet(connector: Connector): Observable<DataSet> {
        const dataSet: DataSet = {
            id: uuidv4(),
            connector: connector,
            connectorId: connector.id ? connector.id : connector.title
        };
        this.dataSets[dataSet.id] = dataSet;
        return ArrayObservable.of(dataSet);
    }

    /**
     * Gets the list of connectors.
     */
    getConnectors(): Observable<Connector[]> {
        return ArrayObservable.of(connectors);
    }

    getDataSet(dataSetId: string): Observable<DataSet> {
        if (this.dataSets[dataSetId]) {
            return ArrayObservable.of(this.dataSets[dataSetId]);
        } else {
            return ErrorObservable.create("Not found");
        }
    }
}

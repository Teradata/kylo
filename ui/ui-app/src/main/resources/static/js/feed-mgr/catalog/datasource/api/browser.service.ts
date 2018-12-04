import {PartialObserver} from "rxjs/Observer";
import {BrowserObject} from "../../api/models/browser-object";
import {Subject} from "rxjs/Subject";
import {ISubscription} from "rxjs/Subscription";
import {Injectable} from "@angular/core";

@Injectable()
export class BrowserService {


    private dataFiltered$ = new Subject<BrowserObject[]>();

    constructor(){

    }

    subscribeToDataFiltered(o:PartialObserver<BrowserObject[]>) :ISubscription{
        return this.dataFiltered$.subscribe(o);
    }

    onBrowserDataFiltered(data:BrowserObject[]){
        this.dataFiltered$.next(data);
    }

}
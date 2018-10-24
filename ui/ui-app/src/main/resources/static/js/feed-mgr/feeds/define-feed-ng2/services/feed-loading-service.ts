import {Injectable, Injector} from "@angular/core";
import {DefineFeedService} from "./define-feed.service";
import {TdLoadingService} from "@covalent/core/loading";
import {Feed, LoadMode} from "../../../model/feed/feed.model"
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import {StateService} from "@uirouter/angular";



@Injectable()
export class FeedLoadingService {

    public loadingFeed:boolean;

    constructor(private defineFeedService :DefineFeedService, private stateService:StateService,private loadingService: TdLoadingService) {

    }

    public loadFeed(feedId:string, loadMode:LoadMode = LoadMode.LATEST, force?:boolean) :Observable<Feed> {
        //load it
        this.registerLoading();
        this.loadingFeed = true;
      let observable: Observable<Feed> = undefined;
      observable =  this.defineFeedService.loadFeed(feedId, loadMode,force);
      observable.subscribe((feed: Feed) => {
            this.loadingFeed = false;
            this.resolveLoading();
        }, (error1: any) => {
            this.loadingFeed = false;
            this.resolveLoading();
            //ERROR
        });
      return observable
    }

    public registerLoading(): void {
        this.loadingFeed = true;
        this.loadingService.register('processingFeed');
    }

   public  resolveLoading(): void {
        this.loadingFeed = false;
        this.loadingService.resolve('processingFeed');
    }

}
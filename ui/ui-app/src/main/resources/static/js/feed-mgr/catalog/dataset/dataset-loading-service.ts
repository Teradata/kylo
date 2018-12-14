import {Injectable} from "@angular/core";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import {StateService} from "@uirouter/angular";
import {TdLoadingService} from '@covalent/core/loading';


@Injectable()
export class DatasetLoadingService {

    public loading: boolean;

    constructor(private stateService:StateService,private loadingService: TdLoadingService) {

    }

    // public loadDataset(feedId:string, loadMode:LoadMode = LoadMode.LATEST, force?:boolean) :Observable<Feed> {
    //     //load it
    //     this.registerLoading();
    //     this.loading = true;
    //     let observable: Observable<Feed> = undefined;
    //     observable =  this.defineFeedService.loadFeed(feedId, loadMode,force);
    //     observable.subscribe((feed: Feed) => {
    //         this.loading = false;
    //         this.resolveLoading();
    //     }, (error1: any) => {
    //         this.loading = false;
    //         this.resolveLoading();
    //         //ERROR
    //     });
    //     return observable
    // }

    public registerLoading(): void {
        this.loading = true;
        this.loadingService.register('processingDataset');
    }

    public  resolveLoading(): void {
        this.loading = false;
        this.loadingService.resolve('processingDataset');
    }

}
import {Injectable} from "@angular/core";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import {TdLoadingService} from '@covalent/core/loading';

@Injectable()
export class DatasetLoadingService {

    constructor(private loadingService: TdLoadingService) {
    }

    public registerLoading(): void {
        this.loadingService.register('processingDataset');
    }

    public  resolveLoading(): void {
        this.loadingService.resolve('processingDataset');
    }

}
import {Dataset} from '../api/models/dataset';
import {DatasetSaveResponse} from './dataset-info/dataset-save-response';
import {CatalogService} from '../api/services/catalog.service';
import {Subject} from 'rxjs/Subject';
import {Injectable} from '@angular/core';

@Injectable()
export class DatasetService {

    constructor(private catalogService: CatalogService) {
    }

    saveDataset(dataset: Dataset) {
        console.log("saveDataset, dataset", dataset);
        let subject = new Subject<DatasetSaveResponse>();
        let observableSubject = subject.asObservable();

        this.catalogService.createDataset(dataset).subscribe(
            source => {
                subject.next(new DatasetSaveResponse(source, true, "Dataset saved successfully"))
            },
            err => {
                console.error(err);
                subject.next(new DatasetSaveResponse(dataset, false, "Could not save dataset"))
            }
        );
        return observableSubject;
    }
}
import {DatasetSaveResponse} from './dataset-info/dataset-save-response';
import {CatalogService} from '../../../api/services/catalog.service';
import {Subject} from 'rxjs/Subject';
import {Injectable} from '@angular/core';
import {SparkDataSet} from '../../../../model/spark-data-set.model';

@Injectable()
export class DatasetService {

    constructor(private catalogService: CatalogService) {
    }

    saveDataset(dataset: SparkDataSet) {
        let subject = new Subject<DatasetSaveResponse>();
        let observableSubject = subject.asObservable();

        this.catalogService.createDataset(dataset).subscribe(
            source => {
                dataset.id = source.id;
                subject.next(new DatasetSaveResponse(dataset, true, "Dataset saved successfully"))
            },
            err => {
                console.error(err);
                subject.next(new DatasetSaveResponse(dataset, false, "Could not save dataset"))
            }
        );
        return observableSubject;
    }
}
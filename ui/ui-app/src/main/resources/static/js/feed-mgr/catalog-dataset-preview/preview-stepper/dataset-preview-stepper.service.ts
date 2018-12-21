import {Subject} from "rxjs/Subject";
import {DataSource} from "../../catalog/api/models/datasource";
import {PartialObserver} from "rxjs/Observer";
import {Injectable} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {DatasetPreviewService, DataSourceChangedEvent, PreviewDataSetResultEvent} from "../../catalog/datasource/preview-schema/service/dataset-preview.service";
import {Observable} from "rxjs/Observable";
import {Node} from "../../catalog/api/models/node";
import {PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";


@Injectable()
export class DatasetPreviewStepperService {
    public stepIndex: number;

    public stepChanged$ = new Subject<number>();

    public previews: PreviewDataSet[] = [];




    constructor(private _datasetPreviewService: DatasetPreviewService) {

    }


    public setDataSource(dataSource: DataSource, params?: any) {
      this._datasetPreviewService.setDataSource(dataSource, params)
    }

    public subscribeToDataSourceChanges(o: PartialObserver<DataSourceChangedEvent>) {
        return this._datasetPreviewService.subscribeToDataSourceChanges(o);
    }

    public subscribeToStepChanges(o: PartialObserver<number>) {
        return this.stepChanged$.subscribe(o);
    }

    public subscribeToUpdateView(o: PartialObserver<any>) {
        return this._datasetPreviewService.subscribeToUpdateView(o);
    }

    public setStepIndex(index: number) {
        if (this.stepIndex == undefined || this.stepIndex != index) {
            this.stepIndex = index;
            this.stepChanged$.next(index);
        }
    }

    public savePreviews(previews:PreviewDataSet[]){
        this.previews = previews;
    }


    markFormAsInvalid(formGroup: FormGroup) {
        if (formGroup.contains("hiddenValidFormCheck")) {
            formGroup.get("hiddenValidFormCheck").setValue("")
            this._datasetPreviewService.notifyToUpdateView();
        }
    }

    markFormAsValid(formGroup: FormGroup) {
        if (formGroup.contains("hiddenValidFormCheck")) {
            formGroup.get("hiddenValidFormCheck").setValue("true")
            this._datasetPreviewService.notifyToUpdateView();
        }
    }


    public prepareAndPopulatePreview(node: Node, datasource: DataSource): Observable<PreviewDataSetResultEvent> {
        return this._datasetPreviewService.prepareAndPopulatePreview(node,datasource);
    }

}
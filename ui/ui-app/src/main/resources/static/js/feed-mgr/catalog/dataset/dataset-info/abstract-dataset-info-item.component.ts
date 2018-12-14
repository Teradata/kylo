import {Input} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {Observable} from "rxjs/Observable";
import {Dataset} from '../../api/models/dataset';
import {InfoItemService} from '../../../shared/info-item/item-info.service';
import {ItemSaveResponse} from '../../../shared/info-item/item-save-response';
import {DatasetService} from './../dataset-service';
import {DatasetLoadingService} from './../dataset-loading-service';
import {DatasetSaveResponse} from './dataset-save-response';


export abstract class AbstractDatasetInfoItemComponent {

    @Input()
    public editing: boolean;

    @Input()
    public dataset: Dataset;

    valid:boolean = true;

    formGroup: FormGroup;

    protected constructor(protected itemInfoService: InfoItemService, private datasetService: DatasetService, private datasetLoadingService: DatasetLoadingService) {
        this.initForm();
    }

    initForm() {
        this.formGroup = new FormGroup({})
    }

    /**
     * Called when save is successful
     * override and handle the callback
     * @param {SaveFeedResponse} response
     */
    onSaveSuccess(response: ItemSaveResponse) {
        this.hideProgress();
    }

    /**
     * called when failed to save
     * Override and handle callback
     * @param {SaveFeedResponse} response
     */
    onSaveFail(response: ItemSaveResponse) {
        this.hideProgress();
    }

    showProgress() {
        this.datasetLoadingService.registerLoading()
    }

    hideProgress() {
        this.datasetLoadingService.resolveLoading()
    }

    saveDataset(dataset: Dataset): Observable<ItemSaveResponse> {
        let observable = this.datasetService.saveDataset(dataset);
        observable.subscribe(
            (response: DatasetSaveResponse) => {
                if (response.success) {
                    this.dataset = response.dataset;
                    this.onSaveSuccess(response);
                    this.editing = false;
                    this.itemInfoService.onSaved(response);
                } else {
                    this.onSaveFail(response);
                }
            },
            error => this.onSaveFail(error)
        );
        return observable;
    }
}

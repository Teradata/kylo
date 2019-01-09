import {Input, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {Observable} from "rxjs/Observable";
import {InfoItemService} from '../../../../../shared/info-item/item-info.service';
import {ItemSaveResponse} from '../../../../../shared/info-item/item-save-response';
import {DatasetService} from '../dataset-service';
import {DatasetLoadingService} from '../dataset-loading-service';
import {DatasetSaveResponse} from './dataset-save-response';
import {StateService} from "@uirouter/angular";
import {SparkDataSet} from '../../../../../model/spark-data-set.model';


export abstract class AbstractDatasetInfoItemComponent implements OnInit {

    @Input()
    public editing: boolean;

    @Input()
    public dataset: SparkDataSet;

    valid:boolean = true;

    formGroup: FormGroup;

    protected constructor(protected itemInfoService: InfoItemService,
                          private datasetService: DatasetService,
                          private datasetLoadingService: DatasetLoadingService,
                          private stateService: StateService) {
        this.initForm();
    }

    ngOnInit(): void {
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

    saveDataset(dataset: SparkDataSet): Observable<ItemSaveResponse> {
        const isExistingDataset = dataset.id !== undefined;
        let observable = this.datasetService.saveDataset(dataset);
        observable.subscribe(
            (response: DatasetSaveResponse) => {
                if (response.success) {
                    this.dataset = response.dataset;
                    this.onSaveSuccess(response);
                    this.editing = false;
                    this.itemInfoService.onSaved(response);
                    if (!isExistingDataset) {
                        this.stateService.go("catalog.datasource.preview",
                            {
                                datasetId: this.dataset.id,
                                datasource: this.dataset.dataSource,
                                displayInCard:true,
                                location: "replace"});
                    }
                } else {
                    this.onSaveFail(response);
                }
            },
            error => this.onSaveFail(error)
        );
        return observable;
    }

}

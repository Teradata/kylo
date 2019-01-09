import {FormControl} from "@angular/forms";
import {Component, OnInit} from "@angular/core";
import {AbstractDatasetInfoItemComponent} from '../abstract-dataset-info-item.component';
import {DatasetLoadingService} from '../../dataset-loading-service';
import {InfoItemService} from '../../../../../../shared/info-item/item-info.service';
import {DatasetService} from '../../dataset-service';
import {StateService} from '@uirouter/core';


@Component({
    selector: "dataset-info-title",
    templateUrl: "./dataset-info-title.component.html"
})
export class DatasetInfoTitleComponent extends AbstractDatasetInfoItemComponent implements OnInit {

    constructor(itemInfoService: InfoItemService,
                datasetService: DatasetService,
                datasetLoadingService: DatasetLoadingService,
                stateService: StateService) {
        super(itemInfoService, datasetService, datasetLoadingService, stateService);
    }

    ngOnInit() {
        this.formGroup.addControl("title", new FormControl(this.dataset.title));
    }

    cancel() {
        this.formGroup.reset({"title": this.dataset.title});
    }

}
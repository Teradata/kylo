import {FormControl} from "@angular/forms";
import {Component, OnInit} from "@angular/core";
import {AbstractDatasetInfoItemComponent} from '../abstract-dataset-info-item.component';
import {DatasetLoadingService} from '../../dataset-loading-service';
import {InfoItemService} from '../../../../shared/info-item/item-info.service';
import {DatasetService} from '../../dataset-service';


@Component({
    selector: "dataset-info-description",
    templateUrl: "./dataset-info-description.component.html"
})
export class DatasetInfoDescriptionComponent extends AbstractDatasetInfoItemComponent implements OnInit {

    constructor(itemInfoService: InfoItemService, datasetService: DatasetService, datasetLoadingService: DatasetLoadingService) {
        super(itemInfoService, datasetService, datasetLoadingService);
    }

    ngOnInit() {
        this.formGroup.addControl("description", new FormControl(this.dataset.description));
    }

    save() {
        this.showProgress();
        let values = this.formGroup.value;
        this.dataset.description = values.description;
        this.saveDataset(this.dataset);
    }

    cancel() {
        this.formGroup.reset({"description": this.dataset.description});
    }

}
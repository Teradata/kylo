import {Component, Input, OnInit} from "@angular/core";
import {SparkDataSet} from '../../../../../model/spark-data-set.model';

/**
 * Displays dataset information card
 */
@Component({
    selector: "dataset-info",
    styleUrls: ["../dataset.component.scss"],
    templateUrl: "./dataset-info.component.html"
})
export class DatasetInfoComponent implements OnInit {

    @Input()
    public dataset: SparkDataSet;

    constructor() {
    }

    ngOnInit(): void {
    }
}

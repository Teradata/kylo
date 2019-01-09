import {Component, Input} from "@angular/core";
import {SparkDataSet} from '../../../../../model/spark-data-set.model';

/**
 * Displays dataset information card
 */
@Component({
    selector: "dataset-overview",
    styleUrls: ["../dataset.component.scss"],
    templateUrl: "./dataset-overview.component.html"
})
export class DatasetOverviewComponent {

    @Input()
    public dataset: SparkDataSet;
}

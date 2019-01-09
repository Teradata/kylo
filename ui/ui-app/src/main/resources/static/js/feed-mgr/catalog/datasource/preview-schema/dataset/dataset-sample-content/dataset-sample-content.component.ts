import {Component, Input} from "@angular/core";
import {SparkDataSet} from '../../../../../model/spark-data-set.model';

/**
 * Displays sample data content of the dataset
 */
@Component({
    selector: "dataset-sample-content",
    styleUrls: ["../dataset.component.scss"],
    templateUrl: "./dataset-sample-content.component.html"
})
export class DatasetSampleContentComponent {

    @Input()
    public dataset: SparkDataSet;
}

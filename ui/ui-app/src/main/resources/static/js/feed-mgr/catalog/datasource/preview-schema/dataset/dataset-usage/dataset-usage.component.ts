import {Component, Input} from "@angular/core";
import {SparkDataSet} from '../../../../../model/spark-data-set.model';

/**
 * Displays usage across Kylo, e.g. used in Projects, used in Wrangler
 */
@Component({
    selector: "dataset-usage",
    styleUrls: ["../dataset.component.scss"],
    templateUrl: "./dataset-usage.component.html"
})
export class DatasetUsageComponent {

    @Input()
    public dataset: SparkDataSet;
}

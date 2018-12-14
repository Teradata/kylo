import {Component, Input} from "@angular/core";
import {Dataset} from '../../api/models/dataset';

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
    public dataset: Dataset;
}

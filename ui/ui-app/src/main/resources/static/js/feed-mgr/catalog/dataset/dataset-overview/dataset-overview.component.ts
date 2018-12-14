import {Component, Input} from "@angular/core";
import {Dataset} from '../../api/models/dataset';

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
    public dataset: Dataset;
}

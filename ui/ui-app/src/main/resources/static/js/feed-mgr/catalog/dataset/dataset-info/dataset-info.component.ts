import {Component, Input} from "@angular/core";
import {Dataset} from '../../api/models/dataset';

/**
 * Displays dataset information card
 */
@Component({
    selector: "dataset-info",
    styleUrls: ["../dataset.component.scss"],
    templateUrl: "./dataset-info.component.html"
})
export class DatasetInfoComponent {

    @Input()
    public dataset: Dataset;
}

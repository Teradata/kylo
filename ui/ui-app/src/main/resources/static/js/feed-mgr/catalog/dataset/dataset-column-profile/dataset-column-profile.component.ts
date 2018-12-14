import {Component, Input} from "@angular/core";
import {Dataset} from '../../api/models/dataset';

/**
 * Displays profile information for dataset columns
 */
@Component({
    selector: "dataset-column-profile",
    styleUrls: ["../dataset.component.scss"],
    templateUrl: "./dataset-column-profile.component.html"
})
export class DatasetColumnProfileComponent {

    @Input()
    public dataset: Dataset;
}

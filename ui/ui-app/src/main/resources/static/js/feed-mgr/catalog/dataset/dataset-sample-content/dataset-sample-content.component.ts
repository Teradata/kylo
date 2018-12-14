import {Component, Input} from "@angular/core";
import {Dataset} from '../../api/models/dataset';

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
    public dataset: Dataset;
}

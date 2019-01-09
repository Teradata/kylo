import {Component, Input} from "@angular/core";
import {PreviewDataSet} from '../../model/preview-data-set';

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
    public dataset: PreviewDataSet;
}

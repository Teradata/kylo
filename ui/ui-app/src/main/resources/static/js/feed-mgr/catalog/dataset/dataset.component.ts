import {Component, Input, OnInit} from "@angular/core";
import {Dataset} from '../api/models/dataset';


/**
 * Displays available dataset
 */
@Component({
    selector: "catalog-dataset",
    styleUrls: ["./dataset.component.scss"],
    templateUrl: "./dataset.component.html"
})
export class DatasetComponent implements OnInit {

    public static LOADER = "DatasetComponent.LOADER";

    @Input("dataset")
    public dataset: Dataset;

    constructor() {
    }

    public ngOnInit() {
    }
}

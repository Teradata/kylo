import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
    selector: "metadata-indexing",
    styleUrls: ["./metadata-indexing.component.scss"],
    templateUrl: "./metadata-indexing.component.html"
})

export class MetadataIndexingComponent implements OnInit {
    @Input()
    title?:string;

    @Input()
    editable: boolean;

    @Input()
    allowIndexing: boolean = true;

    @Output()
    allowIndexingChange = new EventEmitter<boolean>();

    @Input()
    parentFormGroup: FormGroup;

    metadataIndexingForm: FormGroup;

    component: MetadataIndexingComponent = this;

    constructor() {
        this.metadataIndexingForm = new FormGroup({});
    }

    ngOnInit() {
        if (this.parentFormGroup) {
            this.parentFormGroup.addControl("metadataIndexingForm", this.metadataIndexingForm);
        }

        if (this.editable) {
            let metadataIndexingValidators = [];
            let metadataIndexingControl = new FormControl(this.allowIndexing, metadataIndexingValidators);
            this.metadataIndexingForm.addControl("metadataIndexingOption", metadataIndexingControl);
        }
    }

    allowIndexingChanged($event: any) {
        this.allowIndexingChange.emit($event.checked);
    }
}
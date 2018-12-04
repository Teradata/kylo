import { Component, Input, Output, EventEmitter } from "@angular/core";

@Component({
    selector: "tba-view-type-selection",
    templateUrl: "./view-type-selection-template.html"
})
export class ViewTypeSelectionComponent {

    @Input()
    viewType: string;

    @Output()
    viewTypeChange: EventEmitter<string> = new EventEmitter<string>();

    viewTypeChanged(viewType: any) {
        this.viewType = viewType;
        this.viewTypeChange.emit(this.viewType);
    }

}

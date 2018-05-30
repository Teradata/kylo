import {Component, ElementRef} from "@angular/core";
import {TransitionService} from "@uirouter/core";
import * as $ from "jquery";

import AddButtonService from "../../services/AddButtonService";
import BroadcastService from "../../services/broadcast-service";

@Component({
    selector: "add-button",
    template: `
      <button mat-button
              class="md-fab md-fab-bottom-right kylo-add-button"
              aria-label="Add" (click)="onClickAddButton($event)">
        <mat-icon>add</mat-icon>
      </button>`
})
export class AddButtonComponent {

    currentState: string = '';

    constructor(private elRef: ElementRef,
                private $transitions: TransitionService,
                private addButtonService: AddButtonService,
                private broadcastService: BroadcastService) {
    }

    ngOnInit() {

        this.broadcastService.subscribe(null, this.addButtonService.NEW_ADD_BUTTON_EVENT, () => this.updateShowState);
        this.broadcastService.subscribe(null, this.addButtonService.HIDE_ADD_BUTTON_EVENT, () => this.hideButton);
        this.broadcastService.subscribe(null, this.addButtonService.SHOW_ADD_BUTTON_EVENT, () => this.showButton);

        this.$transitions.onSuccess({}, (transition: any) => {
            const toState = transition.to();
            if (toState != undefined) {
                let state = toState.name;
                if (toState.name == 'home') {
                    state = 'feeds';
                }
                this.currentState = state;
                this.updateShowState();
            }
        });

    }

    onClickAddButton(event: any) {
        this.addButtonService.onClick(this.currentState);
    }

    isShowAddButton() {
        return this.addButtonService.isShowAddButton(this.currentState);
    }

    hideButton() {
        $(this.elRef.nativeElement).hide();
    }

    showButton() {
        $(this.elRef.nativeElement).show();
    }

    updateShowState() {
        if (this.isShowAddButton()) {
            $(this.elRef.nativeElement).show();
        }
        else {
            $(this.elRef.nativeElement).hide();
        }
    }

}



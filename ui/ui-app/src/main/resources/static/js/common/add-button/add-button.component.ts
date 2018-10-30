import {Component, ElementRef, Inject} from "@angular/core";
import {TransitionService} from "@uirouter/core";
import * as $ from "jquery";

import {AddButtonService} from "../../services/AddButtonService";
import {BroadcastService} from "../../services/broadcast-service";
import {AccessControlService} from "../../services/AccessControlService";
import {StateService} from "../../services/StateService";

@Component({
    selector: "add-button",
    template: `
      <button mat-button
              class="md-button md-fab md-fab-bottom-right kylo-add-button md-kylo-theme"
              aria-label="Add" (click)="onClickAddButton($event)">
        <mat-icon>add</mat-icon>
      </button>`
})
export class AddButtonComponent {

    currentState: string = '';
    private addButtonService: AddButtonService;
    private broadcastService: BroadcastService;
    private accessControlService: AccessControlService;

    constructor(private elRef: ElementRef,
                private $transitions: TransitionService,
                @Inject("$injector") private $injector: any,
                private stateService: StateService) {

        this.addButtonService = $injector.get("AddButtonService");
        this.broadcastService = $injector.get("BroadcastService");
        this.accessControlService = $injector.get("AccessControlService");

    }

    ngOnInit() {

        // Register Add button (categories, feeds, templates) on initial application load
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.CATEGORIES_EDIT, actionSet.actions)) {
                    this.addButtonService.registerAddButton('categories', () => {
                        this.stateService.FeedManager().Category().navigateToCategoryDetails(null);
                    });
                }

                if (this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions)) {
                    this.addButtonService.registerAddButton("feeds", () => {
                        this.stateService.FeedManager().Feed().navigateToDefineFeed();
                    });
                }

                if (this.accessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                    this.addButtonService.registerAddButton("registered-templates", () => {
                        this.stateService.FeedManager().Template().navigateToRegisterNewTemplate();
                    });
                }

                this.subscribeAndUpdateShowState();
            }, (error: any) => {
            this.subscribeAndUpdateShowState();
        });

    }

    subscribeAndUpdateShowState() {
        this.broadcastService.subscribe(null, this.addButtonService.NEW_ADD_BUTTON_EVENT, () => this.updateShowState());
        this.broadcastService.subscribe(null, this.addButtonService.HIDE_ADD_BUTTON_EVENT, () => this.hideButton());
        this.broadcastService.subscribe(null, this.addButtonService.SHOW_ADD_BUTTON_EVENT, () => this.showButton());

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



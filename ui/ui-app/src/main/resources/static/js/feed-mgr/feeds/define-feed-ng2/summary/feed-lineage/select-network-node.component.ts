import {Component, Injector, Input} from "@angular/core";

@Component({
    selector: "select-network-node",
    styleUrls: ["./select-network-node.component.css"],
    templateUrl: "./select-network-node.component.html"
})
export class SelectNetworkNodeComponent {
    @Input()
    selectedNode: any;

    objectKeys = Object.keys;

    panelOpenState = false;

    private kyloStateService:any

    constructor( private $$angularInjector: Injector) {
        this.kyloStateService = $$angularInjector.get("StateService");
    }

    navigateToFeed() {
        if (this.selectedNode.type == 'FEED' && this.selectedNode.content) {
            this.kyloStateService.FeedManager().Feed().navigateToFeedDetails(this.selectedNode.content.id, 2);
        }
    }
}
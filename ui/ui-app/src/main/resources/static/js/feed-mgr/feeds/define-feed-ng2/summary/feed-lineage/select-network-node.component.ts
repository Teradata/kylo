import {Component, Input} from "@angular/core";

@Component({
    selector: "select-network-node",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/feed-lineage/select-network-node.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/feed-lineage/select-network-node.component.html"
})
export class SelectNetworkNodeComponent {
    @Input()
    selectedNode: any;

    objectKeys = Object.keys;
}
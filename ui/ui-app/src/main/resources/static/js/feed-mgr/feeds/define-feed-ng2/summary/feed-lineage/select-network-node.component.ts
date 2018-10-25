import {Component, Input} from "@angular/core";

@Component({
    selector: "select-network-node",
    styleUrls: ["./select-network-node.component.css"],
    templateUrl: "./select-network-node.component.html"
})
export class SelectNetworkNodeComponent {
    @Input()
    selectedNode: any;

    objectKeys = Object.keys;
}
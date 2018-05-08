import {Component, Input} from "@angular/core";

import {Connector} from "./catalog/models/connector";

@Component({
    template: `
      <td-layout>
        <ui-view>
          <explorer-connectors [connectors]="connectors"></explorer-connectors>
        </ui-view>
      </td-layout>`
})
export class ExplorerComponent {

    @Input()
    public connectors: Connector[];
}

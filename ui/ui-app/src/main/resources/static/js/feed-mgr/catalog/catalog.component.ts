import {Component, Input} from "@angular/core";

import {Connector} from "./api/models/connector";

@Component({
    template: `
      <td-layout>
        <ui-view>
          <catalog-connectors [connectors]="connectors"></catalog-connectors>
        </ui-view>
      </td-layout>`
})
export class CatalogComponent {

    @Input()
    public connectors: Connector[];
}

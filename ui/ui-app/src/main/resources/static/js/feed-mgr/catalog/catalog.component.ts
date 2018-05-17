import {Component, Input} from "@angular/core";

import {DataSource} from "./api/models/datasource";

@Component({
    template: `
      <td-layout>
        <ui-view>
          <catalog-datasources [datasources]="datasources"></catalog-datasources>
        </ui-view>
      </td-layout>`
})
export class CatalogComponent {

    @Input()
    public datasources: DataSource[];
}

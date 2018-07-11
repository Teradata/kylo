import {Component, Input} from "@angular/core";

import {TemplateMetadata} from "./services/model";

@Component({
    template: `
      <td-layout>
        <ui-view>
          <list-templates></list-templates>
        </ui-view>
      </td-layout>`
})
export class RepositoryComponent {
}

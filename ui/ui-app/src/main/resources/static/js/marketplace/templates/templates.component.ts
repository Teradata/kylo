import {Component, Input} from "@angular/core";

import {TemplateMetadata} from "./api/model/model";

@Component({
    template: `
      <td-layout>
        <ui-view>
          <list-templates [templates]="templates"></list-templates>
        </ui-view>
      </td-layout>`
})
export class TemplatesComponent {

    @Input()
    public templates: TemplateMetadata[];
}

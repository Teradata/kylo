import {Component, Input} from "@angular/core";

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

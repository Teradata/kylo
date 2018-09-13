import {Component} from "@angular/core";
import {ProcessorRef} from "@kylo/feed";

@Component({
    template: `
      <thinkbig-get-table-data-properties
          [processor]="processor"
          connectionServiceKey="Sqoop Connection Service"
          loadStrategyKey="Source Load Strategy"
          incrementalPropertyKey="Source Check Column Name (INCR)"
          [loadStrategyOptions]="loadStrategyOptions"
          renderLoadStrategyOptions="true"
          useTableNameOnly="true">
      </thinkbig-get-table-data-properties>
    `
})
export class ImportSqoopComponent {

    loadStrategyOptions = [
        {
            name: 'Full Load', type: 'SNAPSHOT', strategy: 'FULL_LOAD', hint: 'Replace entire table'
        },
        {
            name: 'Incremental Date', type: 'DELTA', strategy: 'INCREMENTAL_LASTMODIFIED', hint: 'Incremental load based on a high watermark date', incremental: true, restrictDates: true
        },
        {
            name: 'Incremental Append', type: 'DELTA', strategy: 'INCREMENTAL_APPEND', hint: 'Incremental load based on a high watermark', incremental: true, restrictDates: false
        }];

    constructor(readonly processor: ProcessorRef) {
    }
}

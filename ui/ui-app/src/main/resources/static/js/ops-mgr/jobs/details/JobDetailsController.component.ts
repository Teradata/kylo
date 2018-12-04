import { Component } from "@angular/core";
import { StateService } from "@uirouter/core";

@Component({
  selector: 'job-details-controller',
  templateUrl: './job-details.html'
})
export class JobDetailsController {
    executionId: any;

    ngOnInit() {
      this.executionId = this.stateService.params.executionId;
    }
    constructor(private stateService: StateService){}
}


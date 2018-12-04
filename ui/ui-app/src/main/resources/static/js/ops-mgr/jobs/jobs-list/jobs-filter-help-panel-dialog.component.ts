import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Component, Inject} from "@angular/core";

@Component({
    templateUrl: './jobs-filter-help-panel-dialog.component.html',
    styles: [`
        .filter-help-backdrop {
            background: transparent;
        }
    `]
})
export class JobsFilterHelpPanelDialogComponent {

    filterHelpExamples: any[];
    filterHelpOperators: any[];
    filterHelpFields: any[];

    ngOnInit() {
        this.filterHelpOperators = [];
        this.filterHelpFields = []
        this.filterHelpExamples = [];
        this.filterHelpOperators.push(this.newHelpItem("Equals", "=="));
        this.filterHelpOperators.push(this.newHelpItem("Like condition", "=~"));
        this.filterHelpOperators.push(this.newHelpItem("In Clause", "Comma separated surrounded with quote    ==\"value1,value2\"   "));
        this.filterHelpOperators.push(this.newHelpItem("Greater than, less than", ">,>=,<,<="));
        this.filterHelpOperators.push(this.newHelpItem("Multiple Filters", "Filers separated by a comma    field1==value,field2==value  "));

        this.filterHelpFields.push(this.newHelpItem("Filter on a feed name", "feed"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job name", "job"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job start time", "jobStartTime"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job end time", "jobEndTime"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job id", "executionId"));
        this.filterHelpFields.push(this.newHelpItem("Start time date part filters", "startYear,startMonth,startDay"));
        this.filterHelpFields.push(this.newHelpItem("End time date part filters", "endYear,endMonth,endDay"));

        this.filterHelpExamples.push(this.newHelpItem("Find job names that equal 'my.job1' ", "job==my.job1"));
        this.filterHelpExamples.push(this.newHelpItem("Find job names starting with 'my' ", "job=~my"));
        this.filterHelpExamples.push(this.newHelpItem("Find jobs for 'my.job1' or 'my.job2' ", "job==\"my.job1,my.job2\""));
        this.filterHelpExamples.push(this.newHelpItem("Find 'my.job1' starting in 2017 ", "job==my.job1,startYear==2017"));
        this.filterHelpExamples.push(this.newHelpItem("Find jobs that started on February 1st 2017", "startTime>=2017-02-01,startTime<2017-02-02"));
    }
    constructor(private dialogRef: MatDialogRef<JobsFilterHelpPanelDialogComponent>,
                @Inject(MAT_DIALOG_DATA) private data: any) { }

    newHelpItem(description: string, label: string) {
        return { displayName: label, description: description };
    }

    cancel(){
        this.dialogRef.close();
    }
}
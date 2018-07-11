import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Component, Inject, OnInit, SecurityContext} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {TemplateService} from "../services/template.service";
import {HttpClient} from "@angular/common/http";
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";
import {TemplateRepository} from "../services/model";

@Component({
    selector: 'template-publish-dialog',
    templateUrl: 'js/repository/dialog/template-publish-dialog.html',
})
export class TemplatePublishDialog implements OnInit {

    success: boolean = false;
    error: boolean = false;
    repositories: TemplateRepository[] = [];
    selectedRepository: TemplateRepository;
    errorMsg: string;

    constructor(private dialog: MatDialog,
                private dialogRef: MatDialogRef<TemplatePublishDialog>,
                @Inject(MAT_DIALOG_DATA) private data: any, private templateService: TemplateService,
                private http: HttpClient, private state: StateService) {
    }

    public ngOnInit() {
        this.templateService.getRepositories()
            .subscribe((data: TemplateRepository[]) => {
                this.repositories = data;
            }, (errorRsp: any) => {
                this.error = true;
                this.errorMsg = errorRsp.error.message;
                console.log(errorRsp.error.message);
            });
    }

    close(): void {
        this.dialogRef.close();
    }

    publishTemplate(overwrite: boolean) {
        console.log(this.data.templateId, this.selectedRepository);
        if (this.selectedRepository) {
            this.templateService
                .publishTemplate({ "repositoryName": this.selectedRepository.name,
                    "repositoryType": this.selectedRepository.type,
                    "templateId": this.data.templateId,
                    "overwrite": overwrite})
                .subscribe((response: any) => {
                    this.success = true;
                }, (errorRsp: any) => {
                    console.log("Error publishing template to repository", errorRsp.error.message);
                    this.errorMsg = errorRsp.error.message;
                    this.error = true;
                });
        }
    }

}
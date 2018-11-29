import {Component, OnInit} from "@angular/core";
import {TemplateMetadata, TemplateRepository} from "../services/model";
import {TemplateService} from "../services/template.service";
import {TdDataTableService} from "@covalent/core/data-table";
import {StateService} from "@uirouter/angular";
import {MatDialog} from "@angular/material/dialog";
import {TemplateUpdatesDialog} from "../dialog/template-updates-dialog";
import {IPageChangeEvent} from "@covalent/core";
import {MatTableDataSource} from '@angular/material/table';

/**
 * List templates from repository ready for installation.
 */
@Component({
    selector: "list-templates",
    templateUrl: "./list.component.html"
})
export class ListTemplatesComponent implements OnInit {

    constructor(private templateService: TemplateService,
                private dataTableService: TdDataTableService,
                private state: StateService,
                private dialog: MatDialog) {
    }

    loading:boolean = true;
    selectedTemplate: TemplateMetadata;
    errorMsg: string = "";
    /**
     * List of available templates
     */
    templates: TemplateMetadata[] = [];
    filteredList: TemplateMetadata[] = [];
    repositories: TemplateRepository[] = [];
    selectedRepository: TemplateRepository;

    dataSource = new MatTableDataSource();

    length = undefined;

    public ngOnInit() {
        this.init();
    }

    private init() {
        this.templateService.getRepositories().subscribe(
            (repos: TemplateRepository[]) => {
                this.repositories = repos;
                if(this.repositories.length > 0){
                    this.selectedRepository = this.repositories[0];
                    this.loadTemplates();
                }
            }
        );
    }

    loadTemplates() {

        this.templateService.getTemplatesInRepository(this.selectedRepository).subscribe(
            (data: TemplateMetadata[]) => {
                this.templates = data;
                this.filteredList = data;
                this.search();
                this.sortData(null);
                this.loading = false;
            },
            (error: any) => {
                console.log(error);
                if(error.developerMessage)
                    this.errorMsg += error.developerMessage;


                this.loading = false;
            }
        );
    }

    direction: string = "";
    icon: string = "";
    sortData(event: any) {

        const isAsc = this.direction === 'asc' || this.direction === "";
        if(isAsc){
            this.direction = 'desc';
            this.icon = "keyboard_arrow_up";
        } else{
            this.direction = 'asc';
            this.icon = "keyboard_arrow_down";
        }
        this.templates = this.templates.sort((a, b) => {
            return this.compare(a.templateName, b.templateName, isAsc);
        });
        this.search();

        if(event) {
            event.stopPropagation();
            event.preventDefault();
        }
    }

    private compare(a: number | string, b: number | string, isAsc: boolean) {
        return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
    }

    /*
     * download template from repository.
     */
    downloadTemplate(template: TemplateMetadata) {
        this.templateService.downloadTemplate(template).subscribe(blob => {
            var link: any = document.createElement('a');
            link.href = (window.URL as any).createObjectURL(blob);
            link.download = template.fileName;
            link.click();
        });
    }

    updateTemplate(template: TemplateMetadata) {
        this.importTemplate(template);
    }

    importTemplate(template: TemplateMetadata) {
        this.selectedTemplate = template;
        let param = {"template": template};
        this.state.go("import-template", param);
    }

    pageSize: number = 10;
    currentPage: number = 1;
    fromRow: number = 1;
    filteredTotal = 0;
    searchTerm = '';

    search(): void {
        // this.dataSource.filter = filter.trim().toLowerCase();
        let newData = this.dataTableService.filterData(this.templates, this.searchTerm, true, []);
        this.filteredTotal = newData.length;
        newData = this.dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredList = newData;
    }

    page(pagingEvent: IPageChangeEvent): void {
        this.fromRow = pagingEvent.fromRow;
        this.currentPage = pagingEvent.page;
        this.pageSize = pagingEvent.pageSize;
        this.search();
    }

    viewUpdates(template: TemplateMetadata): void {

        this.dialog.open(TemplateUpdatesDialog, {
            data: {updates: template.updates},
            width: '40%'
        });
    }
}

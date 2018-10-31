import {Component, OnInit, ViewChild} from "@angular/core";
import {TemplateMetadata, TemplateRepository} from "../services/model";
import {TemplateService} from "../services/template.service";
import {TdDataTableService} from "@covalent/core/data-table";
import {StateService} from "@uirouter/angular";
import {MatPaginator, PageEvent} from "@angular/material/paginator";
import {MatSort, Sort} from "@angular/material/sort";
import {MatTableDataSource} from "@angular/material/table";
import {MatDialog} from "@angular/material/dialog";
import {TemplateUpdatesDialog} from "../dialog/template-updates-dialog";
import {IPageChangeEvent} from "@covalent/core";

/**
 * List templates from repository ready for installation.
 */
@Component({
    selector: "list-templates",
    styleUrls: ['js/repository/list/list.component.css'],
    templateUrl: "js/repository/list/list.component.html"
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
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    /**
     * List of available templates
     */
    templates: TemplateMetadata[] = [];
    filteredList: TemplateMetadata[] = [];
    repositories: TemplateRepository[] = [];
    selectedRepository: TemplateRepository;

    dataSource = new MatTableDataSource();

    public ngOnInit() {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
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
                this.dataSource.data = data;
                this.search();
                this.loading = false;
            },
            (error: any) => {
                console.log(error);
                if(error.developerMessage)
                    this.errorMsg += error.developerMessage;

                console.log(this.errorMsg);
                this.loading = false;
            }
        );
    }

    sortData(sort: Sort) {

        this.dataSource.data= this.dataSource.data.sort((a, b) => {
            const isAsc = sort.direction === 'asc';
            switch (sort.active) {
                case 'templateName': return this.compare(a.templateName, b.templateName, isAsc);
                default: return 0;
            }
        });
    }

    private compare(a: number | string, b: number | string, isAsc: boolean) {
        return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
    }

    /*
     * download template from repository.
     */
    downloadTemplate(template: TemplateMetadata) {
        this.templateService.downloadTemplate(template).subscribe(blob => {
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
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

    pageSize: number = 50;
    currentPage: number = 1;
    fromRow: number = 1;
    filteredTotal = 0;
    searchTerm = '';
    pageEvent: PageEvent;

    search(): void {
        // this.dataSource.filter = filter.trim().toLowerCase();
        let newData = this.dataTableService.filterData(this.templates, this.searchTerm, true, []);
        this.filteredTotal = newData.length;
        newData = this.dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredList = newData;
    }

    page(pagingEvent: IPageChangeEvent): void {
        console.log(pagingEvent.fromRow, pagingEvent.page, pagingEvent.pageSize)
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

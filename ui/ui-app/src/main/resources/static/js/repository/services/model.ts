import {DataSource} from "@angular/cdk/table";
import {Observable} from "rxjs/Observable";
import {CollectionViewer} from "@angular/cdk/collections";

export class TemplateMetadata {

    fileName: string;
    type: string;
    description: string;
    templateName: string;
    installed: boolean
}

export class ImportStatus{

    fileName: string;
    templateName: string;
    success: boolean;
    valid: boolean;
}

export class TemplateDatasource extends DataSource<TemplateMetadata> {

    public constructor(private items$: Observable<TemplateMetadata[]>) {
        super()
    }

    public connect(collectionViewer: CollectionViewer): Observable<TemplateMetadata[]> {
        return this.items$;
    }

    public disconnect(collectionViewer: CollectionViewer): void {}
}
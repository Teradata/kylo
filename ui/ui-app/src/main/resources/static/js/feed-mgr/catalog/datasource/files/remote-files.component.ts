import {RemoteFile, RemoteFileDescriptor} from './remote-file';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';


export class RemoteFilesComponent extends BrowserComponent {


    init(): void {
        if (this.path === "undefined") { //e.g. when navigating from Catalog into Files
            const path = this.datasource.template.paths[0];
            this.browse(path, "replace");
        } else {
            this.initData();
        }
    }

    getColumns() {
        return RemoteFileDescriptor.COLUMNS;
    }

    getSortByColumnName() {
        return this.columns[1].name;
    }

    getStateName(): string {
        return "catalog.datasource.browse";
    }

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/files?path=";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new RemoteFile(obj.name, obj.path, obj.directory, obj.length, obj.modificationTime);
    }

}

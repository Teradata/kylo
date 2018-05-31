import {RemoteFile, RemoteFileDescriptor} from './remote-file';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';


export class RemoteFilesComponent extends BrowserComponent {

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

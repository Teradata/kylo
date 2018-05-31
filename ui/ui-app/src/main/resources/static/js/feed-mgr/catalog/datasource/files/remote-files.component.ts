import {RemoteFile, RemoteFileDescriptor} from './remote-file';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';


export class RemoteFilesComponent extends BrowserComponent {

    getColumns() {
        console.log("get columns");
        return RemoteFileDescriptor.COLUMNS;
    }

    getSortByColumnName() {
        return this.columns[1].name;
    }

    getStateName(): string {
        return "catalog.datasource.browse";
    }

    getUrl(): string {
        console.log('get url');
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/files?path=";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        console.log('map remote file');
        return new RemoteFile(obj.name, obj.path, obj.directory, obj.length, obj.modificationTime);
    }

}

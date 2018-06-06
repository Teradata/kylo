import {RemoteFile, RemoteFileDescriptor} from './remote-file';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';
import {Node} from '../api/node';


export class RemoteFilesComponent extends BrowserComponent {

    init(): void {
        if (this.params.path === undefined) { //e.g. when navigating from Catalog into Files
            this.params.path = this.datasource.template.paths[0];
            this.browse(this.params, "replace");
        } else {
            this.initData();
        }
    }


    createRootNode(): Node {
        return new Node(this.datasource.template.paths[0]);
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

    createParentNodeParams(node: Node): any {
        return {path: node.path};
    }

    createChildBrowserObjectParams(obj: BrowserObject): object {
        return {path: this.params.path !== undefined ? this.params.path + "/" + obj.name : obj.name};
    }

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/files";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new RemoteFile(obj.name, obj.path, obj.directory, obj.length, obj.modificationTime);
    }

}

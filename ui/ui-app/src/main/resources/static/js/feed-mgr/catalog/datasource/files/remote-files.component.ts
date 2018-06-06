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

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/files";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new RemoteFile(obj.name, obj.path, obj.directory, obj.length, obj.modificationTime);
    }

    createParentNodeParams(node: Node): any {
        return {path: node.getPathNodes().map(n => n.name).join("/")};
    }

    createChildBrowserObjectParams(obj: BrowserObject): object {
        return {path: this.params.path !== undefined ? this.params.path + "/" + obj.name : obj.name};
    }

    findOrCreateThisNode(root: Node, params: any): Node {
        if (params.path === undefined || params.path === '') {
            return root;
        }

        let relativePath = params.path.substring(root.name.length, params.path.length);
        if (relativePath.length > 0) {
            let node: Node = root;
            let paths = relativePath.split("/").filter(p => p.length > 0);
            for (let path of paths) {
                let child = node.childrenMap.get(path);
                if (child === undefined) {
                    child = new Node(path);
                    node.addChild(child);
                }
                node = child;
            }
            return node;
        } else {
            return root;
        }
    }

}

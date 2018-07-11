import {RemoteFile, RemoteFileDescriptor} from './remote-file';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../../api/models/browser-object';
import {Node} from '../../api/models/node';
import {Input} from "@angular/core";


export class RemoteFilesComponent extends BrowserComponent {



    init(): void {
        if (this.params.path === undefined) { //e.g. when navigating from Catalog into Files
            this.params.path = this.datasource.template.paths[0];
            if(this.useRouterStates) {
                this.browseTo(this.params, "replace");
            }
            else {
                this.initData();
            }
        }
        else {
            this.initData();
        }
    }


    createRootNode(): Node {
        const rootPath = this.datasource.template.paths[0];
        const root = new Node(rootPath);
        root.setBrowserObject(new RemoteFile(rootPath, rootPath, false, 0, new Date()));
        return root;
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

    createChildBrowserObjectParams(obj: BrowserObject): object {
        return {path: (<RemoteFile>obj).path};
    }

    createParentNodeParams(node: Node): any {
        const pathNodes = node.getPathNodes();
        const root = pathNodes[0];
        if (RemoteFilesComponent.isAzure(new URL(root.getName()))) {
            return {path: (<RemoteFile>node.getBrowserObject()).path};
        } else {
            return {path: pathNodes.map(n => n.getName()).join("/")};
        }
    }

    findOrCreateThisNode(root: Node, params: any): Node {
        if (params.path === undefined || params.path === '') {
            return root;
        }

        const rootUrl = new URL(root.getName());
        const pathUrl = new URL(params.path);

        if (RemoteFilesComponent.isAzure(rootUrl)) {
            let node: Node = root;
            let child: Node;

            const pathname = pathUrl.pathname;
            //remove first slash and split into segments
            const segments = pathname.substring(1).split("/").filter(p => p.length > 0);

            //First segment can have container and host
            const segment = segments[0];
            const containerNameIdx = segment.indexOf("@");
            let containerPath;
            if (containerNameIdx > 0) {
                //has container
                const container = segment.substring(0, containerNameIdx);
                child = node.getChild(container);
                if (child === undefined) {
                    child = new Node(container);
                    containerPath = rootUrl.protocol + "//" + segment + "/";
                    child.setBrowserObject(RemoteFilesComponent.createTempPlaceholder(container, containerPath));
                    node.addChild(child);
                }
                node = child;
            } else {
                //there is no container, so there must be no more segments either
                if (segments.length > 1) {
                    console.error("Invalid Azure URL '" + params.path + "'. Found reference to file(s) without a container");
                }
            }

            // Remove the container from the segments
            segments.shift();

            for (let path of segments) {
                child = node.getChild(path);
                if (child === undefined) {
                    child = new Node(path);
                    const childPath = containerPath + "/" + path;
                    child.setBrowserObject(RemoteFilesComponent.createTempPlaceholder(path, childPath));
                    node.addChild(child);
                }
                node = child;
            }
            return node;

        } else {
            //for all others types of file protocols
            const rootPath = rootUrl.toString(); //normalise root url
            const path = pathUrl.toString(); //normalise path url
            let relativePath = path.substring(rootPath.length, path.length);
            if (relativePath.length > 0) {
                let node: Node = root;
                const splits: string[] = relativePath.split("/");
                const paths = splits.filter(p => p.length > 0);
                for (let path of paths) {
                    let child = node.getChild(path);
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

    /**
     * Create temporary placeholder for file.
     * Its directory indicator, length and date are not accurate, but its ok since its only a placeholder until user browses
     * to the parent at which point this object will be replaced with result from server.
     * @param {string} name
     * @param {string} path
     * @returns {RemoteFile}
     */
    private static createTempPlaceholder(name: string, path: string) {
        return new RemoteFile(name, path, true, 0, new Date());
    }

    private static isAzure(url: URL) {
        return url.protocol === "wasb:";
    }

}

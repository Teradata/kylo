import {DatabaseObject, DatabaseObjectDescriptor, DatabaseObjectType} from './database-object';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../../api/models/browser-object';
import {BrowserColumn} from '../../api/models/browser-column';
import {Node} from '../../api/models/node';

export class TablesComponent extends BrowserComponent {

    init(): void {
        this.initData();
    }

    getColumns(): BrowserColumn[] {
        return DatabaseObjectDescriptor.COLUMNS;
    }

    getSortByColumnName(): string {
        return this.columns[1].name;
    }

    getStateName(): string {
        return "catalog.datasource.connection";
    }

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/tables";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new DatabaseObject(obj.name, obj.type, obj.catalog, obj.schema);
    }

    createRootNode(): Node {
        if (this.datasource.template && this.datasource.template.options && this.datasource.template.options.url) {
            return new Node(this.datasource.template.options.url);
        } else if (this.datasource.connector && this.datasource.connector.template && this.datasource.connector.template.options && this.datasource.connector.template.options.url) {
            return new Node(this.datasource.connector.template.options.url);
        } else {
            return new Node("");
        }
    }

    createParentNodeParams(node: Node): any {
        const params = {
            catalog: '',
            schema: ''
        };
        const dbObj: DatabaseObject = <DatabaseObject>node.getBrowserObject();
        if (dbObj === undefined) {
            //root node for database will have no browser object
            return params;
        }
        if (dbObj.type === DatabaseObjectType.Catalog) {
            return params.catalog = dbObj.name;
        } else if (dbObj.type === DatabaseObjectType.Schema) {
            return params.schema = dbObj.name;
        }

        return params;
    }

    createChildBrowserObjectParams(obj: BrowserObject): any {
        const child: DatabaseObject = <DatabaseObject> this.files.find(f => f.name === obj.name);
        if (child.type === DatabaseObjectType.Catalog) {
            this.params.catalog = child.name;
        } else if (child.type === DatabaseObjectType.Schema) {
            this.params.schema = child.name;
        } else if (child.type === DatabaseObjectType.Table) {
            this.params.table = child.name;
        } else if (child.type === DatabaseObjectType.Column) {
            this.params.column = child.name;
        }
        return this.params;
    }

    findOrCreateThisNode(root: Node, params: any): Node {
        if (params.catalog === undefined && params.schema === undefined) {
            return root;
        }
        if (params.catalog) {
            let node = root.getChild(params.catalog);
            if (node === undefined) {
                node = new Node(params.catalog);
                node.setBrowserObject(this.createTempPlaceholder(params.catalog, DatabaseObjectType.Catalog));
                root.addChild(node);
            }
            return node;
        } else if (params.schema) {
            let node = root.getChild(params.schema);
            if (node === undefined) {
                node = new Node(params.schema);
                node.setBrowserObject(this.createTempPlaceholder(params.schema, DatabaseObjectType.Schema));
                root.addChild(node);
            }
            return node;
        }

        return undefined;
    }

    private createTempPlaceholder(name: string, type: DatabaseObjectType) {
        return new DatabaseObject(name, type, undefined, undefined);
    }
}

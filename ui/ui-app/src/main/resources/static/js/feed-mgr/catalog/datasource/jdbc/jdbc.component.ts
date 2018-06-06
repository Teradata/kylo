import {DatabaseObject, DatabaseObjectDescriptor, DatabaseObjectType} from './database-object';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';
import {BrowserColumn} from '../api/browser-column';
import {Node} from '../api/node';

export class JdbcComponent extends BrowserComponent {

    init(): void {
        this.initData();
    }

    getColumns(): BrowserColumn[] {
        return DatabaseObjectDescriptor.COLUMNS;
    }

    getSortByColumnName(): string {
        return this.columns[0].name;
    }

    getStateName(): string {
        return "catalog.datasource.connection";
    }

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/tables";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new DatabaseObject(obj.name, obj.type);
    }

    createRootNode(): Node {
        return new Node(this.datasource.template.options.url);
    }

    createParentNodeParams(node: Node): any {
        console.log('createParentNodeParams');
        const params = {
            catalog: undefined,
            schema: undefined
        };
        const dbObj: DatabaseObject = <DatabaseObject>node.browserObject;
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
                node.setBrowserObject(new DatabaseObject(params.catalog, DatabaseObjectType.Catalog));
            }
            return node;
        } else if (params.schema) {
            let node = root.getChild(params.schema);
            if (node === undefined) {
                node = new Node(params.schema);
                node.setBrowserObject(new DatabaseObject(params.schema, DatabaseObjectType.Schema));
            }
            return node;
        }

        return undefined;
    }
}

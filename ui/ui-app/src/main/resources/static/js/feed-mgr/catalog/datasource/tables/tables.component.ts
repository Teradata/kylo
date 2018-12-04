import {DatabaseObject, DatabaseObjectDescriptor, DatabaseObjectType} from './database-object';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../../api/models/browser-object';
import {BrowserColumn} from '../../api/models/browser-column';
import {Node} from '../../api/models/node';
import {Component} from "@angular/core";
import * as _ from "underscore"

@Component({
    selector: "catalog-table-browser",
    styleUrls: ["../api/browser.component.scss"],
    templateUrl: "../api/browser.component.html"
})
export class TablesComponent extends BrowserComponent {

    hideInternalTables: boolean = true;
    showHideInternalOption : boolean = true;

    init(): void {
        this.initData();
        this.showHideInternalOption = (this.datasource.template.format === 'hive');
        this.hideInternalTables = (this.showHideInternalOption);
    }

    /**
     * Ensure the params has some values in it to trigger the state to reload
     * @param params
     */
    protected browse(params: any): void {
        if(_.isEmpty(params)){
            params.schema="";
            params.catalog = "";
        }
        this.browseTo(params, undefined);
    }

    getColumns(): BrowserColumn[] {
        return DatabaseObjectDescriptor.COLUMNS;
    }

    getSortByColumnName(): string {
        return this.columns[2].name;
    }

    getStateName(): string {
        return "catalog.datasource.connection";
    }

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/tables";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new DatabaseObject(obj.name, obj.type, obj.catalog, obj.schema, obj.qualifiedIdentifier);
    }

    createRootNode(): Node {
        let node: Node = undefined;
        if (this.datasource.template && this.datasource.template.options && this.datasource.template.options.url) {
            node = new Node(this.datasource.template.options.url);
        } else if (this.datasource.connector && this.datasource.connector.template && this.datasource.connector.template.options && this.datasource.connector.template.options.url) {
            node = new Node(this.datasource.connector.template.options.url);
        } else {
            node = new Node("");
        }
        if (node.name == "" && this.datasource && this.datasource.title) {
            node.name = this.datasource.title;
        }
        return node;
    }

    createParentNodeParams(node: Node): any {
        let datasourceId = this.params && this.params.datasourceId ? this.params.datasourceId : undefined;
        let params: any = {}
        if (datasourceId) {
            params.datasourceId = datasourceId;
        }
        const dbObj: DatabaseObject = <DatabaseObject>node.getBrowserObject();
        if (dbObj === undefined) {
            //root node for database will have no browser object
            return params;
        }
        if (dbObj.type === DatabaseObjectType.Catalog) {
            params.catalog = dbObj.name;
        } else if (dbObj.type === DatabaseObjectType.Schema) {
            params.schema = dbObj.name;
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

    private isEmpty(item: string) {
        return item === undefined || item == "";
    }

    findOrCreateThisNode(root: Node, params: any): Node {
        if (this.isEmpty(params.catalog) && this.isEmpty(params.schema)) {
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

    /**
     * Filter internal tables
     * @param {BrowserObject[]} data
     * @returns {BrowserObject[]}
     */
    applyCustomFilter(data: BrowserObject[]): BrowserObject[] {

        if (this.hideInternalTables) {
            data = data.filter((item) => {
                return (!(item.name.endsWith("_invalid") || item.name.endsWith("_profile") || item.name.endsWith("_valid") || item.name.endsWith("_feed")));
            });
        }
        return data;
    }

    private createTempPlaceholder(name: string, type: DatabaseObjectType) {
        return new DatabaseObject(name, type, undefined, undefined, undefined);
    }

}

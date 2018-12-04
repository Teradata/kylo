import {Injectable} from "@angular/core";
import {Node} from '../models/node';
import {ObjectUtils} from "../../../../../lib/common/utils/object-utils";
import {KyloObject} from "../../../../../lib/common/common.model";
import {PreviewDataSet} from "../../datasource/preview-schema/model/preview-data-set";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {SelectedItem} from "../../datasource/api/dialog/selection-dialog.component";

/**
 * Defines what to do when Node is selected/de-selected in BrowserComponent
 */
export interface SelectionStrategy {
    toggleAllChildren(node: Node, checked: boolean): void;
    toggleChild(node: Node, childName: string, checked: boolean): void;
    isChildSelected(node: Node, childName: string): boolean;
    isSelectChildDisabled(node: Node, childName: string): boolean;
    isSelectAllDisabled(node: Node): boolean;
}

/**
 * Participates in deciding what selection actions are allowed for end user.
 */
export interface SelectionPolicy {
    isSelectAllDisabled(node: Node): boolean;
    isSelectChildDisabled(node: Node, childName: string): boolean;
    toggleNode(node: Node, checked: boolean): void;
}

/**
 * Allows for multiple times to be selected on any path.
 * If parent item (item which hold other items) is selected then all
 * its descendants will be automatically deselected.
 */
export class DefaultSelectionPolicy implements SelectionPolicy {
    constructor() {}
    toggleNode(node: Node, checked: boolean): void {
        if (checked) {
            this.uncheckAllDescendants(node);
        }
    }
    isSelectChildDisabled(node: Node, childName: string): boolean {
        return node.isAnyParentSelected();
    }
    isSelectAllDisabled(node: Node): boolean {
        return node.isAnyParentSelected();
    }
    private uncheckAllDescendants(node: Node) {
        for (let child of node.children()) {
            child.setSelected(false);
            this.uncheckAllDescendants(child);
        }
    }
}

/**
 * Allows only a single item to be selected at a time.
 */
export class SingleSelectionPolicy implements SelectionPolicy {
    private selectedNode: Node = new Node('initial-placeholder');

    constructor() {}

    toggleNode(node: Node, checked: boolean): void {
        this.selectedNode.setSelected(false);
        this.selectedNode = node;
        this.selectedNode.setSelected(checked);
    }

    isSelectChildDisabled(node: Node, childName: string): boolean {
        return false;
    }
    isSelectAllDisabled(node: Node): boolean {
        return true;
    }
}

/**
 * Does not allow parent items (items which can hold other items) to be selected, e.g.
 * this will not allow for directories to be selected in file browser or for schemas and
 * catalogs to be selected in database browser.
 */
export class BlockParentObjectSelectionPolicy implements SelectionPolicy {

    constructor() {}
    toggleNode(node: Node, checked: boolean): void {
    }
    isSelectChildDisabled(node: Node, childName: string): boolean {
        let childNode = node.getChild(childName);
        if(childNode != undefined){
            return childNode.getBrowserObject().canBeParent();
        }
        else {
            return true;
        }
    }
    isSelectAllDisabled(node: Node): boolean {
        return false;
    }
}

/**
 * Uses SelectionPolicy array to decide what selection actions are available to user.
 * All SelectionPolicies participate to decide whether action is available or not where
 * their "votes" are OR'ed, i.e. it is enough for one SelectionPolicy to block an action.
 */
export class DefaultSelectionStrategy implements SelectionStrategy {

    private policies: SelectionPolicy[] = [];

    withPolicy(policy: SelectionPolicy): DefaultSelectionStrategy {
        this.policies.push(policy);
        return this;
    }

    isSelectAllDisabled(node: Node): boolean {
        return this.policies.map(p => p.isSelectAllDisabled(node)).reduce((sum, next) => sum || next, false);
    }

    isSelectChildDisabled(node: Node, childName: string): boolean {
        return this.policies.map(p => p.isSelectChildDisabled(node, childName)).reduce((sum, next) => sum || next, false);
    }

    isChildSelected(node: Node, childName: string): boolean {
        return node.isAnyParentSelected() || node.isChildSelected(childName);
    }

    toggleAllChildren(node: Node, checked: boolean): void {
        const children = node.children();
        for (let child of children) {
            this.toggleNode(child, checked);
        }
    }

    toggleChild(node: Node, childName: string, checked: boolean): void {
        const child = node.getChild(childName);
        this.toggleNode(child, checked);
    }

    private toggleNode(node: Node, checked: boolean) {
        if(node) {
            node.setSelected(checked);
            for (let policy of this.policies) {
                policy.toggleNode(node, checked);
            }
        }
    }


    /**
     * check to see if the passed in policy exists in this service
     * Example:
     *  hasPolicy(SingleSelectionPolicy)
     *
     * @param {{new(): T}} type
     * @return {boolean}
     */
    hasPolicy<T extends SelectionPolicy>(type: { new(): T ;}):boolean {
        return this.policies.find(policy => ObjectUtils.getObjectClass(policy) === type.name) != undefined;
    }
}

@Injectable()
export class SelectionService {

    private singleSelection:boolean;
    private selections: Map<string, any> = new Map<string, any>();
    private lastPath: Map<string, any> = new Map<string, any>();
    private selectionStrategy: SelectionStrategy = new DefaultSelectionStrategy()
        .withPolicy(new DefaultSelectionPolicy())
        .withPolicy(new SingleSelectionPolicy())
        .withPolicy(new BlockParentObjectSelectionPolicy());

    constructor(){
        this.singleSelection = this.hasPolicy(SingleSelectionPolicy);
    }

    singleSelectionStrategy(){
        this.selectionStrategy = new DefaultSelectionStrategy()
            .withPolicy(new DefaultSelectionPolicy())
            .withPolicy(new SingleSelectionPolicy())
            .withPolicy(new BlockParentObjectSelectionPolicy());
        this.singleSelection = true;
    }

    multiSelectionStrategy(){
        this.selectionStrategy = new DefaultSelectionStrategy()
            .withPolicy(new DefaultSelectionPolicy())
            .withPolicy(new BlockParentObjectSelectionPolicy());
        this.singleSelection = false;
    }

    /**
     * Stores selection for data source
     * @param {string} datasourceId
     * @param {any} selection
     */
    set(datasourceId: string, selection: any): void {
        this.selections.set(datasourceId, selection);
    }

    /**
     * Resets selection for data source
     * @param {string} datasourceId
     */
    reset(datasourceId?: string): void {
        if(datasourceId) {
            this.selections.delete(datasourceId);
        }
        else {
            //clear it all
            this.selections.clear()
        }
    }

    clearSelected(datasourceId?: string): void {
        let root:Node = this.get(datasourceId);
        if(root){
            const nodes = root.getSelectedDescendants();
            if(nodes){
                nodes.forEach((node:Node) => {
                   node.setSelected(false)
                });
            }
        }
    }

    /**
     * @param {string} datasourceId
     * @returns {any} selection for data source
     */
    get(datasourceId: string): any {
        return this.selections.get(datasourceId);
    }

    setLastPath(datasourceId: string, params: any):void {
        this.lastPath.set(datasourceId, params)
    }

    getLastPath(datasourceId: string): any {
        return this.lastPath.get(datasourceId)
    }

    /**
     * For the lastPath of a given datasource return the name
     * @param {string} datasourceId
     * @return {string}
     */
    getLastPathNodeName(datasourceId: string):string {
      let lastPath = this.getLastPath(datasourceId);
      let pathString = undefined;
      if(lastPath && lastPath.path) {
          pathString = lastPath.path;
      }
      return this.getLastNameInPath(pathString);
    }

    /**
     * For a given path return the last item after the final '/'
     * @param {string} path
     * @return {string}
     */
    getLastNameInPath(path: string):string {
        let name :string = undefined;
      if(path) {
          if(path.lastIndexOf("/") >0) {
              name = path.substring(path.lastIndexOf("/")+1);
          }
        }
        return name;
    }

    getSelectionStrategy(): SelectionStrategy {
        return this.selectionStrategy;
    }

    setSelectionStrategy(strategy: SelectionStrategy) {
        this.selectionStrategy = strategy;
        this.singleSelection = this.hasPolicy(SingleSelectionPolicy);
    }

    /**
     * check to see if the passed in policy exists in this service
     * Example:
     *  hasPolicy(SingleSelectionPolicy)
     *
     * @param {{new(): T}} type
     * @return {boolean}
     */
    hasPolicy<T extends SelectionPolicy>(type: { new(): T ;}):boolean {
        return (<DefaultSelectionStrategy>this.selectionStrategy).hasPolicy(type);
    }

    isSingleSelection(){
        return this.singleSelection;
    }

    isMultiSelection(){
        return !this.isSingleSelection();
    }
}

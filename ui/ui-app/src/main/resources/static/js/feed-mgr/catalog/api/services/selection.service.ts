import {Injectable} from "@angular/core";
import {Node} from '../models/node';

/**
 * Defines what to do when Node is selected/de-selected in BrowserComponent.
 */
export interface SelectionStrategy {
    toggleAllChildren(node: Node, checked: boolean): void;
    toggleChild(node: Node, childName: string, checked: boolean): void;
}

/**
 * Allows to select multiple items on different paths.
 */
export class MultipleSelectionStrategy implements SelectionStrategy {
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
        node.setSelected(checked);
        if (checked) {
            this.uncheckAllDescendants(node);
        }
    }

    private uncheckAllDescendants(node: Node) {
        for (let child of node.children()) {
            child.setSelected(false);
            this.uncheckAllDescendants(child);
        }
    }
}

/**
 * Allows a single item to be selected at a time.
 */
export class SingleSelectionStrategy implements SelectionStrategy {

    private selectedNode: Node;

    /**
     * This should not really be called since its a single selection at a time.
     */
    toggleAllChildren(node: Node, checked: boolean): void {
    }

    toggleChild(node: Node, childName: string, checked: boolean): void {
        const child = node.getChild(childName);
        this.selectedNode.setSelected(false);
        this.selectedNode = child;
        this.selectedNode.setSelected(checked);
    }
}

@Injectable()
export class SelectionService {

    private selections: Map<string, any> = new Map<string, any>();
    private lastPath: Map<string, any> = new Map<string, any>();
    private selectionStrategy: SelectionStrategy = new MultipleSelectionStrategy();

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
    reset(datasourceId: string): void {
        this.selections.delete(datasourceId);
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

    getSelectionStrategy(): SelectionStrategy {
        return this.selectionStrategy;
    }

    setSelectionStrategy(strategy: SelectionStrategy) {
        this.selectionStrategy = strategy;
    }
}

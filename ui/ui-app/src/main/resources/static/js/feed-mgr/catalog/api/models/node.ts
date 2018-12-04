import {BrowserObject} from './browser-object';

/**
 * Browser object hierarchy
 */
export class Node {
    public name: string;
    public displayName: string;
    private isSelected: boolean = false;
    private childrenMap: Map<string, Node> = new Map<string, Node>();
    private parent: Node;
    private browserObject: BrowserObject;

    constructor(name: string) {
        this.name = name;
        this.displayName = decodeURI(name);
    }

    countSelectedDescendants(): number {
        let count: number = 0;
        const children = this.children();
        for (let child of children) {
            count += child.isSelected ? 1 : 0;
            count += child.countSelectedDescendants();
        }
        return count;
    }

    getSelectedDescendants(): Node[] {
        let selectedItems: Node[] = [];
        const children = this.children();
        for (let child of children) {
            if (child.isSelected) {
                selectedItems.push(child);
            } else {
                const selectedDescendants = child.getSelectedDescendants();
                selectedItems = selectedItems.concat(selectedDescendants);
            }
        }
        return selectedItems;
    }

    isAnyParentSelected(): boolean {
        if (this.isSelected) {
            return true;
        }
        if (!this.parent) {
            return false;
        }
        if (this.parent.isSelected) {
            return true;
        }
        return this.parent.isAnyParentSelected();
    }

    children() {
        return Array.from(this.childrenMap.values());
    }

    isChildSelected(name: string) {
        let node = this.childrenMap.get(name);
        if(node != undefined ){
            return node.isSelected;
        }
        else {
            return false;
        }
    }

    countSelectedChildren() {
        return this.getSelectedChildren().length;
    }

    private getSelectedChildren() {
        return this.children().filter(c => c.isSelected);
    }

    addChild(node: Node): void {
        if (!this.childrenMap.get(node.name)) {
            node.parent = this;
            this.childrenMap.set(node.name, node);
        }
    }

    getPathNodes(): Node[] {
        const pathNodes: Node[] = [];
        pathNodes.push(this);
        let parent = this.parent;
        while (parent) {
            pathNodes.push(parent);
            parent = parent.parent;
        }
        return pathNodes.reverse();
    }

    getChild(name: string): Node {
        return this.childrenMap.get(name);
    }

    setBrowserObject(browserObj: BrowserObject) {
        this.browserObject = browserObj;
    }

    setSelected(selected: boolean): void {
        this.isSelected = selected;
    }

    getBrowserObject(): BrowserObject {
        return this.browserObject;
    }

    getName(): string {
        return this.name;
    }
}
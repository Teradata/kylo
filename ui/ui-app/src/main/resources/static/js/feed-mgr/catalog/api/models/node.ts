import {BrowserObject} from './browser-object';

/**
 * Browser object hierarchy
 */
export class Node {
    name: string;
    private isSelected: boolean = false;
    childrenMap: Map<string, Node> = new Map<string, Node>();
    parent: Node;
    browserObject: BrowserObject;

    constructor(name: string) {
        this.name = name;
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
        return this.childrenMap.get(name).isSelected;
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

    /**
     * Temporary just not to break existing Preview code,
     * this is currently only valid for subset of files, e.g. valid for Local/HDFS/S3 files, but not valid for Azure
     * @returns {string} full path of this Node
     */
    getPath(): string {
        return this.browserObject.path;
    }

    setSelected(selected: boolean): void {
        this.isSelected = selected;
    }
}
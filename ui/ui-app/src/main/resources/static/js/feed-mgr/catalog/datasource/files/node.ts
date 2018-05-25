/**
 * File hierarchy
 */
export class Node {
    name: string;
    isSelected: boolean = false;
    private childrenMap: Map<string, Node> = new Map<string, Node>();
    private parent: Node;
    private path: string;

    constructor(name: string) {
        this.name = name;
        this.path = name;
    }

    find(paths: string[]): Node {
        const ownName = paths.splice(0, 1)[0];
        if (this.name !== ownName) {
            throw ReferenceError("Invalid path, expecting this node's name as first path name");
        }
        if (paths.length === 0) {
            return this;
        }

        const childName = paths[0];
        let child = this.childrenMap.get(childName);
        if (child === undefined) {
            //e.g. when browsing to this path for the fist time
            child = new Node(childName);
            this.addChild(child);
        }
        if (paths.length > 0) {
            return child.find(paths);
        } else {
            return child;
        }
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

    addChild(node: Node): void {
        if (!this.childrenMap.get(node.name)) {
            node.parent = this;
            node.path = this.path + "/" + node.path;
            this.childrenMap.set(node.name, node);
        }
    }

    toggleAll(isSelected: boolean) {
        const children = this.children();
        for (let child of children) {
            Node.toggleNode(child, isSelected);
        }
    }

    toggleChild(name: string, isSelected: boolean) {
        const child = this.childrenMap.get(name);
        Node.toggleNode(child, isSelected);
    }

    private static toggleNode(node: Node, isSelected: boolean) {
        node.isSelected = isSelected;
        if (isSelected) {
            for (let child of node.children()) {
                child.isSelected = false;
                child.toggleAll(false);
            }
        }
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

    getSelectedChildren() {
        return this.children().filter(c => c.isSelected);
    }

    getPath(): string {
        return this.path;
    }
}

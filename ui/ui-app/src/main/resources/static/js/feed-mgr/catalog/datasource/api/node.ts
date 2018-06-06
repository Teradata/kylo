/**
 * File hierarchy
 */
export class Node {
    name: string;
    isSelected: boolean = false;
    childrenMap: Map<string, Node> = new Map<string, Node>();
    parent: Node;
    path: string;

    constructor(name: string) {
        this.name = name;
        this.path = name;
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

    addChild(node: Node): void {
        if (!this.childrenMap.get(node.name)) {
            node.parent = this;
            node.path = this.path + "/" + node.name;
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

    private getSelectedChildren() {
        return this.children().filter(c => c.isSelected);
    }

    getPath(): string {
        return this.path;
    }

    /**
     * @param {string} params - query parameters which define the 'path' of this node
     * @returns {Node}
     */
    findFullPath(params: any): Node {
        if (params.path === undefined || params.path === '' || this.path === params.path) {
            return this;
        }

        let relativePath = params.path.substring(this.path.length, params.path.length);
        if (relativePath.length > 0) {
            let node: Node = this;
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
            return this;
        }
    }
}
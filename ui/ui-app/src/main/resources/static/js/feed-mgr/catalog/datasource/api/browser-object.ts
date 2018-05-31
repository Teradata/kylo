export class BrowserObject {
    name: string;
    path: string;

    canBeParent(): boolean {
        return false;
    }
}
declare function fattable(parameters: object): fattable.TableView;

declare module fattable {

    class TableModel {
        hasCell(i: number, j: number): boolean;
        hasHeader(j: number): boolean;
        getCell(i: number, j: number, cb: (value: string) => void): void;
        getHeader(j: number, cb: (value: string) => void): void;
    }

    // Extends this class if you
    // don't need to access your data in a asynchronous
    // fashion (e.g. via ajax).
    //
    // You only need to override
    // getHeaderSync and getCellSync
    class SyncTableModel extends TableModel {
       getCellSync(i: number, j: number): string;
       getHeaderSync(j: number): string;
    }

    // Extend this class if you have access
    // to your data in a page fashion
    // and you want to use a LRU cache
    class PagedAsyncTableModel extends TableModel {
        constructor();
        constructor(cacheSize: number);

        // Override me
        // Should return a string identifying your page.
        cellPageName(i: number, j: number): string;

        // Override me
        // Should return a string identifying the page of the column.
        headerPageName(j: number): string;

        // override this
        // a page is a function that
        // returns the cell value for any(i, j)
        fetchCellPage(pageName: string, cb: (page: (i: number, j: number) => string) => void): void;
    }

    // The cell painter tells how
    // to fill, and style cells.
    // Do not set height or width.
    // in either fill and setup methods.
    class Painter {

        // Setup method are called at the creation
        // of the cells. That is during initialization
        // and for all window resize event.
        //
        // Cells are recycled.
        setupCell(cellDiv: HTMLElement): void;

        // Setup method are called at the creation
        // of the column header. That is during
        // initialization and for all window resize
        // event.
        //
        // Columns are recycled.
        setupHeader(headerDiv: HTMLElement): void;

        // Will be called whenever a cell is
        // put out of the DOM
        cleanUpCell(cellDiv: HTMLElement): void;

        // Will be called whenever a column is
        // put out of the DOM
        cleanUpHeader(headerDiv: HTMLElement): void;


        cleanUp(table: object): void;

        // Fills and style a column div.
        fillHeader(headerDiv: HTMLElement, data: object): void;

        // Fills and style a cell div.
        fillCell(cellDiv: HTMLElement, data: object): void;

        // Mark a column header as pending.
        // Its content is not in cache
        // and needs to be fetched
        fillHeaderPending(headerDiv: HTMLElement): void;

        // Mark a cell content as pending
        // Its content is not in cache and
        // needs to be fetched
        fillCellPending(cellDiv: HTMLElement): void;
    }

    class TableView {
        readRequiredParameter(parameters: any, k: any, default_value: any): void;
        constructor(paramaters: any);
        getContainerDimensions(): void;
        leftTopCornerFromXY(x: number, y: number): {x: number, y: number};
        cleanUp(): void;
        setup(): void;
        refreshAllContent(evenNotPending?: boolean): void;
        onScroll(x: number, y: number): void;
        goTo(i: number, y: number): void;
        display(i: number, y: number): void;
        moveX(j: number): void;
        moveY(i: number): void;
    }
}

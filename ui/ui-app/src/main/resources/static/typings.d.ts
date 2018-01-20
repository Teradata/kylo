// Typings reference file, see links for more information
// https://github.com/typings/typings
// https://www.typescriptlang.org/docs/handbook/writing-declaration-files.html

declare interface IScope extends angular.IScope {
    [key: string]: any;
}

declare class ocLazyLoad {

    /**
     * Load a module or a list of modules into Angular
     * @param module the name of a predefined module config object, or a module config object, or an array of either
     * @param params optional parameters
     * @returns promise
     */
    load(module: string | object | [string | object], params?: object): Promise<any>;
}

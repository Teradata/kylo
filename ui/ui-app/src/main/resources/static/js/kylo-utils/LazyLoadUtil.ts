import * as _ from 'underscore';
class LazyLoadUtilClass{
    constructor(){}
         /**
         * Used for ui-router to lazy load a given controller
         * @param path the requirejs path to the controller you want to load
         * @param moduleDependencies an optional requirejs path or array of paths to the module-require file that describes the dependencies needed for the controller
         * @returns {[string,*]}
         */
        lazyLoadController = function lazyLoad(path: any, moduleDependencies: any) {
            return ['$ocLazyLoad','$rootScope', ($ocLazyLoad: any,$rootScope: any)=> {
                if (moduleDependencies != null && moduleDependencies != undefined) {
                    if (!_.isArray(path)) {
                        path = [path];
                    }
                    var dependencies: any[] = null;
                    if(_.isArray(moduleDependencies)){
                        dependencies = moduleDependencies;
                    }
                    else {
                        dependencies = [moduleDependencies];
                    }

                    //reverse sort and add
                    dependencies.sort(function (a, b) {
                        if (a < b) return 1;
                        if (b < a) return -1;
                        return 0;
                    });

                    _.each(dependencies,(dependency: any)=> {

                        if (_.indexOf(path,dependency) < 0) {
                            path.unshift(dependency);
                        }

                    })

                }
                return $ocLazyLoad.load(path, {serie: true})
            }]
        }

        lazyLoad = function lazyLoad(moduleDependencies: any) {
            return ['$ocLazyLoad','$rootScope', ($ocLazyLoad: any,$rootScope: any)=> {
                if (moduleDependencies != null && moduleDependencies != undefined) {
                    var dependencies: any[] = null;
                    if(_.isArray(moduleDependencies)){
                        dependencies = moduleDependencies;
                    }
                    else {
                        dependencies = [moduleDependencies];
                    }
                }
                return $ocLazyLoad.load(dependencies, {serie: true})
            }]
        }
}

const lazyLoadUtil = new LazyLoadUtilClass();
export default lazyLoadUtil;
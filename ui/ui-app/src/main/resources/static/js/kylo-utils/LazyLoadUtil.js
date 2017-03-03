define([], function () {

    function LazyLoadUtil() {
        /**
         * Used for ui-router to lazy load a given controller
         * @param path the requirejs path to the controller you want to load
         * @param moduleDependencies an optional requirejs path or array of paths to the module-require file that describes the dependencies needed for the controller
         * @returns {[string,*]}
         */
        this.lazyLoadController = function lazyLoad(path, moduleDependencies) {
            return ['$ocLazyLoad','$rootScope', function ($ocLazyLoad,$rootScope) {
                if (moduleDependencies != null && moduleDependencies != undefined) {

                    if (!_.isArray(path)) {
                        path = [path];
                    }
                    var dependencies = null;
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

                    _.each(dependencies,function(dependency) {

                        if (_.indexOf(dependency) < 0) {
                            path.unshift(dependency);
                        }

                    })

                }
                return $ocLazyLoad.load(path, {serie: true})
            }]
        }

        this.lazyLoad = function lazyLoad(moduleDependencies) {
            return ['$ocLazyLoad','$rootScope', function ($ocLazyLoad,$rootScope) {
                if (moduleDependencies != null && moduleDependencies != undefined) {

                    var dependencies = null;
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
    return new LazyLoadUtil();

});
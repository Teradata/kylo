define(['angular','common/module-name'], function (angular,moduleName) {
    'use strict';

    angular.module(moduleName)
        .run(['$templateCache', function ($templateCache) {
            $templateCache.put('accordion-menu.tmpl.html',
                ' <md-list class="side-menu "> \n' +
                ' <md-list-item ng-repeat="section in vm.menu" class="parent-list-item" \n' +
            ' ng-class="{\'parentActive\' : vm.isOpen(section)}"> \n' +
                                      '  <h2 class="menu-heading" ng-if="section.type === \'heading\'" \n' +
            ' id="heading_{{ section.name }}"> \n' +
            '       {{section.text}}\n' +
            ' </h2>\n' +
            ' <menu-link section="section" ng-if="section.type === \'link\'" style="width:100%"></menu-link>\n' +
            ' <menu-toggle section="section" ng-if="section.type === \'toggle\'" style="width:100%"></menu-toggle>\n' +
                '<md-divider></md-divider>' +
            ' </md-list-item>\n'
                + '</md-list>' +

                                                                                      '');
        }])
        .directive('accordionMenu', ['$location',function ($location) {
            return {
                bindToController: {
                    menu: '=',
                    collapsed: '=',
                    onlyOne:'=?'
                },
                controllerAs: 'vm',
                templateUrl: 'accordion-menu.tmpl.html',
                link: function ($scope, $element) {

                },
                controller: function(){
                    var self = this;
                    self.openedSection = null;
                    self.autoFocusContent = false;
                    self.onlyOne = angular.isDefined(self.onlyOne) ? self.onlyOne : false;

                    self.openedSections = []

                    var setOpenSections = function(){
                        self.openedSections = _.filter(self.menu,function(item){
                            return item.type == 'toggle' && item.expanded == true;
                        });

                        _.each(self.openedSections,function(section){
                            section.expandIcon = 'expand_less';
                        })
                    }

                    setOpenSections();

                    self.focusSection = function () {
                        // set flag to be used later when
                        // $locationChangeSuccess calls openPage()
                        $scope.autoFocusContent = true;
                    };

                    self.isCollapsed = function() {
                        return self.collapsed == true;
                    }


                    self.toggleOpen = function (section) {
                        self.openedSection = (self.openedSection === section ? null : section);
                        var idx = _.indexOf(self.openedSections,section);
                        if(idx >=0){
                            self.openedSections.splice(idx,1);
                            section.expanded = false;
                        }
                        else {
                            self.openedSections.push(section);
                            section.expanded = true;
                        }
                    }
                    self.isOpen = function (section) {
                        return section.expanded && section.hidden == false && ( (self.onlyOne && self.openedSection === section )|| (!self.onlyOne && _.contains(self.openedSections,section)));
                    }
                    self.selectPage = function (section, page) {
                        page && page.url && $location.path(page.url);
                        self.currentSection = section;
                        self.currentPage = page;
                    }
                }
            };
        }])
});

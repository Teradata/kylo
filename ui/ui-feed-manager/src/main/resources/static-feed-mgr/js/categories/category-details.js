(function () {

    var controller = function($scope,$stateParams, $http,$mdToast,$mdDialog,CategoriesService,StateService){

        var self = this;

        this.sectionTitle = 'Add a Category'
        this.editModel = {};
        this.editable = false;
        this.model = {id:null,name:null,description:null,icon:null,iconColor:null,relatedFeedSummaries:[]};

        this.icons=['local_airport','local_dining','people','directions_run','traffic','format_paint','email','cloud'];

        if(CategoriesService.categories.length == []){
            CategoriesService.reload().then(function() {
                self.model = CategoriesService.findCategory($stateParams.categoryId);
            })
        }

        if($stateParams.categoryId != null) {
            if(CategoriesService.categories.length == []){
                CategoriesService.reload().then(function() {
                    self.model = CategoriesService.findCategory($stateParams.categoryId);
                    CategoriesService.getRelatedFeeds(self.model);
                })
            }else {
                this.model = CategoriesService.findCategory($stateParams.categoryId);
                CategoriesService.getRelatedFeeds(this.model);
            }
            this.editable = false;
            this.sectionTitle = 'Edit Category';
        }
        else {
            this.editable = true;
        }
        //fetch the related Feeds



        this.iconSearchText = '';
        this.iconSearchTextChanged = function(){

        }

        this.selectedItemChange = function(item) {
            if(item != null && item != undefined) {
                self.editModel.icon = item;
            }
            else {
                self.editModel.icon = null;
            }
        }

        function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(tag) {
                return (tag.toLowerCase().indexOf(lowercaseQuery) === 0);
            };
        }


        this.queryIconSearch = function(query) {
                var self = this;
                var results = query ? self.icons.filter( createFilterFor(query) ) : self.icons;
                 return results;
        }

        this.onEdit = function() {
            this.editModel = angular.copy(self.model);
        }


        this.canDelete = function(){
            if(this.model != null && this.model != undefined && this.model.relatedFeedSummaries) {
                return this.model.relatedFeedSummaries.length <= 0;
            }
            return false ;
        }
        this.onDelete = function() {
            var name = self.editModel.name;
           CategoriesService.delete(self.editModel).then(function() {
               CategoriesService.reload();

               $mdToast.show(
                   $mdToast.simple()
                       .textContent('Successfully deleted the category '+name)
                       .hideDelay(3000)
               );
               //redirect
               StateService.navigateToCategories();

           }, function(err){
               $mdDialog.show(
                   $mdDialog.alert()
                    //   .parent(angular.element(document.querySelector('#popupContainer')))
                       .clickOutsideToClose(true)
                       .title('Unable to delete the category')
                       .textContent('Unable to delete the category '+name+". "+err.data.message)
                       .ariaLabel('Unable to delete the category')
                       .ok('Got it!')
               );
            });
        }

        this.onCancel = function() {
            if(self.model.id == null){
                StateService.navigateToCategories();
            }
        }
        this.onSave = function() {

            CategoriesService.save(self.editModel).then(function () {
                CategoriesService.reload();
                self.model = self.editModel;
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Saved the Category')
                        .hideDelay(3000)
                );
            })

        }

        this.onFeedClick = function(feed) {
            StateService.navigateToFeedDetails(feed.id)

        }



        this.showIconPicker= function() {

            $mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/shared/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    iconModel:self.editModel
                }
            })
                .then(function(msg) {
                    if(msg) {
                        self.editModel.icon = msg.icon;
                        self.editModel.iconColor=msg.color;
                    }

                }, function() {

                });
        };

    };

    angular.module(MODULE_FEED_MGR).controller('CategoryDetailsController',controller);



}());

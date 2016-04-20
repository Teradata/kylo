
(function () {

    var controller = function($scope, $mdDialog, $mdToast, $http, StateService, field, policyParameter , FieldPolicyRuleOptionsFactory){
    $scope.field = field;
    $scope.options = FieldPolicyRuleOptionsFactory.getOptionsForType(policyParameter);

    //$scope.policyRules = field[policyParameter];
    var arr = field[policyParameter];

    if(arr != null && arr != undefined)
    {
        $scope.policyRules = angular.copy(arr);
    }
    var modeText = "Add";
    if($scope.policyRules != null && $scope.policyRules.length  && $scope.policyRules.length >0 ){
        modeText = "Edit";
    }

    $scope.title = modeText+" "+FieldPolicyRuleOptionsFactory.getTitleForType(policyParameter);


    $scope.pendingEdits = false;

    $scope.ruleType = null;
    $scope.editIndex;

    $scope.editRule;

    $scope.addText = 'ADD RULE';
    $scope.cancelText = 'CANCEL ADD';

    $scope.editMode = 'NEW';

    function _cancelEdit() {
        $scope.editMode='NEW';
        $scope.addText = 'ADD RULE';
        $scope.cancelText = 'CANCEL ADD';
        $scope.ruleType = null;
        $scope.editRule = null;
    }


    $scope.cancelEdit = function($event) {
        _cancelEdit();

    }

    $scope.onRuleTypeChange = function() {
        if ($scope.ruleType != null) {
            $scope.editRule = angular.copy($scope.ruleType );
        }
        else {
            $scope.editRule = null;
        }

    }



    $scope.addPolicy = function($event){

        if( $scope.policyRules == null) {
            $scope.policyRules = [];
        }

        if($scope.editMode == 'NEW') {
            $scope.policyRules.push($scope.editRule);
        }
        else if($scope.editMode == 'EDIT') {
            $scope.policyRules[$scope.editIndex] = $scope.editRule;
        }

        $scope.pendingEdits = true;
        _cancelEdit();
    }

    $scope.done = function($event) {
        field[policyParameter] = $scope.policyRules;
        $mdDialog.hide('done');
    }


    $scope.deletePolicyByIndex = function($index){
        if($scope.policyRules != null){
            $scope.policyRules.splice($index, 1);
        }
        $scope.pendingEdits = true;
        _cancelEdit();
    }
    $scope.editPolicy = function($event,$index,rule) {
        if(   $scope.editMode == 'EDIT') {
            _cancelEdit();
        }

        $scope.addText = 'SAVE EDIT';
        $scope.cancelText = 'CANCEL EDIT';
        $scope.editMode = 'EDIT'
        $scope.editIndex = $index;
        $scope.editRule = angular.copy(rule);
        var match = _.find($scope.options,function(option){
            return option.name = rule.name;
        })
        $scope.ruleType = angular.copy(match);
    }

    $scope.hide = function($event) {
        _cancelEdit();
        $mdDialog.hide();
    };

    $scope.cancel = function($event) {
        _cancelEdit();
        $mdDialog.hide();
    };


};

angular.module(MODULE_FEED_MGR).controller('FeedFieldPolicyRuleDialogController',controller);



}());


angular.module(MODULE_FEED_MGR).factory('FieldPolicyRuleOptionsFactory', function () {

    function getStandardizationOptions() {
        var options = [];
        options.push({name:"Date/Time",description:"Converts any date to ISO8601",properties:[{name:"Date Format",value:"",placeholder:"",type:"string",hint:'Format Example: MM/DD/YYYY'}]});
        options.push({name:"Default value",description:"Default Value if empty",properties:[{name:"Default value",value:"",placeholder:"",type:"string"}]});
        options.push({name:"Control characters",description:"Strips control characters",properties:[{name:"Control Characters",value:"",placeholder:"",type:"string",hint:'Comma separated list of characters'}]});
        options.push({name:"Mask credit card",description:"Preserves last 4 digits"});
        options.push({name:"Mask SSN",description:"Preserves last 4 digits"});
        options.push({name:"Regex replace",description:"Replace all matching",properties:[{name:"Regex",value:"",placeholder:"",type:"regex"}]});
        options.push({name:"Strip non-numeric",description:"Strips non numeric characters"});
        return options;
    }

    function getValidationOptions(){
        var options = [];

        options.push({name:"Schema",description:"Must convert to target schema type"});
        options.push({name:"Regular expression",description:"Must match user defined regex",properties:[{name:"Regex",value:"",placeholder:"",type:"regex"}]});
        options.push({name:"Lookup list",description:"Must be contained in list",properties:[{name:"List",value:"",placeholder:"",type:"string",hint:'Comma separated list of values'}]});
        options.push({name:"Empty check",description:"Cannot be null or empty"});
        options.push({name:"Credit card",description:"Valid credit card"});
        options.push({name:"Range",description:"Numeric must fall within range",properties:[{name:"Min",value:"",placeholder:"",type:"number",hint:'Minimum Value'},{name:"Max",value:"",placeholder:"",type:"number",hint:'Maximum Value'}]});
        options.push({name:"String length",description:"String cannot exceed length",properties:[{name:"Maximum length",value:"",placeholder:"",type:"number"}]});
        options.push({name:"Timestamp",description:"Valid ISO8601 timestamp"});
        options.push({name:"Email",description:"Valid email address"});
        options.push({name:"IP address",description:"Valid IP address"});
        options.push({name:"US phone",description:"Valid US phone",properties:[{name:"Phone format",value:"",placeholder:"",type:"string",hint:'(###) ###-####'}]});
        options.push({name:"US zip",description:"Valid US zip",properties:[{name:"Zip format",value:"",placeholder:"",type:"string",hint:'#####'}]});
        return options;

    }

    var data = {
        getTitleForType:function(type){
            if(type == 'standardization'){
                return "Standardization Policies";
            }
            else if(type =='validation'){
                return 'Validation Policies';
            }

        },
        getOptionsForType:function(type) {
            if(type == 'standardization'){
                return getStandardizationOptions();
            }
            else if(type =='validation'){
                return getValidationOptions();
            }
        }
    };
    return data;



});
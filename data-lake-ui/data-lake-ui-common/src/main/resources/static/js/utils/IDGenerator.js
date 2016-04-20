
var IDGenerator = (function () {
    function IDGenerator() {
    }
    IDGenerator.generateId = function (prefix) {
       IDGenerator.idNumber++;
        if(prefix){
            return prefix+'_'+IDGenerator.idNumber;
        }
        else{
            return IDGenerator.idNumber;
        }
    };
    IDGenerator.idNumber = 0;

    return IDGenerator;
})();


import DataexportController from "./dataexport.controller";
import DataimportController from "./dataimport.controller";

export default angular.module('arachne.admin', [])
    .controller('DataexportController', ['$scope', '$http', '$timeout', 'arachneSettings', 'authService', 'messageService', DataexportController])
    .controller('DataimportController', ['$scope', '$http', '$interval', 'arachneSettings', DataimportController])
;

import 'angular';
import 'angular-ui-grid';
import 'angular-ui-grid/ui-grid.css';
import con10tTableWrapper from "./con10t-table-wrapper.directive.js";

export default angular.module('arachne.con10t-widgets.table', ['ui.grid'])
    .directive('con10tTableWrapper', ['$http', con10tTableWrapper]);

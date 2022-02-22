export default angular.module('arachne.visualizations.table', [])
.directive('con10tTable', function() {
    return {
        restrict: 'E',
        scope: {
            pathToData: '@',
            pathToTableTemplate: '@',
            rowsPerPage: '@',
        },
        link: function(scope) {
            import('./con10t-table-wrapper.directive.js')
                .then(mod => scope.$apply(() => scope.lazyLoadTable = mod.default)); 
        },
        template: `<div oc-lazy-load="lazyLoadTable">
            <con10t-table-wrapper
                path-to-data="{{pathToData}}"
                path-to-table-template="{{pathToTableTemplate}}"
                rows-per-page="{{rowsPerPage}}"
            />
        </div>`
    }
});

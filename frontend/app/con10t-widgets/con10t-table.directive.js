export default function(lazyLoad) {
    return {
        restrict: 'E',
        scope: {
            pathToData: '@',
            pathToTableTemplate: '@',
            rowsPerPage: '@',
        },
        link: function(scope) {
            lazyLoad(import('./table/con10t-table.module.js'))
                .then(mod => scope.$apply(scope => scope.lazyLoadTable = mod)); 
        },
        template: `<div oc-lazy-load="lazyLoadTable">
            <con10t-table-wrapper
                path-to-data="{{pathToData}}"
                path-to-table-template="{{pathToTableTemplate}}"
                rows-per-page="{{rowsPerPage}}"
            />
        </div>`
    }
};

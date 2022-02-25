export default function() {
    return {
        restrict: 'E',
        scope: {
            pathToData: '@',
            pathToTableTemplate: '@',
            rowsPerPage: '@',
        },
        link: function(scope) {
            import('./table/con10t-table.module.js')
                .then(mod => scope.$apply(scope => scope.lazyLoadTable = mod.default)); 
        },
        template: `<div oc-lazy-load="[lazyLoadTable]">
            <con10t-table-wrapper
                path-to-data="{{pathToData}}"
                path-to-table-template="{{pathToTableTemplate}}"
                rows-per-page="{{rowsPerPage}}"
            />
        </div>`
    }
};

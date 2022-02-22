import 'angular';
import 'angular-ui-grid';
import 'angular-ui-grid/ui-grid.css';

/**
 * @author: Sebastian Cuy
 */
export default angular.module('arachne.visualizations.table_lazy', ['ui.grid'])
.directive('con10tTableWrapper', ['$http', function($http) {
    return {
        restrict: 'E',
        scope: {
            pathToData: '@',
            rowsPerPage: '@',
            columnDefs: '=',
        },
        template: '<div ng-if="gridOptions" ui-grid="gridOptions"></div>',
        link: function(scope) {
            $http.get(scope.pathToData)
                .then(({ data }) => scope.gridOptions = buildGridOptions(data, scope.columnDefs, scope.rowsPerPage));
        }
    };
}]);

const buildGridOptions = (data, columnDefs, rowsPerPage) => ({
    data,
    columnDefs: columnDefs ?? buildColumnDefs(data[0]),
    rowTemplate,
    minRowsToShow: rowsPerPage ?? 10,
});

const rowTemplate = `
    <div ng-style="{ 'font-weight': row.entity.highlight_row && 'bold' }"
        ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.uid"
        ui-grid-one-bind-id-grid="rowRenderIndex + '-' + col.uid + '-cell'"
        class="ui-grid-cell"
        ng-class="{ 'ui-grid-row-header-cell': col.isRowHeader }"
        role="{{col.isRowHeader ? 'rowheader' : 'gridcell'}}"
        ui-grid-cell>
    </div>
`;

const buildColumnDefs = (row) => Object.keys(row).reduce((columnDefs, field) => buildColumnDef(field, columnDefs), []);

const buildColumnDef = (field, columnDefs) => {

    if (field === 'highlight_row') {
        // skip
    } else if (field.endsWith('_Ref') || field.endsWith('Ref')) {
        const existingIndex = columnDefs.findIndex(({ name }) => name === field.replace(/_?Ref$/, ''));
        if (existingIndex !== -1) {
            columnDefs[existingIndex].cellTemplate = buildCellTemplate(field);
        } else {
            console.error('No matching field found for ref field "%s"! Ref fields have to appear after the corresponding field.', field);
        }
    } else if (field.endsWith('_Sort') || field.endsWith('Sort')) {
        const existingIndex = columnDefs.findIndex(({ name }) => name === field.replace(/_?Sort$/, ''));
        if (existingIndex !== -1) {
            columnDefs[existingIndex].sortingAlgorithm = buildSortingAlgorithm(field);
        } else {
            console.error('No matching field found for sort field "%s"! Sort fields have to appear after the corresponding field.', field);
        }
    } else {
        columnDefs = columnDefs.concat({ field, name: field });
    }
    return columnDefs;
}

const buildCellTemplate = (refField) => {

    const mainField = refField.replace(/_?Ref$/, '');

    return `
        <div class="ui-grid-cell-contents">
            <a ng-if="row.entity.${refField}" href="{{row.entity.${refField}}}">
                {{row.entity.${mainField}}}
            </a>
            <span ng-if="!row.entity.${refField}">
                {{row.entity.${mainField}}}
            </span>
        </div>
    `;
}

const buildSortingAlgorithm = (field) => (_a, _b, rowA, rowB) =>
    (rowA.entity[field] == rowB.entity[field])
        ? 0
        : (rowA.entity[field] < rowB.entity[field])
            ? -1
            : 1;

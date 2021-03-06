/**
 * @author: Sebastian Cuy
 * @author: Jan G. Wieners
 */
export default function (authService) {
    return {
        restrict: 'E',
        scope: {
            datasetGroup: '@'
        },
        transclude: true,
        template: require('./con10t-show-if.html'),

        link: function(scope, element, attrs) {

            attrs.$observe('datasetGroup', function(value) {

                scope.showContent = (authService.getDatasetGroups().indexOf(value) !== -1);
            });
        }
    }
};

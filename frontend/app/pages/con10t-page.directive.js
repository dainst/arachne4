import './con10t-page.scss';

angular.module('arachne.widgets.directives')

.directive('con10tPage', function() {
return {
    restrict: 'A',
    link: function(scope, element, attrs) {
        attrs.$observe('con10tPage', function(value) {
            element.attr("href", "http://arachne.dainst.org/project/" + value);
        });
    }
}});

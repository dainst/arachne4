import con10tWidgetsModule from '../con10t-widgets/con10t-widgets.module.js';
import con10tPages from '../../con10t/content.json';

export default angular.module('arachne.project', [con10tWidgetsModule.name])

/**
 * Sets the templateUrl for a localized project page.
 * The version of the static page gets determined
 * by specification by the user via search param lang
 * or otherwise by an automatic language selection rule.
 *
 * $scope
 *   templateUrl
 *
 * @author: Sebastian Cuy
 * @author: Daniel M. de Oliveira
 * @author: Jan G. Wieners
 */

.controller('ProjectController', ['$scope', '$stateParams', '$location', 'localizedContent', '$templateCache',
    function ($scope, $stateParams, $location, localizedContent, $templateCache) {

        $scope.$on("$includeContentError", function (event, templateName) {
            console.error('Failed to include template: ' + templateName);
            $location.path('/404');
        });

        const lang = $location.search()['lang'] || localizedContent.determineLanguage(con10tPages, $stateParams.title);

        $scope.templateUrl = `con10t/${lang}/${$stateParams.title}.html`;

        // Ensure that images are loaded correctly
        $templateCache.remove($scope.templateUrl);
    }]);

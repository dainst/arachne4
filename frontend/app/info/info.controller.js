import infoPages from '../../info/content.json';

/**
 * Sets the templateUrl for a localized info page.
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
export default function ($scope, $stateParams, $location, localizedContent) {

    $scope.$on("$includeContentError", function (event, templateName) {
        console.error('Failed to include template: ' + templateName);
        $location.path('/404');
    });

    const lang = $location.search()['lang'] || localizedContent.determineLanguage(infoPages, $stateParams.title);

    $scope.templateUrl = `info/${lang}/${$stateParams.title}.html`;

};

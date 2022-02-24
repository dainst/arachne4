import './welcome-page.scss';
import frontProjects from '../con10t/front.json';

export default function ($rootScope, $scope, $http, arachneSettings, messages, $timeout) {

    $rootScope.tinyFooter = false;

    $scope.projects = frontProjects;

    var lang = navigator.language || navigator.userLanguage;

    if (lang === 'de' || lang === 'de-de') {
        $scope.lang = 'de';
    } else {
        $scope.lang = 'en';
    }

    $http.get(arachneSettings.dataserviceUri + "/entity/count")
        .then(function (result) {
            $scope.entityCount = result.data.entityCount;
        }).catch(function () {
        $timeout(function () {
            messages.add("backend_missing");
        }, 500);
    });
};

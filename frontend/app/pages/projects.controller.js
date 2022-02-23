import con10tPages from '../../con10t/content.json';
angular.module('arachne.controllers')


/**
 * $scope
 *   columns - an array of 3 elements which each
 *   represent a column of project items.
 *
 * @author: Daniel de Oliveira
 * @author: Sebastian Cuy
 */
    .controller('ProjectsController', ['$scope', '$http', 'localizedContent',
        function ($scope, $http, localizedContent) {

            $scope.columns = [];

            localizedContent.reduceTitles(con10tPages);
            $scope.sliceColumns(con10tPages.children);

            $scope.getProjectLink = function (project) {

                var projectLink = 'project/' + project.id;

                if (project.fallbackLanguage) {
                    projectLink += '?lang=' + project.fallbackLanguage;
                }

                return projectLink;
            };

            $scope.sliceColumns = function (projects) {

                $scope.columns[0] = projects.slice(0, 3);
                $scope.columns[1] = projects.slice(3, 5);
                $scope.columns[2] = projects.slice(5);
            };
        }
    ]);

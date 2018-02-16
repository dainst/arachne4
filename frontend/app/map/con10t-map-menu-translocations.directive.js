'use strict';

angular.module('arachne.widgets.map')

/**
 * @author: Richard Henck
 * @author: Philipp Franck
 * @author: Sebastian Cuy
 */
    .directive('con10tMapMenuTranslocations', ['placesPainter', 'heatmapPainter', 'mapService', 'searchService', 'placesService',
        function (placesPainter, heatmapPainter, mapService, searchService, placesService) {

            return {
                restrict: 'A',
                scope: {
                    type: '@',
                    searchScope: '@'
                },
                templateUrl: 'app/map/con10t-map-menu-translocations.html',
                link: function (scope) {
                    scope.isTranslocationViewShown = false;

                    scope.toggleTranslocationView = function() {
                        scope.isTranslocationViewShown = !scope.isTranslocationViewShown;
                        mapService.setTranslocationLayerActive(scope.isTranslocationViewShown);
                        searchService.getCurrentPage().then(function (entities) {
                            drawMapEntities(entities);
                        });
                    }

                    scope.$on('$destroy', function() {
                        placesPainter.clearTranslocationLines();
                    });

                    function drawMapEntities(entities) {
                        if (scope.isTranslocationViewShown) {
                            for (var i = 0; i < entities.length; i++) {
                                placesPainter.drawTranslocationLines(entities[i].places);
                            }
                        } else {
                            placesPainter.clearTranslocationLines();
                        }
                    }
                }
            }
        }
    ]);
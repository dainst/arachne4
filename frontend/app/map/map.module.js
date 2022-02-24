import arEntityMap from './ar-entity-map.directive.js';
import arMapMarkerPopup from './ar-map-marker-popup.directive.js';
import arMapNav from './ar-map-nav.directive.js';
import heatmapPainter from './heatmap-painter.js';
import MapMenuController from './map-menu.controller.js';
import mapService from './map.service.js';
import Place from './place.prototype.js';
import placesPainter from './places-painter.js';
import placesService from './places.service.js';
import SearchModule  from '../search/search.module.js';

import './map.scss';

export default angular.module('arachne.map', [SearchModule.name])
    .config(['$stateProvider', $stateProvider => {
        $stateProvider.state({ name: 'map', url: '/map?fl&q&fq&view&sort&offset&limit&desc&bbox&ghprec&lat&lng&baselayer', template: require('./map.html')});
    }])
    .directive('arEntityMap', ['$compile', 'Query', 'placesPainter', arEntityMap])
    .directive('arMapMarkerPopup', ['$location', 'Entity', 'searchScope', arMapMarkerPopup])
    .directive('con10tMapPopup', ['$location', 'Entity', 'searchScope', arMapMarkerPopup])
    .directive('arMapNav', ['searchService', 'authService', '$uibModal', '$location', 'mapService', arMapNav])
    .factory('heatmapPainter', heatmapPainter)
    .controller('MapMenuController', ['$scope', 'searchService', MapMenuController])
    .factory('mapService', [ 'searchService' , mapService])
    .factory('Place', Place)
    .factory('placesPainter', ['$compile', 'Place', '$rootScope', placesPainter])
    .factory('placesService', ['searchService', 'Place', placesService])
;

import arEntityMap from './ar-entity-map.directive.js';
import arMapMarkerPopup from './ar-map-marker-popup.directive.js';
import arMapNav from './ar-map-nav.directive.js';
import MapMenuController from './map-menu.controller.js';
import SearchModule  from '../search/search.module.js';
import arMapTranslocationsButton from './ar-map-translocations-button.directive.js';

import './map.scss';

export default angular.module('arachne.map', [SearchModule.name])
    .config(['$stateProvider', $stateProvider => {
        $stateProvider.state({ name: 'mapScoped', url: '/project/:title/map?fl&q&fq&view&sort&offset&limit&desc&bbox&ghprec&lat&lng&baselayer', template: require('./map.html'), data: { searchPage: 'map' }});
        $stateProvider.state({ name: 'map', url: '/map?fl&q&fq&view&sort&offset&limit&desc&bbox&ghprec&lat&lng&baselayer', template: require('./map.html')});
    }])
    .directive('arEntityMap', ['$compile', 'Query', 'placesPainter', arEntityMap])
    .directive('arMapMarkerPopup', ['$location', 'Entity', 'searchScope', arMapMarkerPopup])
    .directive('arMapNav', ['searchService', 'authService', '$uibModal', '$location', 'mapService', arMapNav])
    .directive('arMapTranslocationsButton', ['placesPainter', 'mapService', 'searchService', arMapTranslocationsButton])
    .controller('MapMenuController', ['$scope', 'searchService', MapMenuController])
;

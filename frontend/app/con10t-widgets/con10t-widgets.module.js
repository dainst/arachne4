import con10tCatalogTree from '../con10t-widgets/con10t-catalog-tree.directive.js';
import con10tItem from '../con10t-widgets/con10t-item.directive.js';
import con10tImage from '../con10t-widgets/con10t-image.directive.js';
import con10tMapMenuBaselayer from '../con10t-widgets/con10t-map-menu-baselayer.directive.js';
import con10tMapMenuFacetSearch from './con10t-map-menu-facet-search.directive.js';
import con10tMapMenuLegend from './con10t-map-menu-legend.directive.js';
import con10tMapMenuOverlays from './con10t-map-menu-overlays.directive.js';
import con10tMapMenuSearchField from './con10t-map-menu-search-field.directive.js';
import con10tMapMenuSearchInfo from './con10t-map-menu-search-info.directive.js';
import con10tMapOverlays from './con10t-map-overlays.directive.js';
import con10tMap from './con10t-map.directive.js';
import con10tPage from './con10t-page.directive.js';
import con10tToc from './con10t-toc.directive.js';
import con10tSearchCatalog from '../con10t-widgets/con10t-search-catalog.directive.js';
import con10tSearchQuery from './con10t-search-query.directive.js';
import con10tSearch from './con10t-search.directive.js';
import con10tInclude from './con10t-include.directive.js';
import con10tMediaTree from './con10t-media-tree.directive.js';
import con10tShowIf from './con10t-show-if.directive.js';
import con10tTree from './con10t-tree.directive.js';
import con10tTable from './con10t-table.directive.js';
import con10tNetwork from './con10t-network.directive.js';

import './con10t-widgets.scss';

export default angular.module('arachne.con10t-widgets', ['oc.lazyLoad'])
    .directive('con10tCatalogTree', ['Catalog', 'CatalogEntry', '$filter', con10tCatalogTree])
    .directive('con10tItem', con10tItem)
    .directive('con10tImage', con10tImage).directive('con10tMapMenuBaselayer', ['searchService', 'mapService', con10tMapMenuBaselayer])
    .directive('con10tMapMenuFacetSearch', ['$location', 'searchService', 'mapService', 'arachneSettings', con10tMapMenuFacetSearch])
    .directive('con10tMapMenuLegend', con10tMapMenuLegend)
    .directive('con10tMapMenuOverlays', ['searchService', 'mapService', con10tMapMenuOverlays])
    .directive('con10tMapMenuSearchField', ['$location', 'searchService', 'mapService', '$window', con10tMapMenuSearchField])
    .directive('con10tMapMenuSearchInfo', ['searchService', 'mapService', con10tMapMenuSearchInfo])
    .directive('con10tMapOverlays', ['mapService', 'searchService', con10tMapOverlays])
    .directive('con10tMap', ['searchService', 'mapService', 'heatmapPainter', 'placesService', 'placesPainter', 'arachneSettings', con10tMap])
    .directive('con10tPage', con10tPage)
    .directive('con10tToc', con10tToc)
    .directive('con10tSearchCatalog', con10tSearchCatalog)
    .directive('con10tSearchQuery', ['$location', con10tSearchQuery])
    .directive('con10tSearch', ['$location', '$filter', con10tSearch])
    .directive('con10tInclude', con10tInclude)
    .directive('con10tMediaTree', ['arachneSettings', con10tMediaTree])
    .directive('con10tShowIf', ['authService', con10tShowIf])
    .directive('con10tTree', ['Query', 'Entity', con10tTree])
    .directive('con10tTable', ['lazyLoad', con10tTable])
    .directive('con10tNetwork', ['lazyLoad', con10tNetwork])
;

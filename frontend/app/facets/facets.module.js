import arActiveFacets from './ar-active-facets.directive.js';
import arFacetBrowser from './ar-facet-browser.directive.js';

export default angular.module('arachne.facets', [])
    .directive('arActiveFacets', arActiveFacets)
    .directive('arFacetBrowser', ['Entity', arFacetBrowser])
;

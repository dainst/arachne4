import con10tNetworkChordDirective from "./con10t-network-chord.directive.js";
import con10tNetworkMapPopupDirective from "./con10t-network-map-popup.directive.js";
import con10tNetworkMapDirective from "./con10t-network-map.directive.js";
import con10tNetworkWrapperDirective from "./con10t-network-wrapper.directive.js";
import con10tPaginatedItemListDirective from "./con10t-paginated-item-list.directive.js";
import con10tTimeLineChartDirective from "./con10t-time-line-chart.directive.js";

export default angular.module('arachne.visualizations.network_lazy', [])
    .directive('con10tNetworkChord', con10tNetworkChordDirective)
    .directive('con10tNetworkMapPopup', con10tNetworkMapPopupDirective)
    .directive('con10tNetworkMap', ['$compile', 'transl8', con10tNetworkMapDirective])
    .directive('con10tNetworkWrapper', ['$http', '$q', '$filter', '$window', con10tNetworkWrapperDirective])
    .directive('con10tPaginatedItemList', con10tPaginatedItemListDirective)
    .directive('con10tTimeLineChart', con10tTimeLineChartDirective);

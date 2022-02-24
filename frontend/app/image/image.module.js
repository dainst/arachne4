import arImagegridCell from './ar-imagegrid-cell.directive.js';
import arImagegrid from './ar-imagegrid.directive.js';
import arImageslider from './ar-imageslider.directive.js';
import arImg from './ar-img.directive.js';
import cellsFromImages from './cells-from-images.filter.js';
import zoomifyimg from './zoomifyimg.directive.js';

export default angular.module('arachne.image', [])
    .directive('arImagegridCell', arImagegridCell)
    .directive('arImagegrid', ['$http', '$window', 'searchScope', arImagegrid])
    .directive('arImageslider', arImageslider)
    .directive('arImg', ['arachneSettings', '$http', arImg])
    .filter('cellsFromImages', ['arachneSettings', cellsFromImages])
    .directive('zoomifyimg', ['arachneSettings', '$http', zoomifyimg])
;

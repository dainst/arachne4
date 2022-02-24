import autofillFix from './autofillfix.directive.js';
import convertToBool from './convert-to-bool.directive.js';
import base64Filter from './filters/base64.filter.js';
import decapitalizeFilter from './filters/decapitalize.filter.js';
import escapeSlashesFilter from './filters/escape-slashes.filter.js';
import md5Filter from './filters/md5.filter.js';
import nl2brFilter from './filters/nl2br.filter.js';
import rangeFilter from './filters/range.filter.js';
import focusMe from './focus-me.directive.js';
import stripCoordsFilter from './filters/strip-coords.filter.js';
import tsvDataFilter from './filters/tsvData.filter.js';

export default angular.module('arachne.utils', [])
    .directive('autofillfix', ['$timeout', autofillFix])
    .directive('convertToBool', convertToBool)
    .directive('focusMe', ['$timeout', focusMe])
    .filter('base64', base64Filter)
    .filter('decapitalize', decapitalizeFilter)
    .filter('escapeSlashes', escapeSlashesFilter)
    .filter('md5', md5Filter)
    .filter('nl2br', ['$sce', nl2brFilter])
    .filter('range', rangeFilter)
    .filter('stripCoords', stripCoordsFilter)
    .filter('tsvData', tsvDataFilter)
;

import angular from 'angular';
import '@uirouter/angularjs';
import 'angular-resource';
import 'angular-cookies';
import 'angular-sanitize';
import 'oclazyload';

import './lib/ui-bootstrap-custom-build/ui-bootstrap-custom-2.5.0.js';
import './lib/ui-bootstrap-custom-build/ui-bootstrap-custom-tpls-2.5.0.js';
import './lib/ui-bootstrap-custom-build/ui-bootstrap-custom-2.5.0-csp.css';

import transl8_en from './_transl8.en.js';
import transl8_de from './_transl8.de.js';

import 'idai-components';
import '../lib/relative-paths-in-partial.js';

require.context('../con10t/frontimages', false, /^\.\/.*\.(png|jpg|gif|svg|webp)$/);
require.context('../img/', true, /^\.\/.*\.(png|jpg|gif|svg|webp)$/);

import './_modules.js';
import './menu.controller.js';
import './admin/dataexport.controller.js';
import './admin/dataimport.controller.js';
import adminModule from './admin/admin.module.js';
import infoModule from './info/info.module.js';
import projectModule from './project/project.module.js';
import con10tWidgetsModule from './con10t-widgets/con10t-widgets.module.js';
import utilsModule from './utils/utils.module.js';

import './app.scss';

import Catalog from './catalog/catalog.resource.js';
import CatalogEntry from './catalog/catalog-entry.resource.js';
import categoryService from './category/category.service.js';
import Entity from './entity/entity.resource.js';
import Query from './search/query.prototype.js';
import searchService from './search/search.service.js';
import authService from './users/auth.service.js';
import scopeModule from './scope/scope.module.js';
import welcomePageController from './welcome-page.controller.js';
import mapService from './map/map.service.js';
import heatmapPainter from './map/heatmap-painter.js';
import placesService from './map/places.service.js';
import placesPainter from './map/places-painter.js';

const lazyLoad = (importPromise) => ($transition$) => {
    const $ocLazyLoad = $transition$.injector().get('$ocLazyLoad');
    return importPromise.then(mod => $ocLazyLoad.load(mod.default));
}

const lazyLoadService = ($ocLazyLoad) => (importPromise) =>
    importPromise.then(mod => {
        $ocLazyLoad.load(mod.default);
        return mod.default.name;
    });

angular.module('arachne', [
    'ui.bootstrap',
    'ui.bootstrap.tpls',
    'ui.router',
    'oc.lazyLoad',
    'ngResource',
    'ngCookies',
    'ngSanitize',
    'relativePathsInPartial',
    'idai.templates',
    'idai.components',
    'arachne.filters',
    'arachne.resources',
    'arachne.services',
    'arachne.directives',
    'arachne.controllers',
    'arachne.widgets.directives',
    'arachne.widgets.map',
    adminModule.name,
    infoModule.name,
    projectModule.name,
    con10tWidgetsModule.name,
    utilsModule.name,
    scopeModule.name
])
.controller('WelcomePageController', ['$rootScope', '$scope', '$http', 'arachneSettings', 'messageService', '$timeout', welcomePageController])
.factory('Catalog', ['$resource', 'arachneSettings', Catalog])
.factory('CatalogEntry', ['$resource', 'arachneSettings', CatalogEntry])
.factory('categoryService', ['$filter', '$q', 'transl8', categoryService])
.factory('Entity', ['$resource', 'arachneSettings', '$q', Entity])
.factory('searchService', ['$location', 'Entity', 'Query', '$q', 'searchScope', searchService])
.factory('Query', ['arachneSettings', Query])
.factory('authService', ['$http', 'arachneSettings', '$filter', '$cookies', authService])
.factory('lazyLoad', ['$ocLazyLoad', lazyLoadService])
.factory('mapService', ['searchService', mapService])
.factory('heatmapPainter', [heatmapPainter])
.factory('placesService', [placesService])
.factory('placesPainter', [placesPainter])
.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', '$compileProvider', '$resourceProvider', '$qProvider', '$httpProvider',
    function($stateProvider, $urlRouterProvider, $locationProvider, $compileProvider, $resourceProvider, $qProvider, $httpProvider) {

        // eliminate "Possibly unhandled rejection" errors with Angular 1.5.9
        $qProvider.errorOnUnhandledRejections(false);

        $locationProvider.html5Mode(true);

        //$qProvider.errorOnUnhandledRejections(false);

        $compileProvider.imgSrcSanitizationWhitelist(/^\s*(https?|blob):/);

        $resourceProvider.defaults.cancellable = true;

        $urlRouterProvider.when('', '/');
        $urlRouterProvider.otherwise('/404');

        $httpProvider.useApplyAsync(true);


        /**
         * we want to realize scope-prefixed urls like project/whatever/search as well as /search
         * and don't want to define all of them double.
         * since both shall lead to search, not to project in the first case, they are not children in the
         * sense of ui-router. Also ui-router does not support optional parameters in the urls yet,
         * and the regex-support in the urls is to limited to realize it like this.
         * that's why we use a little nice function for that
         */


        var states = {
            '404': { url: '/404', template: require('./404.html'), data: { pageTitle: 'Arachne | 404' } },
            'welcome': { url: '/', template: require('./welcome-page.html')},
            'catalogs.**': { url: '/catalogs', lazyLoad: lazyLoad(import('./catalog/catalog.module.js'))},
            'catalog.**': { url: '/catalog', lazyLoad: lazyLoad(import('./catalog/catalog.module.js'))},
            'books.**': { url: '/books', lazyLoad: lazyLoad(import('./entity/entity.module.js')), reloadOnSearch: false },
            'entity.**': { url: '/entity', lazyLoad: lazyLoad(import('./entity/entity.module.js')), reloadOnSearch: false },
            'search.**': { url: '/search', lazyLoad: lazyLoad(import('./search/search.module.js'))},
            'categories.**': { url: '/categories', lazyLoad: lazyLoad(import('./category/category.module.js'))},
            'category.**': { url: '/category', lazyLoad: lazyLoad(import('./category/category.module.js')) },
            'map.**': { url: '/map', lazyLoad: lazyLoad(import('./map/map.module.js')), data: { searchPage: 'map' } },
            '3d.**': { url: '/3d', lazyLoad: lazyLoad(import('./3d/3d.module.js'))},
            'svg.**': { url: '/svg', lazyLoad: lazyLoad(import('./svg/svg.module.js'))},
            'register.**': { url: '/register', lazyLoad: lazyLoad(import('./users/users.module.js'))},
            'editUser.**': { url: '/editUser', lazyLoad: lazyLoad(import('./users/users.module.js'))},
            'contact.**': { url: '/contact', lazyLoad: lazyLoad(import('./users/users.module.js'))},
            'pwdreset.**': { url: '/pwdreset', lazyLoad: lazyLoad(import('./users/users.module.js'))},
			'pwdchange.**': { url: '/pwdchange', lazyLoad: lazyLoad(import('./users/users.module.js'))},
			'userActivation.**': { url: '/user/activation/:token', lazyLoad: lazyLoad(import('./users/users.module.js'))},
            'login.**': { url: '/login?redirectTo',lazyLoad: lazyLoad(import('./users/users.module.js'))},
            'dataimport': { url: '/admin/dataimport', template: require('./admin/dataimport.html')},
            'dataexport': { url: '/admin/dataexport', template: require('./admin/dataexport.html')},
            'project': { url: '/project/:title', template: require('./project/project.html')},
            'info': { url: '/info/:title?id', template: require('./info/info.html')},
        };

        var scoped = {'project': ['search.**', 'map.**', 'entity.**']};

        function registerState(state, name) {
            $stateProvider.state(name, angular.copy(state));
            angular.forEach(scoped[name] || [], function(child) {
                var newState = angular.copy(states[child]);
                newState.url = state.url + newState.url;

                if ('data' in newState) {
                    newState.data.scoped = true;
                } else {
                    newState.data = {scoped: true};
                }
                registerState(newState, name + '-' + child);
            });
        }

        angular.forEach(states, registerState);

    }
])
/**
 * Change <title> after page change
 */
.run(['$transitions', 'searchScope', function($transitions, searchScope) {

    $transitions.onSuccess({}, function(trans) {

        var toState = trans.to().$$state();
        var toParams = trans.params();

        document.title = (typeof toParams.title !== "undefined") ? searchScope.getScopeTitle(toParams.title) + ' |\u00A0' : '';
        document.title += toState.data?.pageTitle || '';

        if (document.title === '') {
            document.title = 'iDAI.objects / Arachne';
        }

        searchScope.refresh(); // refresh scopeObject for navbarSearch
    });


}])
.constant('transl8map', { en: transl8_en, de: transl8_de })
.constant('arachneSettings', {
    arachneUrl: 'https://arachne.dainst.org',
    dataserviceUri: "//" + document.location.host + "/data",
    limit: 50,
    facetLimit: 20,
    openFacets : ["facet_kategorie", "facet_image", "facet_bestandsname", "facet_subkategoriebestand"], // order is important for sorting of default facets
    sortableFields : ["entityId", "title", "subtitle"],
    maxSearchSizeForCatalog: 10000,
    batchSizeForCatalog: 100,
})
.constant('componentsSettings', {
    transl8Uri: 'https://arachne.dainst.org/transl8/translation/jsonp?application=arachne4_frontend&application=shared&lang={LANG}',
    searchUri: 'https://arachne.dainst.org/data/suggest?q=',
    dataProtectionPolicyUri: 'http://www.dainst.org/datenschutz',
    mailTo: 'idai.objects@dainst.org',
});

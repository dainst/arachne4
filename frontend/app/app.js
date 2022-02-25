import angular from 'angular';
import '@uirouter/angularjs';
import 'angular-resource';
import 'angular-cookies';
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
import infoModule from './info/info.module.js';
import projectModule from './project/project.module.js';
import con10tWidgetsModule from './con10t-widgets/con10t-widgets.module.js';
import utilsModule from './utils/utils.module.js';
import con10tNetworkModule from './visualizations/network/con10t-network.directive.js';
import con10tTableModule from './visualizations/table/con10t-table.directive.js';

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

const lazyLoad = (importPromise) => ($transition$) => {
    const $ocLazyLoad = $transition$.injector().get('$ocLazyLoad');
    return importPromise.then(mod => $ocLazyLoad.load(mod.default));
}

const lazyLoadService = ($ocLazyLoad) => (importPromise) =>
    importPromise.then(mod => $ocLazyLoad.load(mod.default));

angular.module('arachne', [
    'ui.bootstrap',
    'ui.bootstrap.tpls',
    'ui.router',
    'oc.lazyLoad',
    'ngResource',
    'ngCookies',
    'relativePathsInPartial',
    'idai.templates',
    'idai.components',
    'arachne.filters',
    'arachne.resources',
    'arachne.services',
    'arachne.directives',
    'arachne.controllers',
    'arachne.widgets.directives',
    infoModule.name,
    projectModule.name,
    con10tWidgetsModule.name,
    utilsModule.name,
    con10tNetworkModule.name,
    con10tTableModule.name,
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

        var title = 'iDAI.objects / Arachne';

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
            'welcome': { url: '/', template: require('./welcome-page.html'), data: { pageTitle: title } },
            'catalogs.**': { url: '/catalogs', lazyLoad: lazyLoad(import('./catalog/catalog.module.js')), data: { pageTitle: title } },
            'catalog.**': { url: '/catalog', lazyLoad: lazyLoad(import('./catalog/catalog.module.js')), data: { pageTitle: title } },
            'books.**': { url: '/books', lazyLoad: lazyLoad(import('./entity/entity.module.js')), reloadOnSearch: false, data: { pageTitle: title } },
            'entity.**': { url: '/entity', lazyLoad: lazyLoad(import('./entity/entity.module.js')), reloadOnSearch: false, data: { pageTitle: title } },
            'search.**': { url: '/search', lazyLoad: lazyLoad(import('./search/search.module.js')), data: { pageTitle: title } },
            'categories.**': { url: '/categories', lazyLoad: lazyLoad(import('./category/category.module.js')), data: { pageTitle: title } },
            'category.**': { url: '/category', lazyLoad: lazyLoad(import('./category/category.module.js')), data: { pageTitle: title } },
            'map.**': { url: '/map', lazyLoad: lazyLoad(import('./map/map.module.js')), data: { pageTitle: title, searchPage: 'map' } },
            '3d.**': { url: '/3d', lazyLoad: lazyLoad(import('./3d/3d.module.js')), data: { pageTitle: title } },
            'svg.**': { url: '/svg', lazyLoad: lazyLoad(import('./svg/svg.module.js')), data: { pageTitle: title } },
            'register.**': { url: '/register', lazyLoad: lazyLoad(import('./users/users.module.js')), data: { pageTitle: title }},
            'editUser.**': { url: '/editUser', lazyLoad: lazyLoad(import('./users/users.module.js')), data: { pageTitle: title }},
            'contact.**': { url: '/contact', lazyLoad: lazyLoad(import('./users/users.module.js')), data: { pageTitle: title }},
            'pwdreset.**': { url: '/pwdreset', lazyLoad: lazyLoad(import('./users/users.module.js')), data: { pageTitle: title }},
			'pwdchange.**': { url: '/pwdchange', lazyLoad: lazyLoad(import('./users/users.module.js')), data: { pageTitle: title }},
			'userActivation.**': { url: '/user/activation/:token', lazyLoad: lazyLoad(import('./users/users.module.js')), data: { pageTitle: title }},
            'login.**': { url: '/login?redirectTo',lazyLoad: lazyLoad(import('./users/users.module.js')), data: { pageTitle: title }},
            'dataimport': { url: '/admin/dataimport', template: require('./admin/dataimport.html'), data: { pageTitle: title }},
            'dataexport': { url: '/admin/dataexport', template: require('./admin/dataexport.html'), data: { pageTitle: title }},
            'project': { url: '/project/:title', template: require('./project/project.html'), data: { pageTitle: title } },
            'index.**': { url: '/index', lazyLoad: lazyLoad('./index/indes.module.js'), data: { pageTitle: title } },
            'info': { url: '/info/:title?id', template: require('./info/info.html'), data: { pageTitle: title } },
        };

        var scoped = {'project': ['search.**', 'map.**', 'entity.**']};

        function registerState(state, name) {
            $stateProvider.state(name, angular.copy(state));
            angular.forEach(scoped[name] || [], function(child) {
                var newState = angular.copy(states[child]);
                newState.url = state.url + newState.url;
                newState.data.scoped = true;
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

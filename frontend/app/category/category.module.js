import CategoriesController from './categories.controller.js';
import CategoryController from './category.controller.js';
import imageModule from '../image/image.module.js';
import facetsModule from '../facets/facets.module.js';

export default angular.module('arachne.category', [imageModule.name, facetsModule.name])
    .config(['$stateProvider', $stateProvider => {
        $stateProvider.state({ name: 'categories', url: '/categories', template: require('./categories.html')});
        $stateProvider.state({ name: 'category',  url: '/category/?c&facet&fv&group', template: require('./category.html')});
    }])
    .controller('CategoriesController', ['$rootScope', '$scope', '$filter', 'categoryService', CategoriesController])
    .controller('CategoryController', ['$rootScope', '$scope', 'Query', 'categoryService', '$location', 'Entity', 'indexService', CategoryController])
;

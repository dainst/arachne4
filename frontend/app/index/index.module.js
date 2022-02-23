import IndexController from './index.controller.js';
import indexService from './index.service.js';

export default angular.module('arachne.index', [])
    .config(['$stateProvider', $stateProvider => {
        $stateProvider.state({ name: 'index', url: '/index?c&fq&fv&group', template: require('./index.html') });
    }])
    .controller('IndexController', ['$scope', 'categoryService', 'Entity', 'Query', '$stateParams', '$location', 'indexService', IndexController])
    .factory('indexService', ['$filter', 'Entity', '$http', 'Query', '$q', indexService])
;

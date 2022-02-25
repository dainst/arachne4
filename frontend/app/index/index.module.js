import indexService from './index.service.js';

export default angular.module('arachne.index', [])
    .factory('indexService', ['$filter', 'Entity', '$http', 'Query', '$q', indexService])
;

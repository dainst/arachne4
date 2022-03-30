import arCatalogOccurrences from './ar-catalog-occurrences.directive.js';
import ManageEditorController from './catalog-manage-editor.controller.js';
import CatalogController from './catalog.controller.js';
import CatalogsController from './catalogs.controller.js';
import DeleteCatalogController from './delete-catalog.js';
import EditCatalogEntryController from './edit-catalog-entry.controller.js';
import EditCatalogHelpController from './edit-catalog-help.controller.js';
import EditCatalogController from './edit-catalog.controller.js';
import EditEntryController from './edit-entry.controller.js';
import ImageModule  from '../image/image.module.js';
import MarkdownModule from '../markdown/markdown.module.js';
import NgShowdownModule from 'ng-showdown';
import 'angular-ui-tree';

import './catalog.scss';

export default angular.module('arachne.catalog', [ImageModule.name, MarkdownModule.name, NgShowdownModule.name, 'ui.tree'])
    .config(['$stateProvider', $stateProvider => {
        $stateProvider.state({ name: 'catalogs', url: '/catalogs', template: require('./catalogs.html')});
        $stateProvider.state({ name: 'catalog', url: '/catalog/:id', template: require('./catalog.html')});
        $stateProvider.state({ name: 'catalogEntry', url: '/catalog/:id/:entryId', template: require('./catalog.html')});
    }])
    .directive('arCatalogOccurrences', ['arachneSettings', '$http', '$uibModal', 'Catalog', 'CatalogEntry', arCatalogOccurrences])
    .controller('ManageEditorController', ['$scope', '$http', 'arachneSettings', 'messageService', '$uibModalInstance', 'catalog', ManageEditorController])
    .controller('CatalogController', ['$rootScope', '$scope', '$state', '$stateParams', '$uibModal', '$window', '$timeout',
        'Catalog', 'CatalogEntry', 'authService', '$http', 'arachneSettings', 'Entity', '$location', 'messageService', CatalogController])
    .controller('CatalogsController',['$scope', '$uibModal', '$location',
        'authService', 'Entity', 'Catalog', 'CatalogEntry', '$http', 'arachneSettings', 'messageService', CatalogsController])
    .controller('DeleteCatalogController', ['$scope', DeleteCatalogController])
    .controller('EditCatalogEntryController', ['$scope', 'Entity', 'entry', EditCatalogEntryController])
    .controller('EditCatalogHelpController', ['$scope', EditCatalogHelpController])
    .controller('EditCatalogController', ['$scope', 'catalog', EditCatalogController])
    .controller('EditEntryController', ['$scope', EditEntryController])
;

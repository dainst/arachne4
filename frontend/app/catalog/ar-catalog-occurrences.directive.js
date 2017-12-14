'use strict';

angular.module('arachne.directives')

    .directive('arCatalogOccurrences', ['arachneSettings', '$http', '$uibModal', 'Catalog', 'CatalogEntry',
        function (arachneSettings, $http, $uibModal, Catalog, CatalogEntry) {
            return {
                scope: {
                    entity: '='
                },
                templateUrl: 'app/catalog/ar-catalog-occurrences.html',
                link: function (scope, element, attrs) {

                    scope.catalogEntries = [];
                    scope.projectEntries = [];
                    scope.catalogEntrySets = [];

                    scope.previewCatalogEntry = function (catalog, catalogEntry) {
                        var entityPreview = {
                            title: scope.entity.title,
                            subtitle: scope.entity.subtitle,
                            thumbnailId: scope.entity.thumbnailId
                        };
                        var preview = $uibModal.open({
                            templateUrl: 'app/catalog/preview-catalog-entry.html',
                            controller: ['$scope', function ($scope) {
                                $scope.catalog = catalog;
                                $scope.catalogEntry = catalogEntry;
                                $scope.entity = entityPreview;
                            }]
                        });
                    };

                    scope.createEntry = function () {
                        //TODO: Parse Sections in entry.text
                        var createEntryPos = $uibModal.open({
                            templateUrl: 'app/catalog/create-entry-pos.html',
                            controller: ['$scope', function ($scope) {
                                $scope.catalogs = Catalog.query()
                            }]
                        });
                        createEntryPos.close = function (catalog) {
                            var entry = {
                                catalogId: catalog.id,
                                parentId: catalog.root.id,
                                arachneEntityId: scope.entity.entityId,
                                label: scope.entity.title
                            };
                            var editEntryModal = $uibModal.open({
                                templateUrl: 'app/catalog/edit-entry.html',
                                controller: 'EditCatalogEntryController',
                                resolve: {
                                    entry: function () {
                                        return entry
                                    }
                                }
                            });
                            editEntryModal.close = function (newEntry) {
                                CatalogEntry.save(newEntry, function (data) {
                                    if (data.error_message) {
                                        console.log(data.error_message);
                                    } else {
                                        scope.loadOccurences();
                                    }
                                });
                                editEntryModal.dismiss();
                            };
                            createEntryPos.dismiss();
                        }
                    };

                    scope.loadOccurences = function () {
                        if (scope.entity) {

                            var url = arachneSettings.dataserviceUri + "/catalog/list/" + scope.entity.entityId;

                            $http.get(url).then(function (result) {
                                scope.catalogEntries = result.data;

                                var i = 0, len = scope.catalogEntries.length;

                                for (i; i < len; i++) {

                                    if (scope.catalogEntries[i].projectId) {
                                        scope.projectEntries.push(scope.catalogEntries[i]);
                                    }
                                }

                                scope.arrangeEntries();

                            }).catch(function (result) {
                                console.log("Error Arachne dataservice not reachable");
                            });
                        }
                    };

                    scope.arrangeEntries = function() {
                        var i, j, n, len = scope.catalogEntries.length, curi, curj, curk, duplicateAlreadyAdded, newCatalog;

                        for(i = 0; i < len; i++) {
                            curi = scope.catalogEntries[i];
                            if(curi.duplicate)
                                continue;
                            duplicateAlreadyAdded = false;
                            for(j = i; j < len; j++) {
                                curj = scope.catalogEntries[j];
                                if(curi.entry.catalogId === curj.entry.catalogId) {
                                    if(!duplicateAlreadyAdded) {
                                        scope.catalogEntrySets.push(scope.getNewCatalogEntrySet(curi));
                                        duplicateAlreadyAdded = true;
                                    }
                                    else
                                        for(n = 0; n < scope.catalogEntrySets.length; n++) {
                                            curk = scope.catalogEntrySets[n];
                                            if(curk.catalogId === curj.entry.catalogId)
                                                scope.catalogEntrySets[n].entries.push(curj.entry);
                                        }
                                    curi.duplicate = curj.duplicate = true;
                                }
                            }
                            if(!curi.duplicate) {
                                scope.catalogEntrySets.push(scope.getNewCatalogEntrySet(curi));
                            }
                        }
                    };

                    scope.getNewCatalogEntrySet = function (catalogEntry) {
                        var newCatalogEntrySet = {};
                        newCatalogEntrySet.catalogAuthor = catalogEntry.catalogAuthor;
                        newCatalogEntrySet.catalogTitle = catalogEntry.catalogTitle;
                        newCatalogEntrySet.catalogId = catalogEntry.entry.catalogId;
                        newCatalogEntrySet.entries = [];
                        newCatalogEntrySet.entries.push(catalogEntry.entry);
                        newCatalogEntrySet.projectId = catalogEntry.projectId;
                        return newCatalogEntrySet;
                    };

                    scope.$watch('entity', function () {
                        scope.loadOccurences();
                    });
                }
            }
        }
    ]);
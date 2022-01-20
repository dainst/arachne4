'use strict';

angular.module('arachne.controllers')

    .controller('MenuController', ['$scope', '$uibModal', 'authService', '$location', '$window', 'searchScope', 'lazyLoad',
        function ($scope, $uibModal, authService, $location, $window, searchScope, lazyLoad) {

            $scope.user = authService.getUser();

            $scope.currentPath = $location.path();
            $scope.$on("$locationChangeSuccess", function () {
                $scope.currentPath = $location.path();
            });

            $scope.openLoginModal = async function () {
                await lazyLoad(import('./users/users.module.js'));
                var modalInstance = $uibModal.open({
                    template: require('./users/login-form.html'),
                    controller: 'LoginFormController'
                });
                modalInstance.result.then(function (user) {
                    $window.location.reload();
                });
            };

            $scope.logout = function () {
                authService.clearCredentials();
                $scope.user = authService.getUser();
                $window.location = '/';
            }

            // search scoping
			$scope.searchScope = searchScope.currentScopeName;
			$scope.getScopePath = searchScope.currentScopePath;
			$scope.searchScopeObject = searchScope.scopeSettings;

        }]);

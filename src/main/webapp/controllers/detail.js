'use strict';

angular.module('book')
    .controller('DetailCtrl', function ($scope, $route, $routeParams, book) {

        $scope.load = function() {
            book.get($routeParams.id, function (item) {
                $scope.form = item.data;
            });
        }

        $scope.goback = function() {
            window.history.back();
        }

        $scope.load();
    });

app.controller("indexController", function ($scope, userService) {

    $scope.showUsername = function () {
        userService.showUsername().success(function (response) {
            $scope.username = JSON.parse(response);
            //因为传过来的用户名是一个字符串,所以前后有双引号
            //JSON.parse去掉用户名上的双引号
        })
    }
})
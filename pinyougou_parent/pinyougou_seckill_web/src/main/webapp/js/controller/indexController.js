app.controller('indexController',function ($scope,contentService) {

    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.bannerList=response;
        })
    }
})
app.controller('indexController', function ($scope, contentService) {

    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.bannerList = response;
        })
    }
    $scope.keywords="三星";
    $scope.search=function () {
        if($scope.keywords==''){
            $scope.keywords="三星";
        }
        //在searh项目需要用angluarJS来获取对应的keywords,#为angluarJS的写法,方便获取
        location.href="http://search.pinyougou.com#?keywords="+$scope.keywords;

    }
})
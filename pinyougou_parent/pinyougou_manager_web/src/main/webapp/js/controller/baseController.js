app.controller("baseController",function ($scope) {
    //angular.js内置分页工具,初始化参数设置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,  //后台获取
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//页面刷新
        }
    };
    //初始化查询对象，否则一加载页面时报错
    $scope.searchEntity = {};

    //定义一个空的数组.用来存储已勾选的要删除的复选框的id
    $scope.selectIds = [];

    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {     //如果选中
            $scope.selectIds.push(id);  //push 向数组中放数据
        } else {
            var index = $scope.selectIds.indexOf(id);   //数据在数组中的位置
            $scope.selectIds.splice(index, 1);          //splice(移除数据在数组中的位置,数量) 从数组中移除数据
        }
    }

    //页面刷新,则重新加载,获取分页数据
    $scope.reloadList = function () {
        //分页获取数据  当前页码  每页显示的条数
        // PageHelper.startPage(pageNum,pageSize)
        // findPage/1/10	Rest风格的请求
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
})
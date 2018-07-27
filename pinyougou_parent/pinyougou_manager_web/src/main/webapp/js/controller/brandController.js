//品牌控制层
app.controller('brandController', function ($scope,$controller, brandService) {        //angularJS的依赖注入 brandService

    //继承baseController  其本质是共用一个$scope作用域
    $controller("baseController",{$scope:$scope})   //单引号和双引号没有区别

    /*$scope.findPage = function (pageNum, pageSize) {
        brandService.findPage(pageNum, pageSize).success(function (response) {
            // response={total:100,rows:[{},{},{}]}  总条数  当前页数据
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total; //从后台获取的总条数赋值到分页对象中
        })
    }*/
    //搜索完后的分页
    $scope.search = function (pageNum, pageSize) {
        brandService.search(pageNum, pageSize, $scope.searchEntity).success(function (response) {
            // response={total:100,rows:[{},{},{}]}  总条数,当前页数据
            //从后台获取的总条数赋值到分页对象中
            $scope.paginationConf.totalItems = response.total;
            //从后台获取的当前页数据赋值到list中
            $scope.list = response.rows;
        })
    }

    /* //初始化findAll显示
     $scope.findAll = function () {
         brandService.findAll().success(function (response) {
             $scope.list = response;
         })
     }*/
    //新建与修改
    $scope.save = function () {
        // $scope.entity;	浏览器测试打印
        //定义初始method为add
        //var method = "add";
        var resultObject = null ;
        //修改需传id值,如果有id值,就去访问update方法
        if ($scope.entity.id != null) {
            resultObject = brandService.update($scope.entity)
        } else {
            resultObject = brandService.add($scope.entity)
        }
        //将brandService的执行结果封装到一个对象中,
        resultObject.success(function (response) {
            // response ={success:true|false,message:"添加成功"|"添加失败"}
            if (response.success) {
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        })
    }
    //根据id修改品牌
    /* 修改分为两步:
     *      1.数据回显
     *          根据id查询数据库,显示回页面
     *      2.执行修改
     *          根据id修改,点击保存刷新页面,提示修改成功或者失败
      * */
    $scope.findOneToUpdate = function (id) {
        // alert(id) 测试有没有id
        // response  {id  name  firstChar}
        brandService.findOneToUpdate(id).success(function (response) {
            $scope.entity = response;
        })
    }

    //删除

    $scope.dele = function () {
        if ($scope.selectIds.length > 0) {
            if (window.confirm("确认要删除选择的数据吗?")) {
                brandService.dele($scope.selectIds).success(function (response) {
                    if (response.success) {
                        $scope.reloadList();     //删除后页面刷新
                        $scope.selectIds = [];  //删除后将selectIds中的id清空
                    } else {
                        alert(response.message);
                    }
                })
            }
        }
    }

});
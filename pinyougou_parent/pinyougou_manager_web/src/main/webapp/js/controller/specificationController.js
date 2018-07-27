//品牌控制层
app.controller('specificationController', function ($scope,$controller, specificationService) {        //angularJS的依赖注入 specificationService

    //继承baseController  其本质是共用一个$scope作用域
    $controller("baseController",{$scope:$scope})   //单引号和双引号没有区别

    /*$scope.findPage = function (pageNum, pageSize) {
        specificationService.findPage(pageNum, pageSize).success(function (response) {
            // response={total:100,rows:[{},{},{}]}  总条数  当前页数据
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total; //从后台获取的总条数赋值到分页对象中
        })
    }*/
    //初始化组合类
    $scope.entity={specificationOptionList:[]};
    // 动态添加规格项
    $scope.addSpecificationOptionList=function(){
        $scope.entity.specificationOptionList.push({});
    }
    // 动态删除规格项    index的值是用ng-repeat中获取的
    $scope.delSpecificationOptionList=function(index){
        $scope.entity.specificationOptionList.splice(index,1);
    }

    //搜索完后的分页
    $scope.search = function (pageNum, pageSize) {
        specificationService.search(pageNum, pageSize, $scope.searchEntity).success(function (response) {
            // response={total:100,rows:[{},{},{}]}  总条数,当前页数据
            //从后台获取的总条数赋值到分页对象中
            $scope.paginationConf.totalItems = response.total;
            //从后台获取的当前页数据赋值到list中
            $scope.list = response.rows;
        })
    }

    /* //初始化findAll显示
     $scope.findAll = function () {
         specificationService.findAll().success(function (response) {
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
        if ($scope.entity.tbSpecification.id != null) {
            resultObject = specificationService.update($scope.entity)
        } else {
            resultObject = specificationService.add($scope.entity)
        }
        //将specificationService的执行结果封装到一个对象中,
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
        specificationService.findOneToUpdate(id).success(function (response) {
            $scope.entity = response;
        })
    }

    //删除

    $scope.dele = function () {
        if ($scope.selectIds.length > 0) {
            if (window.confirm("确认要删除选择的数据吗?")) {
                specificationService.dele($scope.selectIds).success(function (response) {
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

   /* $scope.search=function(){

    }*/

});
//控制层
app.controller('goodsListController' ,function($scope,$controller ,itemCatService  ,goodsService){

    $controller('baseController',{$scope:$scope});//继承

    // $scope.categoryList={"1":"图书、音像、电子书刊","2":"电子书刊"};    user={username:'class44'}
    $scope.category={};
    $scope.findAllItemCategory=function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                // response[i] {"id":1,"name":"图书、音像、电子书刊","parentId":0,"typeId":35}
                $scope.category[response[i].id]=response[i].name;

            }
        })
    }


    $scope.updateMarketable=function (market) {
        goodsService.updateMarketable($scope.selectIds,market).success(function (response) {
            if(response.success){
                $scope.reloadList();
                $scope.selectIds=[];
            }else{
                alert(response.message);
            }
        })
    }

    // 0：未审核  1：审核通过   2：审核未通过  3：关闭
    $scope.status=["未审核","审核通过","审核未通过","关闭"];
    $scope.maketStatus=["未上架","上架","下架"];

    //读取列表数据绑定到表单中
    $scope.findAll=function(){
        goodsService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }

    //分页
    $scope.findPage=function(page,rows){
        goodsService.findPage(page,rows).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne=function(id){
        goodsService.findOne(id).success(
            function(response){
                $scope.entity= response;
            }
        );
    }

    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.reloadList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele=function(){
        //获取选中的复选框
        goodsService.dele( $scope.selectIds ).success(
            function(response){
                if(response.success){
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity={};//定义搜索对象

    //搜索
    $scope.search=function(page,rows){
        goodsService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

});

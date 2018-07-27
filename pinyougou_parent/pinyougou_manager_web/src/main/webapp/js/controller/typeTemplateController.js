//控制层
app.controller('typeTemplateController', function ($scope, $controller, brandService, specificationService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    // [{"text":"内存大小"},{"text":"颜色"}]
    //大括号中表示customAttributeItems类型的一个数组
    $scope.entity = {customAttributeItems: []};  //初始化模板对象

    //动态新增扩展属性
    $scope.addCustomAttributeItems = function () {
        $scope.entity.customAttributeItems.push({});
    }
    //动态删除扩展属性
    $scope.delCustomAttributeItems = function (index) {
        $scope.entity.customAttributeItems.splice(index, 1);
    }

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        typeTemplateService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }
    // 模拟数据 $scope.brandList = {data: [{id: 1, text: "联想"}, {id: 2, text: "小米"}]};
    // 查询所有品牌数据 为了显示到select2
    $scope.findBrandList = function () {
        brandService.findBrandList().success(function (response) {
            // response 要求的格式[{id:1,text:''},{id:1,text:''}]
            $scope.brandList = {data: response};
        })
    }
    // 查询所有规格数据 为了显示到select2
    $scope.findSpecList = function () {
        specificationService.findSpecList().success(function (response) {
            // response 要求的格式[{id:1,text:''},{id:1,text:''}]
            $scope.specList = {data: response};
        })
    }

    // 把数组转成字符串  想要的效果是 [{"id":30,"text":"劲霸男装"},{"id":26,"text":"海澜之家"}]----> 劲霸男装,海澜之家
    $scope.arrayListToString = function (arrayList) {
        //将arrayList的一堆字符串转换为json格式的数组
        arrayList = JSON.parse(arrayList);
        // arrayList =[{"id":30,"text":"劲霸男装"},{"id":26,"text":"海澜之家"}]
        var str = "";
        for (var i = 0; i < arrayList.length; i++) {
            if (i == arrayList.length - 1) {
                str += arrayList[i].text;
            } else {
                str += arrayList[i].text + ",";
            }
        }
        return str;
    }

    //分页
    $scope.findPage = function (page, rows) {
        typeTemplateService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        typeTemplateService.findOne(id).success(
            function (response) {
                response.brandIds = JSON.parse(response.brandIds);
                response.specIds = JSON.parse(response.specIds);
                response.customAttributeItems = JSON.parse(response.customAttributeItems);
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = typeTemplateService.update($scope.entity); //修改
        } else {
            serviceObject = typeTemplateService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
   /* $scope.dele = function () {
        //获取选中的复选框
        typeTemplateService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }*/
    $scope.dele = function () {
        if ($scope.selectIds.length > 0) {
            if (window.confirm("确认要删除选择的数据吗?")) {
                typeTemplateService.dele($scope.selectIds).success(function (response) {
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

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        typeTemplateService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

});	

app.controller("goodsController", function ($scope, goodsService, itemCatService, typeTemplateService, uploadService) {
    // 初始化entity,方便追加属性
    $scope.entity = {tbGoodsDesc: {itemImages: [], specificationItems: []}, tbGoods: {isEnableSpec: '1'}}

    $scope.save = function () {
        //通过editor.html()获取富文本编辑器中的内容，添加到表中的introduction字段中
        $scope.entity.tbGoodsDesc.introduction = editor.html();
        goodsService.save($scope.entity).success(function (response) {
            if (response.success) {
                location.href = "goods.html";
            } else {
                alert(response.message);
            }
        })
    }
    //三级分类下拉框之一级分类
    $scope.findItemCategory1List = function () {
        //一级分类的ParentId为0
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCategory1List = response;
        })
    }
    // angularJS的一个事件,用来监测某数据的变化,类似于onChange()
    $scope.$watch("entity.tbGoods.category1Id", function (newValue, oldValue) {  //oldValue 可以省略
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCategory2List = response;
            $scope.itemCategory3List = []; //一级分类产生变化，三级分类清空(三级分类是根据二级分类的ID来查的)
        });
    })
    $scope.$watch("entity.tbGoods.category2Id", function (newValue, oldValue) {  //oldValue 可以省略
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCategory3List = response;

        });
    })
    //根据选中的第3级商品分类,获取模板id,在页面指定位置{{entity.tbGoods.typeTemplateId}}显示
    $scope.$watch("entity.tbGoods.category3Id", function (newValue, oldValue) {  //oldValue 可以省略
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.tbGoods.typeTemplateId = response.typeId;
        });
    })


    $scope.$watch("entity.tbGoods.typeTemplateId", function (newValue, oldValue) {  //oldValue 可以省略
        typeTemplateService.findOne(newValue).success(function (response) {
            $scope.typeTemplate = response;//获取类型模板
            $scope.brandList = JSON.parse($scope.typeTemplate.brandIds);//获取已选中的类型模板下的品牌列表
            //显示某模板下的扩展属性
            // 保存的数据格式：[{"text":"内存大小","value":"101M"},{"text":"颜色","value":"红色"}]
            //页面的pojo.value就是这么来的
            $scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
            //类型模板改变,itemList清空
            $scope.entity.itemList=[{spec:{},price:0,num:9999,status:'1',isDefault:'0'}];

            //通过模板id获得规格数据,在页面循环遍历显示
            // 获取规格数据：要求的数据格式[{id text options:[{},{},{}],{id text options:[{},{},{}]}
            typeTemplateService.findSpecList(newValue).success(function (response) {
                $scope.specList = response;
            })
        });
    })

    // 勾选规格小项修改即将保存的规格数据
    // 需要的数据格式  [{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
    $scope.updateSpecificationItems = function ($event, specName, optionName) {   // 1 这里方法的参数是页面传过来的     ng-click="updateSpecificationItems($event,spec.text,option.optionName)"
        var specObject = selectObjectFromList($scope.entity.tbGoodsDesc.specificationItems, specName);
        if ($event.target.checked) { //表示勾选
            // 先判断数组中是否有当前规格的数据
            if (specObject != null) {  //向当前对象的attributeValue中追加optionName
                specObject.attributeValue.push(optionName);
            } else {  //直接向大数组中追加一个对象     2
                $scope.entity.tbGoodsDesc.specificationItems.push({
                    attributeName: specName,
                    attributeValue: [optionName]
                });
            }
        } else {  //取消勾选
            var index = specObject.attributeValue.indexOf(optionName);
            specObject.attributeValue.splice(index, 1);
            // 需要判断当前对象中是否还有attributeValue 如果没有应该从大数组中移除对象
            if (specObject.attributeValue.length == 0) {
                var index = $scope.entity.tbGoodsDesc.specificationItems.indexOf(specObject);
                $scope.entity.tbGoodsDesc.specificationItems.splice(index, 1);
            }
        }
        //创建商品列表
        createItemList();

    }
    //是否启用规格,点击清空数据
    $scope.useSpec = function () {
        $scope.entity.tbGoodsDesc.specificationItems = [];
        $scope.entity.itemList = [];
    }

    function createItemList() {
        // specItems: [{"attributeName":"腰围","attributeValue":["23","32"]},{"attributeName":"尺码","attributeValue":["165","170"]}]
        var specItems = $scope.entity.tbGoodsDesc.specificationItems;
        // 初始化itemList
        $scope.entity.itemList = [{spec: {}, price: 0, num: 9999, status: '1', isDefault: '0'}];

        for (var i = 0; i < specItems.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, specItems[i].attributeName, specItems[i].attributeValue)
        }
    }
    // specItems: [{"attributeName":"腰围","attributeValue":["23","32"]},{"attributeName":"尺码","attributeValue":["165","170"]}]
    // itemList : [{spec:{},price:0,num:9999,status:'1',isDefault:'0'},{spec:{},price:0,num:9999,status:'1',isDefault:'0'}]
    //要求添加数据后,$scope.entity.itemList=[{spec:{腰围:23,尺码:165},price:0,num:9999,status:'1',isDefault:'0'},
    //                                      {spec:{腰围:32,尺码:165},price:0,num:9999,status:'1',isDefault:'0'},
    //                                      {spec:{腰围:23,尺码:170},price:0,num:9999,status:'1',isDefault:'0'},
    //                                      {spec:{腰围:32,尺码:170},price:0,num:9999,status:'1',isDefault:'0'}]
    //attributeName:腰围  attributeValue:23,32    attributeName:尺码  attributeValue:165,170
    function addColumn(itemList, attributeName, attributeValue) {

        var newList = [];

        for (var i = 0; i < itemList.length; i++) {
//          itemList[i]:{spec:{},price:0,num:9999,status:'1',isDefault:'0'}
            for (var j = 0; j < attributeValue.length; j++) {
                var newItem = JSON.parse(JSON.stringify(itemList[i]));  //深克隆
                //往对象中的属性(这个属性也是一个对象)添加一对类似于Map的属性的格式就是下面这样的
                newItem.spec[attributeName] = attributeValue[j];    //往spec中添加attributeName和attributeValue
                newList.push(newItem);      //往大数组变量中添加一个循环赋值后的每一个对象
            }
        }
        //for循环走完之后,spec中的数据就已经添加上了
        // {spec:{"腰围"：23},price:0,num:9999,status:'1',isDefault:'0'}
        // {spec:{"腰围"：32},price:0,num:9999,status:'1',isDefault:'0'}

        return newList;
    }

    //因为不需要跟页面交互,所以这个方法不需要$scope.方法名
    // 根据key值判断数组中是否有此对象
    function selectObjectFromList(specificationItems, specName) {
        for (var i = 0; i < specificationItems.length; i++) {
            if (specificationItems[i].attributeName == specName) {
                return specificationItems[i];
            }
        }
        return null;
    }

    // 上传图片添加到数组中
    $scope.addImageToItemImages = function () {
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image);
    }

    //文件上传
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            // response:{success:true,message:'http://192.168.25.133/group1/M00/00/00/wKgZhVtL_V2AMd-OAACuI4TeyLI452.jpg'}
            if (response.success) {
                $scope.image.url = response.message;
            } else {
                alert(response.message);
            }
        })
    }
    //删除商品图片 注意:页面要传一个$index过来,ng-click="delImage($index)"
    $scope.delImage = function (index) {
        $scope.entity.tbGoodsDesc.itemImages.splice(index, 1);
    }
})

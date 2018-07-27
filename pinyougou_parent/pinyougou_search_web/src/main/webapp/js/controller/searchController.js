app.controller('searchController', function ($scope,$location, searchService) { //$location为内置服务

    //添加搜索过滤条件 规格
    $scope.addParamMap = function (key, value) {
        $scope.paramMap[key] = value;
        //paramMap发生改变,重新调用搜索方法
        $scope.searchByParam();
    }
    //添加搜索过滤条件 规格选项
    $scope.addParamMapSpec = function (key, value) {
        $scope.paramMap.spec[key] = value;
        //paramMap发生改变,重新调用搜索方法
        $scope.searchByParam();
    }
    //点击X号 删除搜索过滤条件 规格
    $scope.deleParamMap=function(key){
        $scope.paramMap[key]='';
        $scope.searchByParam();
    }
    $scope.deleParamMapSpec=function(key){
        // spec:{网络:移动3G,机身内存:16G}
        delete $scope.paramMap.spec[key];  //纯Js的代码   delete删除对象中的数据
        $scope.searchByParam();
    }
    //初始化及搜索
    $scope.initSearch = function () {
        //alert(JSON.stringify($location.search()['keywords']));    //弹出首页搜索传过来的关键字
        //设置搜索框默认值(广告)
        //搜索框默认值    分类  品牌  规格及规格选项   价格    排序方式    默认当前页为第1 页
        //spec:{网络:移动3G,机身内存:16G}   sort:'ASC' 默认排序为价格升序,降序则为DESC
        $scope.paramMap = {keywords: $location.search()['keywords'], category: '', brand: '', spec: {},price:'',priceSort:'ASC',page:1};
        //调用抽离的搜索方法
        $scope.searchByParam();
    }

    //输入框搜索
    $scope.search=function () {
        $scope.paramMap={keywords:$scope.paramMap.keywords,category:'',brand:'',spec:{},price:'',priceSort:'ASC',page:1};  //spec:{网络:移动3G,机身内存:16G}
        $scope.searchByParam();
    }

    //抽离出来的搜索方法
    $scope.searchByParam=function () {
        searchService.search($scope.paramMap).success(function (response) {
            $scope.resultMap = response;
            // $scope.itemList = response.itemList;
            /*$scope.totalPage=[];
            for (var i = 1; i <= response.totalPage; i++) {
                $scope.totalPage.push(i);
            }*/
            // 初始化时加载分页
            buildPageLabel();
        })
    }
    // 分页
    function buildPageLabel() {
        $scope.totalPages = [];//新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPage;//得到最后页码
        var firstPage = 1;//开始页码
        var lastPage = maxPageNo;//截止页码
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后边有点
        if ($scope.resultMap.totalPage > 5) { //如果总页数大于 5 页,显示部分页码
            if ($scope.paramMap.page <= 3) {//如果当前页小于等于 3
                lastPage = 5; //前 5 页
                $scope.firstDot = false;//前面没点
            } else if ($scope.paramMap.page >= lastPage - 2) {//如果当前页大于等于最大页码-2
                firstPage = maxPageNo - 4;  //后 5 页
                $scope.lastDot = false;//后边没点
            } else { //显示当前页为中心的 5 页
                firstPage = $scope.paramMap.page - 2;
                lastPage = $scope.paramMap.page + 2;
            }
        } else {
            $scope.firstDot = false;//前面无点
            $scope.lastDot = false;//后边无点
        }
        //循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.totalPages.push(i);
        }
    }
})



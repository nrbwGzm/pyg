app.controller("cartController", function ($scope, cartService) {

    //  向用户购物车中添加一条订单
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(function (response) {
            if (response.success) {
                //添加成功,刷新购物车页面显示
                $scope.findCartList();
            } else {
                alert(response.message);
            }
        })
    }

    //寻找用户购物车,进行显示
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            $scope.totalNum = 0;
            $scope.totalMoney = 0.00;
            //循环购物车,获取每个商家购物车列表,从每个商家购物车列表中获取每条订单
            for (var i = 0; i < response.length; i++) {
                var orderItemList = response[i].orderItemList;
                for (var j = 0; j < orderItemList.length; j++) {
                    var orderItem = orderItemList[j];
                    //计算用户订单总数量
                    $scope.totalNum += orderItem.num;
                    //计算用户购物车总价
                    $scope.totalMoney += orderItem.totalFee;
                }

            }


        })
    }
})
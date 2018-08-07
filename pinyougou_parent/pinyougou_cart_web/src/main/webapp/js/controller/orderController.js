app.controller("orderController", function ($scope, addressService, cartService, orderService) {

    $scope.entity = {paymentType: '1'};

    //保存订单(含姓名，手机号，地址)
    $scope.saveOrder = function () {
        // `payment_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
        $scope.entity['receiverAreaName'] = $scope.selectedAddress.address;//收货人地址
        $scope.entity['receiverMobile'] = $scope.selectedAddress.mobile;//收货人手机号
        $scope.entity['receiver'] = $scope.selectedAddress.contact;//收货人姓名

        //如果保存数据成功，跳转到支付页
        orderService.save($scope.entity).success(function (response) {
            if (response.success) {
                location.href = "http://pay.pinyougou.com/pay.html"
            } else {
                alert(response.message);
            }
        })

        //     `receiver_area_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
        //     `receiver_mobile` varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
        //     `receiver` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',


    }


    $scope.selectedAddress = null; //定义选择好的收件人信息（收货地址及联系人,联系人手机号）

    $scope.findAddressByUser = function () {
        addressService.findAddressByUser().success(function (response) {
            $scope.addressList = response;
            for (var i = 0; i < response.length; i++) {
                if (response[i].isDefault == '1') { //如果是默认地址
                    $scope.selectedAddress = response[i];
                    break;
                }

            }
            //如果用户有收货地址列表，并且没有指定默认地址，选择收货地址列表中的第一个收货地址
            if (response != null && $scope.selectedAddress == null) {
                $scope.selectedAddress = response[0];
            }
        })
    }


    $scope.isSelectedAddress = function (pojo) {
        if ($scope.selectedAddress == pojo) {
            return true;
        }
        return false;
    }
    //页面点击事件，选择收件人信息
    $scope.selectAddress = function (pojo) {
        $scope.selectedAddress = pojo;
    }


    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            $scope.totalNum = 0;
            $scope.totalMoney = 0.00;
            for (var i = 0; i < response.length; i++) {
                var orderItemList = response[i].orderItemList;
                for (var j = 0; j < orderItemList.length; j++) {
                    $scope.totalNum += orderItemList[j].num;
                    $scope.totalMoney += orderItemList[j].totalFee;
                }

            }

        })
    }
})
app.controller("orderController",function ($scope,addressService,cartService,orderService) {

    $scope.entity={paymentType:'1'};

    $scope.saveOrder=function () {
        // `payment_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
        $scope.entity['receiverAreaName']=$scope.selectedAddress.address;
        $scope.entity['receiverMobile']=$scope.selectedAddress.mobile;
        $scope.entity['receiver']=$scope.selectedAddress.contact;

        orderService.save( $scope.entity).success(function (response) {
            if(response.success){
                location.href="http://pay.pinyougou.com/pay.html"
            }else{
                alert(response.message);
            }
        })

        //     `receiver_area_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
        //     `receiver_mobile` varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
        //     `receiver` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',


    }


    $scope.selectedAddress=null;

    $scope.findAddressByUser=function () {
        addressService.findAddressByUser().success(function (response) {
            $scope.addressList=response;
            for (var i = 0; i < response.length; i++) {
                if(response[i].isDefault=='1'){
                    $scope.selectedAddress=response[i];
                    break;
                }

             }
             if(response!=null&&$scope.selectedAddress==null){
                 $scope.selectedAddress=response[0];
             }
        })
    }

    $scope.isSelectedAddress=function(pojo){
        if( $scope.selectedAddress==pojo){
            return true;
        }
        return false;
    }

    $scope.selectAddress=function (pojo) {
        $scope.selectedAddress=pojo;
    }




    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            $scope.totalNum=0;
            $scope.totalMoney=0.00;
            for (var i = 0; i < response.length; i++) {
                var orderItemList = response[i].orderItemList;
                for (var j = 0; j < orderItemList.length; j++) {
                    $scope.totalNum+= orderItemList[j].num;
                    $scope.totalMoney+=orderItemList[j].totalFee;
                }

            }

        })
    }
})
app.controller("payController",function ($scope,$location,payService) {

    $scope.unifiedorder=function () {
        payService.unifiedorder().success(function (response) {

            $scope.resultMap=response;

            new QRious({
                element: document.getElementById('qrious'),
                size: 250,
                value: response.code_url,
                level:'L'
            });

            $scope.queryOrder(response.out_trade_no);

        })
    }

    $scope.queryOrder=function (out_trade_no) {
        payService.queryOrder(out_trade_no).success(function (response) {
            if(response.success){
                location.href="paysuccess.html#?totalFee="+$scope.resultMap.total_fee;
            }else{
                if(response.message=="支付超时"){
                    $scope.unifiedorder();
                }else{
                    location.href="payfail.html";
                }

            }
        })
        
        
    }

    $scope.showMoney=function(){
       $scope.totalFee = $location.search()['totalFee'];

    }

})
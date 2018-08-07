app.controller("payController",function ($scope,payService) {

    $scope.unifiedorder=function () {
        payService.unifiedorder().success(function (response) {


            new QRious({
                element: document.getElementById('qrious'),
                size: 250,
                value: 'http://www.itheima.com',
                level:'H'
            })



        })
    }

})
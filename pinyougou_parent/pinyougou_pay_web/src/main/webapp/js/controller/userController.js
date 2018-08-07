app.controller('userController',function ($scope,userService) {

    $scope.reg=function () {
        // 判断两次密码是否一致
        if($scope.entity.password != $scope.password2){
            alert("两次密码输入不一致！")
            return;
        }

        userService.add($scope.entity,$scope.code).success(function (response) {
            if(response.success){
                alert("即将跳转到登录页面");
            }else{
                alert(response.message);
            }

        })


    }
    
    $scope.sendCode=function () {

        userService.sendCode($scope.entity.phone).success(function (response) {
            alert("发送成功");
        })

    }

})
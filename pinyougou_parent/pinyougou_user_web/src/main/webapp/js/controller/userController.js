app.controller('userController', function ($scope, userService) {

    //是否成功发送验证码
    $scope.sendCode = function () {
        userService.sendCode($scope.entity.phone).success(function (response) {
            alert("验证码发送成功");
        })

    };

    //完成注册,存数据
    $scope.reg = function () {
        //判断两次密码是否一致
        if ($scope.entity.password != $scope.password2) {
                alert("两次密码输入不一致!请重新输入!");
                return;
        }
        //返回user对象,验证码
        //如果成功,添加user到表中,跳转登录页面
        //如果失败,弹出失败信息
        userService.add($scope.entity,$scope.code).success(function (response) {
            if(response.success){
                alert("即将跳转到登录页面");
                //跳转到登陆页面
                location.href="http://user.pinyougou.com";

            }else{
                alert(response.message);
            }

        })
    };
    $scope.showName=function () {

        userService.showName().success(function (response) {
            $scope.username = JSON.parse(response);
            //因为传过来的用户名是一个字符串,所以前后有双引号
            //JSON.parse去掉用户名上的双引号
        })
    };

});
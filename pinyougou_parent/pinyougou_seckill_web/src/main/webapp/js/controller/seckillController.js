app.controller("seckillController",function ($scope,$location,$interval,seckillService) {

    $scope.findSeckillList=function () {
        seckillService.findSeckillList().success(function (response) {
            $scope.seckillList=response;
        })
    }
    
    
    $scope.saveSeckillOrder=function () {
        // 保存订单 完成后 跳转到支付

        seckillService.saveSeckillOrder($scope.seckillGoodsId).success(function (response) {
            if(response.success){
                alert("下单成功，即将跳转到支付页面");
            }else{
                alert(response.message);
            }

        })

        
    }

    $scope.timeString="";
    $scope.findOne=function () {
        $scope.seckillGoodsId = $location.search()['seckillGoodsId'];

        if($scope.seckillGoodsId>0){



        seckillService.findOne($scope.seckillGoodsId).success(function (response) {
            $scope.seckillGoods=response;

            //计算出剩余时间
            var endTime = new Date($scope.seckillGoods.endTime).getTime();
            var nowTime = new Date().getTime();

            //剩余时间
            $scope.secondes =Math.floor( (endTime-nowTime)/1000 );

            var time =$interval(function () {
                if($scope.secondes>0){
                    //时间递减
                    $scope.secondes--;
                    //时间格式化
                    $scope.timeString=$scope.convertTimeString($scope.secondes);
                }else{
                    //结束时间递减
                    $interval.cancel(time);
                }
            },1000);


        })

        }
      /*  // $interval(执行的函数,间隔的毫秒数,运行次数);
        $scope.times=10;
        $interval(function () {
            $scope.times--;
        },1000,10);



            天数：days =  floor(seconds/60/60/24)  向下取整  floor  ==10        .387
            小时：hours =  floor((seconds -  days*24*60*60)/60/60)
            分钟：minutes = floor((seconds -  days*24*60*60 - hours*60*60) /60)
            秒   ：secs  = seconds -  days*24*60*60 - hours*60*60 -minutes*60

        距离结束：30天 01:56:78   现在  结束时间*/
    }

    $scope.convertTimeString=function (allseconds) {
        //计算天数
        var days = Math.floor(allseconds/(60*60*24));

        //小时
        var hours =Math.floor( (allseconds-(days*60*60*24))/(60*60) );

        //分钟
        var minutes = Math.floor( (allseconds-(days*60*60*24)-(hours*60*60))/60 );

        //秒
        var seconds = allseconds-(days*60*60*24)-(hours*60*60)-(minutes*60);

        //拼接时间
        var timString="";
        if(days>0){
            timString=days+"天:";
        }

        if(hours<10){
            hours="0"+hours;
        }
        if(minutes<10){
            minutes="0"+minutes;
        }
        if(seconds<10){
            seconds="0"+seconds;
        }
        return timString+=hours+":"+minutes+":"+seconds;
    }



})
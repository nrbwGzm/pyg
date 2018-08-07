app.service("seckillService",function ($http) {

    this.findSeckillList=function () {
      return  $http.get("./seckill/findSeckillList");
    }

    this.findOne=function (seckillGoodsId) {
      return  $http.get("./seckill/findOne/"+seckillGoodsId);
    }

    this.saveSeckillOrder=function (seckillGoodsId) {
      return  $http.get("./seckill/saveSeckillOrder/"+seckillGoodsId);
    }


})
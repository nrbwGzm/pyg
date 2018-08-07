app.service("payService",function ($http) {

    this.unifiedorder=function () {
        return $http.get("./pay/unifiedorder");
    }

    this.queryOrder=function (out_trade_no) {
        return $http.get("./pay/queryOrder/"+out_trade_no);
    }

})
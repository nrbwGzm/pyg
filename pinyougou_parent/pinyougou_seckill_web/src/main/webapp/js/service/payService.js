app.service("payService",function ($http) {

    this.unifiedorder=function () {
        return $http.get("./pay/unifiedorder");
    }

})
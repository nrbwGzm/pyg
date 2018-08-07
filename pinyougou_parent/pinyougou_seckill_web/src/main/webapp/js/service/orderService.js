app.service("orderService",function ($http) {

    this.save=function (entity) {
        return $http.post("./order/add",entity);
    }

})
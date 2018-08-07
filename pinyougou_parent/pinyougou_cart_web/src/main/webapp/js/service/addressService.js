app.service("addressService",function ($http) {

    this.findAddressByUser=function () {
       return $http.get("./address/findAddressByUser");
    }

    
})
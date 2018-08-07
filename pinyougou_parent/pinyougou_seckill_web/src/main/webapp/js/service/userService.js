app.service('userService',function ($http) {

    this.sendCode=function (phone) {
        return $http.post("/user/sendCode/"+phone);
    }

    this.add=function(user,code){
        return $http.post("/user/add/"+code,user);

    }

})
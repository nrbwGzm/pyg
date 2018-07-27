app.service("userService", function ($http) {

    this.showUsername = function () {
        return $http.get("../user/showUsername");
    }
})
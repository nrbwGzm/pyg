app.service('searchService',function ($http) {

    this.search=function (paramMap) {
        return $http.post("./search/searchFromSolr",paramMap);
    }

})
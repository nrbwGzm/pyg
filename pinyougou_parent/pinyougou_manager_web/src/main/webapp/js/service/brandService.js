//品牌服务层
app.service("brandService", function ($http) {

    this.findBrandList=function(){
        return $http.get("../brand/findBrandList");
    }
    /*  用search,不传参数相当于findPage
            this.findPage = function (pageNum, pageSize) {
                return $http.get("../brand/findPage/" + pageNum + "/" + pageSize);
            }*/
    //读取列表数据绑定到表单中
    //搜索完后的分页
    this.search = function (pageNum, pageSize, searchEntity) {
        return $http.post("../brand/search/" + pageNum + "/" + pageSize, searchEntity);
    }

    /* //初始化findAll显示
     this.findAll = function () {
         return $http.get("../brand/findAll")
     }*/
    //新建与修改
    this.add = function (entity) {

        return $http.post("../brand/add", entity);
    }
    this.update = function (entity) {

        return $http.post("../brand/update", entity);
    }
    //根据id修改品牌
    /* 修改分为两步:
     *      1.数据回显
     *          根据id查询数据库,显示回页面
     *      2.执行修改
     *          根据id修改,点击保存刷新页面,提示修改成功或者失败
      * */
    this.findOneToUpdate = function (id) {
        // alert(id) 测试有没有id
        // response  {id  name  firstChar}
        return $http.get("../brand/findOneToUpdate/" + id);
    }

    this.dele = function (selectIds) {

        return $http.get("../brand/dele/" + selectIds);
    }
});
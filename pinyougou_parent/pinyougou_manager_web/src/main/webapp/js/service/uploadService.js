app.service("uploadService",function ($http) {
    this.uploadFile=function () {
        //1.新建html5的上传对象
        var formData = new FormData();
        //2.上传对象中添加file
        //file.files[0] js   file看做document中的文件
        formData.append("file",file.files[0]);
        //3.返回http
        return $http({
            method:'post',
            url:'../upload/uploadFile',
            data:formData,
            headers:{'Content-Type':undefined},// 相当于multipart/form-data的方式向后传输数据
            transformRequest:angular.identity   // 通过anjularjs的函数将文件序列化
            //transformRequest:预处理函数     identity:标识
        })
    }

})
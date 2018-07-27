//服务层
app.service('typeTemplateService',function($http){


    this.findSpecList=function(typeTemplateId){
        return $http.get('../typeTemplate/findSpecList/'+typeTemplateId);
    }
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../typeTemplate/findAll');		
	}
	//分页 
	this.findPage=function(pageNum,pageSize){
		return $http.get('../typeTemplate/findPage/'+pageNum+"/"+pageSize);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../typeTemplate/findOne/'+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../typeTemplate/add',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../typeTemplate/update',entity );
	}
	//删除
	this.dele=function(selectIds){
		return $http.get('../typeTemplate/delete/'+selectIds);
	}
	//搜索
	this.search=function(pageNum,pageSize,searchEntity){
		return $http.post('../typeTemplate/search/'+pageNum+"/"+pageSize, searchEntity);
	}    	
});

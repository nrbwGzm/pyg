package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.SearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SolrSearchController {

    @Reference
    private SearchService searchService;

    @RequestMapping("/searchFromSolr")
    public Map searchFromSolr(@RequestBody  Map paramMap){
//        paramMap={Keywords:'三星'}

       return searchService.searchFromSolr(paramMap);

    }
}

package com.test.controller;

import com.test.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 解析数据放入es索引库中
     * @param keyWord
     * @return
     * @throws IOException
     */
    @GetMapping("/parse/{keyWord}")
    public Boolean parse(@PathVariable("keyWord") String keyWord) throws IOException {
        return goodsService.parseGoods(keyWord);
    }

    /**
     * 根据keyword获取数据
     * @param keyWord
     * @param pageNo
     * @param pageSize
     * @return
     * @throws IOException
     */
    @GetMapping("/search/{keyWord}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> searchPage(@PathVariable("keyWord") String keyWord, @PathVariable("pageNo") int pageNo, @PathVariable("pageSize") int pageSize) throws IOException {
        return goodsService.searchPage(keyWord, pageNo, pageSize);
    }
}

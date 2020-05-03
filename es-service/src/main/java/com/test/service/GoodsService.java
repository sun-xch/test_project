package com.test.service;

import com.alibaba.fastjson.JSON;
import com.test.pojo.Goods;
import com.test.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GoodsService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 解析数据放入es索引库中
     * @param keyWord
     * @return
     * @throws IOException
     */
    public Boolean parseGoods(String keyWord) throws IOException {
        //解析数据
        List<Goods> goodsList = new HtmlParseUtil().parseJD(keyWord);
        //把数据集放入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        for (int i = 0; i < goodsList.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_goods").source(JSON.toJSONString(goodsList.get(i)),XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    /**
     * 根据keyword分页查询
     * @param keyWord
     * @param pageNo
     * @param pageSize
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> searchPage(String keyWord, int pageNo, int pageSize) throws IOException {
        if(pageNo <= 1){
            pageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //精准匹配
        //MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyWord);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyWord);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false);//多个高亮显示 关闭
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for(SearchHit documentFields: searchResponse.getHits().getHits()){
            //解析高亮字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            //将原来的字段替换为高亮的字段
            if(title != null){
                Text[] fragments = title.fragments();
                String newTitle = "";
                for (Text text : fragments) {
                    newTitle += text;
                }
                sourceAsMap.put("title",newTitle);//高亮内容 替换 原来的内容
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}

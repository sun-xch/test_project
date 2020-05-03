package com.test;

import com.alibaba.fastjson.JSON;
import com.test.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.action.admin.indices.stats.CommonStatsFlags.Flag.Search;

/**
 * es7.6.2 测试高级客户端API
 */
@SpringBootTest
class EsServiceApplicationTests {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	//测试索引的创建 Request PUT index_1
	@Test
	public void testCreatIndex() throws IOException {
		//1.创建索引请求
		CreateIndexRequest request = new CreateIndexRequest("index_1");
		//2.客户端执行请求 IndicesClient,请求后获得响应
		CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
	}

	//测试获取索引,判断其是否存在
    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("index_1");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

	//测试删除索引
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("index_1");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        boolean isSuccessful = delete.isAcknowledged();

        System.out.println(isSuccessful);
    }

    //测试添加文档
    @Test
    public void testAddDocument() throws IOException {
        User user = new User("测试用户1",23);
        //创建请求
        IndexRequest request = new IndexRequest("index_1");
        //规则 PUT/index_1/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        //将数据放入请求
        request.source(JSON.toJSONString(user), XContentType.JSON);
        //客户端发送请求,获取响应的结果
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());//对应命令返回的状态 新增 CREATE

    }

    //获取文档，判断是否存在
    @Test
    public void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("index_1", "1");
        //不获取返回的——source 的上下文
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //获取文档信息
    @Test
    public void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("index_1", "1");
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        String sourceAsString = getResponse.getSourceAsString();
        System.out.println(sourceAsString);
        System.out.println(getResponse);
    }

    //更新文档信息
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("index_1", "1");
        updateRequest.timeout("1s");
        User user = new User("测试用户2", 18);
        updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    //删除文档记录
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("index_1", "1");
        deleteRequest.timeout("1s");
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    //批量插入数据
    @Test
    public void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("test1",21));
        userList.add(new User("test2",22));
        userList.add(new User("test3",23));
        userList.add(new User("test4",24));
        userList.add(new User("test5",25));
        userList.add(new User("test6",26));

        for (int i = 0; i < userList.size(); i++) {
            bulkRequest.add(new IndexRequest("index_1").id(""+(i+1)).source(JSON.toJSONString(userList.get(i)),XContentType.JSON));
        }
        //批处理请求
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures());//是否失败
    }

    //查询
    //SearchRequest 搜索请求
    //SearchSourceBuilder   条件构造
    //HighlightBuilder  构造高亮
    //TermQueryBuilder  精确查询
    @Test
    public void testSearch() throws IOException {
	    //创建请求
        SearchRequest searchRequest = new SearchRequest("index_1");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询条件，可以使用QueryBuilders工具类实现
        //精确匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "test1");
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("==========================");
        for(SearchHit documentFields : searchResponse.getHits().getHits()){
            System.out.println(documentFields.getSourceAsMap());
        }
    }
}

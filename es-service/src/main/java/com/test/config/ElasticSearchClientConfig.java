package com.test.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfig {

    //spring <beans id="restHighLevelClient" class="RestHighLevelClient">
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1",9200,"http")
                        /*new HttpHost("127.0.0.1",9201,"http"),
                        new HttpHost("127.0.0.1",9202,"http"),
                        new HttpHost("127.0.0.1",9203,"http")*/));
        return client;
    }

}

package com.test.utils;

import com.test.pojo.Goods;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {

    public static void main(String[] args) throws IOException {
        new HtmlParseUtil().parseJD("java").forEach(System.out::println);
    }

    /**
     * 爬取页面信息
     * @param keyword
     * @return
     * @throws IOException
     */
    public List<Goods> parseJD(String keyword) throws IOException {
        //获取请求 :https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword="+keyword;
        //解析网页(Jsoup返回的Document就是Document对象)
        Document document = Jsoup.parse(new URL(url),60000);
        //所有在js中可以使用的方法，这里也能使用
        Element element = document.getElementById("J_goodsList");
        //System.out.println(element.html());
        //获取所有的li元素
        Elements li = element.getElementsByTag("li");
        ArrayList<Goods> goodsList = new ArrayList<>();
        //获取元素中的内容
        System.out.println(li.size());
        for(Element el : li){
            //关于图片网站可能使用懒加载的方式 所以不直接获取src
            String img = el.getElementsByTag("img").eq(0).attr("src");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            Goods goods = new Goods();
            goods.setTitle(title);
            goods.setImg(img);goods.setPrice(price);

            goodsList.add(goods);
        }
        return goodsList;
    }
}

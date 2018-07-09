package com.lianyubo.proxymodule.contentanalysis.multiProxies;

import com.lianyubo.entity.Proxy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class IP66ListPageContentAnalysis implements ProxyListPageContentAnalysis {

    /**
     * 解析结果
     * @param content
     * @return
     */
    @Override
    public List<Proxy> contentAnalysis(String content) {
        //构造ip代理List
        List<Proxy> proxyList = new ArrayList<>();
        if (content == null || content.equals("")){
            return proxyList;
        }
        Document document = Jsoup.parse(content);

        //#footer > div > table > tbody > tr:nth-child(2) > td:nth-child(1)
        Elements elements = document.select("#footer").select("div").select("table")
                .select("tbody").select("tr");
        for (Element element : elements){
            String ip = element.select("td:nth-child(1)").first().text();
            String port  = element.select("td:nth-child(2)").first().text();
            String isAnonymous = element.select("td:nth-child(4)").first().text();
//            if(!anonymousFlag || isAnonymous.contains("匿")){
            proxyList.add(new Proxy(1000,ip, Integer.valueOf(port)));
//            }
        }
        return proxyList;
    }
}

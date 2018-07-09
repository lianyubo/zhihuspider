package com.lianyubo.proxymodule.contentanalysis.multiProxies;

import com.lianyubo.proxymodule.entity.Proxy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class XiciListPageContentAnalysis implements ProxyListPageContentAnalysis {
    @Override
    public List<Proxy> contentAnalysis(String hmtl) {
        Document document = Jsoup.parse(hmtl);
        //#ip_list > tbody > tr:nth-child(7) > td:nth-child(2)
        Elements elements = document.select("#ip_list").select("tbody").select("#ip_list");
        List<Proxy> proxyList = new ArrayList<>(elements.size());
        for (Element element : elements){
            String ip = element.select("td:eq(1)").first().text();
            String port  = element.select("td:eq(2)").first().text();
            String isAnonymous = element.select("td:eq(4)").first().text();
            if(!anonymousFlag || isAnonymous.contains("匿")){
                //proxyList.add(new Proxy(ip, Integer.valueOf(port), TIME_INTERVAL));
            }
        }
        return proxyList;
    }
}
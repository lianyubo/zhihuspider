package com.lianyubo.zhihumodule.contentanalysis;

/**
 * package com.lianyubo.proxymodule.contentanalysis.multiproxies;


 import com.lianyubo.proxymodule.ProxyListPageContentAnalysis;
 import com.lianyubo.proxymodule.entity.Proxy;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;

 import java.util.ArrayList;
 import java.util.List;

 import static com.lianyubo.zhihumodule.util.Constants.TIME_INTERVAL;

 public class IP66ListPageContentAnalysis implements ProxyListPageContentAnalysis {
@Override
public List<Proxy> contentAnalysis(String content) {
List<Proxy> proxyList = new ArrayList<>();
if (content == null || content.equals("")){
return proxyList;
}
Document document = Jsoup.parse(content);
Elements elements = document.select("table tr:gt(1)");
for (Element element : elements){
String ip = element.select("td:eq(0)").first().text();
String port  = element.select("td:eq(1)").first().text();
String isAnonymous = element.select("td:eq(3)").first().text();
//            if(!anonymousFlag || isAnonymous.contains("匿")){
proxyList.add(new Proxy(ip, Integer.valueOf(port), TIME_INTERVAL));
//            }
}
return proxyList;
}
}

 */
public interface ContentAnalysis {

}

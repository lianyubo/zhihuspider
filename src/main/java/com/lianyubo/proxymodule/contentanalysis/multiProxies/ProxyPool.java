package com.lianyubo.proxymodule.contentanalysis.multiProxies;


import com.lianyubo.entity.Proxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 代理池
 */
public class ProxyPool {
    /**
     * proxySet读写锁
     */
    //ReentrantReadWriteLock实现lock的一种形式
    public final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    public final static Set<Proxy> proxySet = new HashSet<Proxy>();


    /**
     * 代理池延迟队列 需要proxy的实体类实现Delay和Serializable
     */
   // public final static DelayQueue<Proxy> proxyQueue = new DelayQueue();
    public final static Map<String, Class> proxyMap = new HashMap<>();

    static {

        //这里只是处理4页的内容
        int pages = 4;
        for(int i = 1; i <= pages; i++){

            proxyMap.put("http://www.xicidaili.com/wt/" + i + ".html", XiciListPageContentAnalysis.class);//国内HTTP代理页面
            proxyMap.put("http://www.xicidaili.com/nn/" + i + ".html", XiciListPageContentAnalysis.class);//国内普通代理页面
            proxyMap.put("http://www.xicidaili.com/wn/" + i + ".html", XiciListPageContentAnalysis.class);//国内HTTPS代理页面
            proxyMap.put("http://www.xicidaili.com/nt/" + i + ".html", XiciListPageContentAnalysis.class);//国内高匿代理页面
            proxyMap.put("http://www.xicidaili.com/daili/" + i + ".html", IP66ListPageContentAnalysis.class);

            //66ip代理网站提供34个省市的代理，每个地区代理一页数据
            for(int j = 1; j < 34; j++){
                proxyMap.put("http://www.66ip.cn/areaindex_" + j + "/" + i + ".html", IP66ListPageContentAnalysis.class);
            }
        }
        //proxyQueue.add(new NoProxy(TIME_INTERVAL));  //TIME_INTERVAL为单个ip请求间隔，单位ms
    }

}

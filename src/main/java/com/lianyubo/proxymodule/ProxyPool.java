package com.lianyubo.proxymodule;


import com.lianyubo.entity.Proxy;
import com.lianyubo.proxymodule.contentanalysis.multiProxies.IP66ListPageContentAnalysis;
import com.lianyubo.proxymodule.contentanalysis.multiProxies.XiciListPageContentAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.lianyubo.utils.Constants.TIME_INTERVAL;

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
     * 代理池延迟队列
     */
    public final static DelayQueue<Proxy> proxyQueue = new DelayQueue();
    public final static Map<String, Class> proxyMap = new HashMap<>();


    static {

        int pages = 8;  //这里只是处理8页的内容
        for(int i = 1; i <= pages; i++){
            proxyMap.put("http://www.xicidaili.com/wt/" + i + ".html", XiciListPageContentAnalysis.class);//国内HTTP代理
            proxyMap.put("http://www.xicidaili.com/nn/" + i + ".html", XiciListPageContentAnalysis.class);//国内普通代理
            proxyMap.put("http://www.xicidaili.com/wn/" + i + ".html", XiciListPageContentAnalysis.class);//国内HTTPS代理
            proxyMap.put("http://www.xicidaili.com/nt/" + i + ".html", XiciListPageContentAnalysis.class);//国内高匿

            proxyMap.put("http://www.xicidaili.com/daili/" + i + ".html", IP66ListPageContentAnalysis.class);


            proxyMap.put("http://www.66ip.cn/" + i + ".html", IP66ListPageContentAnalysis.class);

            //66ip代理网站提供34个省份的代理，每个省份一页数据
            for(int j = 1; j < 34; j++){
                proxyMap.put("http://www.66ip.cn/areaindex_" + j + "/" + i + ".html", IP66ListPageContentAnalysis.class);
            }
        }

        //proxyQueue.add(new NoProxy(TIME_INTERVAL));  //TIME_INTERVAL为单个ip请求间隔，单位ms
    }

}

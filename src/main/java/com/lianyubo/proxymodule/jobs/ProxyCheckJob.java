package com.lianyubo.proxymodule.jobs;


import com.lianyubo.entity.Page;
import com.lianyubo.entity.Proxy;
import com.lianyubo.proxymodule.contentanalysis.multiProxies.ProxyPool;
import com.lianyubo.utils.Constants;
import com.lianyubo.zhihumodule.ZhihuHttpClient;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 代理检测jobs
 * 通过访问知乎首页，能否正确响应
 * 将可用代理添加到DelayQueue延时队列中
 *
 * 多线程任务
 */
public class ProxyCheckJob implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ProxyCheckJob.class);
    private Proxy proxy;
    public ProxyCheckJob(Proxy proxy){
        this.proxy = proxy;
    }


    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        //httpclient
        HttpGet request = new HttpGet(Constants.INDEX_URL);//INDEX_URL为知乎首页的地址

        try {

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(Constants.TIMEOUT).
                    setConnectTimeout(Constants.TIMEOUT).
                    setConnectionRequestTimeout(Constants.TIMEOUT).
                    setProxy(new HttpHost(proxy.getIp(), proxy.getPort())).
                    setCookieSpec(CookieSpecs.STANDARD).
                    build();
            request.setConfig(requestConfig);
            Page page = ZhihuHttpClient.getInstance().getWebPage(request);


            long endTime = System.currentTimeMillis();


            String logStr = Thread.currentThread().getName() + " " + proxy.getProxyStr() +
                    "  executing request " + page.getUrl()  + " response statusCode:" + page.getStatusCode() +
                    "  request cost time:" + (endTime - startTime) + "ms";


            if (page == null || page.getStatusCode() != 200){
                logger.warn(logStr);
                return;
            }
            //关闭连接
            request.releaseConnection();
            //代理可用，输出相关日志，然后将代理加入待代理队列中
            logger.debug(proxy.toString() + "---------" + page.toString());
            logger.debug(proxy.toString() + "----------代理可用--------请求耗时:" + (endTime - startTime) + "ms");
            ProxyPool.proxyQueue.add(proxy);
        } catch (IOException e) {
            logger.debug("IOException:", e);
        } finally {
            if (request != null){
                request.releaseConnection();
            }
        }
    }
    private String getProxyStr(){
        return proxy.getIp() + ":" + proxy.getPort();
    }
}

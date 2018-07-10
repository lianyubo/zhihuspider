package com.lianyubo.proxymodule.jobs;


import com.lianyubo.proxymodule.ProxyListPageContentAnalysis;
import com.lianyubo.proxymodule.ProxyPool;
import com.lianyubo.proxymodule.contentanalysis.ListPageContentAnalysisFactory;
import com.lianyubo.proxymodule.entity.NoProxy;
import com.lianyubo.proxymodule.entity.Proxy;
import com.lianyubo.zhihumodule.entity.Page;
import com.lianyubo.zhihumodule.util.Config;
import com.lianyubo.zhihumodule.util.Constants;
import com.lianyubo.zhihumodule.util.HttpClientUtil;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * 下载代理网页并解析
 * 若下载失败，通过代理去下载代理网页
 *
 */
public class ProxyPageJob implements Runnable{
    private static Logger logger = LoggerFactory.getLogger(ProxyPageJob.class);

    protected String url;
    private boolean proxyFlag;//是否通过代理下载
    private Proxy currentProxy;//当前线程使用的代理

    //使用getInstance的方式创建对象，单例
    protected static ProxyHttpClient proxyHttpClient = ProxyHttpClient.getInstance();

    private ProxyPageJob(Proxy p){
    }

    public ProxyPageJob(String url, boolean proxyFlag){
        this.url = url;
        this.proxyFlag = proxyFlag;
    }


    @Override
    public void run(){

        long requestStartTime = System.currentTimeMillis();

        HttpGet tempRequest = null;
        try {
            Page page = null;

            if (proxyFlag){
                //使用代理，创建http请求
                tempRequest = new HttpGet(url);

                //通过queue的take方法取出队列头部的元素，这里是当前的代理
                currentProxy = ProxyPool.proxyQueue.take();

                if(!(currentProxy instanceof NoProxy)){

                    HttpHost proxy = new HttpHost(currentProxy.getIp(), currentProxy.getPort());
                    tempRequest.setConfig(HttpClientUtil.getRequestConfigBuilder().setProxy(proxy).build());
                }
                page = proxyHttpClient.getWebPage(tempRequest);

            }else {
                page = proxyHttpClient.getWebPage(url);
            }
            page.setProxy(currentProxy);
            int status = page.getStatusCode();
            long requestEndTime = System.currentTimeMillis();
            String logStr = Thread.currentThread().getName() + " " + getProxyStr(currentProxy) +
                    "  executing request " + page.getUrl()  + " response statusCode:" + status +
                    "  request cost time:" + (requestEndTime - requestStartTime) + "ms";
            if(status == HttpStatus.SC_OK){
                logger.debug(logStr);
                handle(page);
            } else {
                logger.error(logStr);
                Thread.sleep(100);
                retry();
            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            retry();
        } finally {
            if(currentProxy != null){
                currentProxy.setTimeInterval(Constants.TIME_INTERVAL);
                ProxyPool.proxyQueue.add(currentProxy);
            }
            if (tempRequest != null){
                tempRequest.releaseConnection();
            }
        }
    }

    /**
     * retry
     */
    public void retry(){
        proxyHttpClient.getProxyDownloadThreadExecutor().execute(new ProxyPageJob(url, Config.isProxy));
    }

    public void handle(Page page){
        if (page.getHtml() == null || page.getHtml().equals("")){
            return;
        }

        //使用工厂模式选择特定的代理解析
        ProxyListPageContentAnalysis contentAnalysis = ListPageContentAnalysisFactory.
                getProxyListPageContentAnalysis(ProxyPool.proxyMap.get(url));

        List<Proxy> proxyList = contentAnalysis.contentAnalysis(page.getHtml());
        for(Proxy p : proxyList){
            ProxyPool.lock.readLock().lock();
            boolean containFlag = ProxyPool.proxySet.contains(p);
            ProxyPool.lock.readLock().unlock();
            if (!containFlag){
                ProxyPool.lock.writeLock().lock();
                ProxyPool.proxySet.add(p);
                ProxyPool.lock.writeLock().unlock();

                proxyHttpClient.getProxyTestThreadExecutor().execute(new ProxyPageJob(p));
            }
        }
    }


    private String getProxyStr(Proxy proxy){
        if (proxy == null){
            return "";
        }
        return proxy.getIp() + ":" + proxy.getPort();
    }
}

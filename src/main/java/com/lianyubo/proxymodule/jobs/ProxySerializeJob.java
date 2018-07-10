package com.lianyubo.proxymodule.jobs;


import com.lianyubo.entity.Proxy;
import com.lianyubo.proxymodule.contentanalysis.multiProxies.ProxyPool;
import com.lianyubo.utils.Config;
import com.lianyubo.utils.HttpClientUtil;
import com.lianyubo.zhihumodule.ZhihuHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxySerializeJob implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(ProxySerializeJob.class);

    @Override
    public void run() {
        while (!ZhihuHttpClient.isStop){
            try {
                Thread.sleep(1000 * 60 * 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Proxy[] proxyArray = null;

            ProxyPool.lock.readLock().lock();
            try {
                proxyArray = new Proxy[ProxyPool.proxySet.size()];
                int i = 0;
                for (Proxy p : ProxyPool.proxySet){
                    if (!ProxyUtil.isDiscardProxy(p)){
                        proxyArray[i++] = p;
                    }
                }
            } finally {
                ProxyPool.lock.readLock().unlock();
            }

            HttpClientUtil.serializeObject(proxyArray, Config.proxyPath);
            logger.info("成功序列化" + proxyArray.length + "个代理");


        }
    }
}

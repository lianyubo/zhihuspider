package com.lianyubo.zhihumodule.jobs;


import com.lianyubo.zhihumodule.ZhihuHttpClient;
import com.lianyubo.zhihumodule.contentanalysis.ListPageContentAnalysis;
import com.lianyubo.zhihumodule.contentanalysis.implement.ZhihuUserListPageContentAnalysis;
import com.lianyubo.zhihumodule.dao.ConnectionManager;
import com.lianyubo.zhihumodule.entity.Page;
import com.lianyubo.zhihumodule.entity.User;
import com.lianyubo.zhihumodule.util.Config;
import com.lianyubo.zhihumodule.util.Md5Util;
import com.lianyubo.zhihumodule.util.SimpleInvocationHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.lianyubo.zhihumodule.ZhihuHttpClient.parseUserCount;
import static com.lianyubo.zhihumodule.util.Constants.USER_FOLLOWEES_URL;

/**
 * 知乎用户列表详情页task
 */
public class DetailListPageJob extends AbstractPageJob {
    private static Logger logger = LoggerFactory.getLogger(DetailListPageJob.class);
    private static ListPageContentAnalysis proxyUserListPageContentAnalysis;

    /**
     * Thread-数据库连接
     */
    private static Map<Thread, Connection> connectionMap = new ConcurrentHashMap<>();
    static {
        proxyUserListPageContentAnalysis = getproxyUserListPageContentAnalysis();
    }

    public DetailListPageJob(HttpRequestBase request, boolean proxyFlag) {
        super(request, proxyFlag);
    }

    /**
     * 代理类
     * @return
     */
    private static ListPageContentAnalysis getproxyUserListPageContentAnalysis(){
        ListPageContentAnalysis userListPageContentAnalysis = ZhihuUserListPageContentAnalysis.getInstance();
        InvocationHandler invocationHandler = new SimpleInvocationHandler(userListPageContentAnalysis);
        ListPageContentAnalysis proxyUserListPageParser = (ListPageContentAnalysis) Proxy.newProxyInstance(userListPageContentAnalysis.getClass().getClassLoader(),
                userListPageContentAnalysis.getClass().getInterfaces(), invocationHandler);
        return proxyUserListPageParser;
    }

    @Override
    protected void retry() {
        zhihuHttpClient.getDetailListPageThreadPool().execute(new DetailListPageJob(request, Config.isProxy));
    }

    @Override
    protected void handle(Page page) {
        if(!page.getHtml().startsWith("{\"paging\"")){
            //代理异常，未能正确返回目标请求数据，丢弃
            currentProxy = null;
            return;
        }
        List<User> list = proxyUserListPageContentAnalysis.contentAnalysisListPage(page);
        for(User u : list){
            logger.info("解析用户成功:" + u.toString());
            if(Config.dbEnable){
                Connection cn = getConnection();
                if (zhiHuDao1.insertUser(cn, u)){
                    parseUserCount.incrementAndGet();
                }
                for (int j = 0; j < u.getFollowees() / 20; j++){
                    if (zhihuHttpClient.getDetailListPageThreadPool().getQueue().size() > 1000){
                        continue;
                    }
                    String nextUrl = String.format(USER_FOLLOWEES_URL, u.getUserToken(), j * 20);
                    if (zhiHuDao1.insertUrl(cn, Md5Util.Convert2Md5(nextUrl)) ||
                            zhihuHttpClient.getDetailListPageThreadPool().getActiveCount() == 1){
                        //防止死锁
                        HttpGet request = new HttpGet(nextUrl);
                        request.setHeader("authorization", "oauth " + ZhihuHttpClient.getAuthorization());
                        zhihuHttpClient.getDetailListPageThreadPool().execute(new DetailListPageJob(request, true));
                    }
                }
            }
            else if(!Config.dbEnable || zhihuHttpClient.getDetailListPageThreadPool().getActiveCount() == 1){
                parseUserCount.incrementAndGet();
                for (int j = 0; j < u.getFollowees() / 20; j++){
                    String nextUrl = String.format(USER_FOLLOWEES_URL, u.getUserToken(), j * 20);
                    HttpGet request = new HttpGet(nextUrl);
                    request.setHeader("authorization", "oauth " + ZhihuHttpClient.getAuthorization());
                    zhihuHttpClient.getDetailListPageThreadPool().execute(new DetailListPageJob(request, true));
                }
            }
        }
    }

    /**
     * 每个thread维护一个Connection
     * @return
     */
    private Connection getConnection(){
        Thread currentThread = Thread.currentThread();
        Connection cn = null;
        if (!connectionMap.containsKey(currentThread)){
            cn = ConnectionManager.createConnection();
            connectionMap.put(currentThread, cn);
        }  else {
            cn = connectionMap.get(currentThread);
        }
        return cn;
    }

    public static Map<Thread, Connection> getConnectionMap() {
        return connectionMap;
    }

}

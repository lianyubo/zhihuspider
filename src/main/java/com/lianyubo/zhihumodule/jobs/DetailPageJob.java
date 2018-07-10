package com.lianyubo.zhihumodule.jobs;


import com.lianyubo.zhihumodule.ZhihuHttpClient;
import com.lianyubo.zhihumodule.contentanalysis.DetailPageContentAnalysis;
import com.lianyubo.zhihumodule.contentanalysis.implement.ZhihuUserDetailPageContentAnalysis;
import com.lianyubo.zhihumodule.entity.Page;
import com.lianyubo.zhihumodule.entity.User;
import com.lianyubo.zhihumodule.util.Config;
import com.lianyubo.zhihumodule.util.SimpleInvocationHandler;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static com.lianyubo.zhihumodule.ZhihuHttpClient.parseUserCount;

/**
 * 知乎用户详情页task
 * 下载成功解析出用户信息并添加到数据库，获取该用户的关注用户list url，添加到ListPageDownloadThreadPool
 */
public class DetailPageJob extends AbstractPageJob {
    private static Logger logger = LoggerFactory.getLogger(DetailPageJob.class);
    private static DetailPageContentAnalysis detailPageContentAnalysis;
    static {
        detailPageContentAnalysis = getdetailPageContentAnalysis();
    }

    public DetailPageJob(String url, boolean proxyFlag) {
        super(url, proxyFlag);
    }

    @Override
    protected void retry() {
        zhihuHttpClient.getDetailPageThreadPool().execute(new DetailPageJob(url, Config.isProxy));
    }

    @Override
    protected void handle(Page page) {
        DetailPageContentAnalysis detailPageContentAnalysis = null;
//        parser = ZhiHuNewUserDetailPageParser.getInstance();
        detailPageContentAnalysis = detailPageContentAnalysis;
        User u = detailPageContentAnalysis.contentAnalysisDetailPage(page);
        logger.info("解析用户成功:" + u.toString());
        if(Config.dbEnable){
//            ZhiHuDAO.insertUser(u);
            zhiHuDao1.insertUser(u);
        }
        parseUserCount.incrementAndGet();
        for(int i = 0;i < u.getFollowees() / 20 + 1;i++) {
            String userFolloweesUrl = formatUserFolloweesUrl(u.getUserToken(), 20 * i);
            handleUrl(userFolloweesUrl);
        }
    }
    public String formatUserFolloweesUrl(String userToken, int offset){
        String url = "https://www.zhihu.com/api/v4/members/" + userToken + "/followees?include=data%5B*%5D.answer_count%2Carticles_count%2Cfollower_count%2C" +
                "is_followed%2Cis_following%2Cbadge%5B%3F(type%3Dbest_answerer)%5D.topics&offset=" + offset + "&limit=20";
        return url;
    }
    private void handleUrl(String url){
        HttpGet request = new HttpGet(url);
        request.setHeader("authorization", "oauth " + ZhihuHttpClient.getAuthorization());
        if(!Config.dbEnable){
            zhihuHttpClient.getListPageThreadPool().execute(new ListPageJob(request, Config.isProxy));
            return ;
        }
        zhihuHttpClient.getListPageThreadPool().execute(new ListPageJob(request, Config.isProxy));
    }

    /**
     * 代理类
     * @return
     */
    private static DetailPageContentAnalysis getdetailPageContentAnalysis(){
        DetailPageContentAnalysis detailPageContentAnalysis = ZhihuUserDetailPageContentAnalysis.getInstance();
        InvocationHandler invocationHandler = new SimpleInvocationHandler(detailPageContentAnalysis);
        DetailPageContentAnalysis proxyDetailPageParser = (DetailPageContentAnalysis) Proxy.newProxyInstance(detailPageContentAnalysis.getClass().getClassLoader(),
                detailPageContentAnalysis.getClass().getInterfaces(), invocationHandler);
        return proxyDetailPageParser;
    }
}

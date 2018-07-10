package com.lianyubo.zhihumodule.jobs;


import com.jayway.jsonpath.JsonPath;
import com.lianyubo.zhihumodule.entity.Page;
import com.lianyubo.zhihumodule.util.Config;
import com.lianyubo.zhihumodule.util.Constants;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.List;

/**
 * 知乎用户关注列表页task
 * 下载成功解析出用户token，去重,构造用户详情url，获，添加到DetailPageDownloadThreadPool
 */
public class ListPageJob extends AbstractPageJob {

    public ListPageJob(HttpRequestBase request, boolean proxyFlag) {
        super(request, proxyFlag);
    }


    @Override
    protected void retry() {
        zhihuHttpClient.getListPageThreadPool().execute(new ListPageJob(request, Config.isProxy));
    }

    @Override
    protected void handle(Page page) {
        /**
         * "我关注的人"列表页
         */
        List<String> urlTokenList = JsonPath.parse(page.getHtml()).read("$.data..url_token");
        for (String s : urlTokenList){
            if (s == null){
                continue;
            }
            handleUserToken(s);
        }
    }
    private void handleUserToken(String userToken){
        String url = Constants.INDEX_URL + "/people/" + userToken + "/following";
        if(!Config.dbEnable){
            zhihuHttpClient.getDetailPageThreadPool().execute(new DetailPageJob(url, Config.isProxy));
            return ;
        }
//        boolean existUserFlag = ZhiHuDAO.isExistUser(userToken);
        boolean existUserFlag = zhiHuDao1.isExistUser(userToken);
        while (zhihuHttpClient.getDetailPageThreadPool().getQueue().size() > 1000){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!existUserFlag || zhihuHttpClient.getDetailPageThreadPool().getActiveCount() == 0){
            /**
             * 防止互相等待，导致死锁
             */
            zhihuHttpClient.getDetailPageThreadPool().execute(new DetailPageJob(url, Config.isProxy));

        }
    }
}

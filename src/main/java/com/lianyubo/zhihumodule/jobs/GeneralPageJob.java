package com.lianyubo.zhihumodule.jobs;


import com.lianyubo.entity.Page;

/**
 * GeneralPageTask
 * 下载初始化authorization字段页面
 */
public class GeneralPageJob extends AbstractPageJob {
    private Page page = null;

    public GeneralPageJob(String url, boolean proxyFlag) {
        super(url, proxyFlag);
    }

    @Override
    protected void retry() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.run();//继续下载
    }

    @Override
    protected void handle(Page page) {
        this.page = page;
    }

    public Page getPage(){
        return page;
    }
}

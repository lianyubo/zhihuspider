package com.lianyubo.proxymodule.contentanalysis.multiProxies;

import com.lianyubo.proxymodule.entity.Proxy;
import com.lianyubo.zhihumodule.contentanalysis.ContentAnalysis;

import java.util.List;

public interface ProxyListPageContentAnalysis extends ContentAnalysis {


    /**
     * IP代理网站的内容解析
     */

    //是否使用高匿名代理
    static final boolean anonymousFlag = true;

    //解析结果
    List<Proxy> contentAnalysis(String content);
}

package com.lianyubo.proxymodule;


import com.lianyubo.entity.Proxy;
import com.lianyubo.zhihumodule.contentanalysis.ContentAnalysis;

import java.util.List;

public interface ProxyListPageContentAnalysis extends ContentAnalysis {
    /**
     * 是否只要匿名代理
     */
    static final boolean anonymousFlag = true;
    List<Proxy> contentAnalysis(String content);
}

package com.lianyubo.entity;

public class Proxy {

    //任务时间间隔
    private long timeInterval;

    //ip
    private String ip;

    //端口号
    private int port;

    //是否可用
    private boolean availableFlag;

    //是否为匿名代理
    private boolean anonymousFlag;

    //最近一次请求成功的时间
    private long lastSuccessfulTime;

    //请求成功所耗费的时间
    private long successfulTotalTime;

    //请求失败的次数
    private int failureTimes;

    //请求成功的次数
    private int successfulTime;

    //成功请求平均耗费的时间
    private double successfulAverageTime;

    public Proxy(long timeInterval, String ip, int port) {
        this.timeInterval = timeInterval;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "timeInterval=" + timeInterval +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", availableFlag=" + availableFlag +
                ", anonymousFlag=" + anonymousFlag +
                ", lastSuccessfulTime=" + lastSuccessfulTime +
                ", successfulTotalTime=" + successfulTotalTime +
                ", failureTimes=" + failureTimes +
                ", successfulTime=" + successfulTime +
                ", successfulAverageTime=" + successfulAverageTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        Proxy proxy = (Proxy) o;
        if (port != proxy.port) {return false;}
        return ip.equals(proxy.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }




    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAvailableFlag() {
        return availableFlag;
    }

    public void setAvailableFlag(boolean availableFlag) {
        this.availableFlag = availableFlag;
    }

    public boolean isAnonymousFlag() {
        return anonymousFlag;
    }

    public void setAnonymousFlag(boolean anonymousFlag) {
        this.anonymousFlag = anonymousFlag;
    }

    public long getLastSuccessfulTime() {
        return lastSuccessfulTime;
    }

    public void setLastSuccessfulTime(long lastSuccessfulTime) {
        this.lastSuccessfulTime = lastSuccessfulTime;
    }

    public long getSuccessfulTotalTime() {
        return successfulTotalTime;
    }

    public void setSuccessfulTotalTime(long successfulTotalTime) {
        this.successfulTotalTime = successfulTotalTime;
    }

    public int getFailureTimes() {
        return failureTimes;
    }

    public void setFailureTimes(int failureTimes) {
        this.failureTimes = failureTimes;
    }

    public int getSuccessfulTime() {
        return successfulTime;
    }

    public void setSuccessfulTime(int successfulTime) {
        this.successfulTime = successfulTime;
    }

    public double getSuccessfulAverageTime() {
        return successfulAverageTime;
    }

    public void setSuccessfulAverageTime(double successfulAverageTime) {
        this.successfulAverageTime = successfulAverageTime;
    }

}

package com.csf.bean;

/**
 * Created by zhaozw on 2018/2/6.
 */
public class CsfClientClusterMapping {

    private  long cfgId ;
    private  String clientName;
    private  String centerCode;
    private  String clusterName;
    private  String state;
    private  String remark;
    private  String lastSyncInfo;
    private  String synchronize;

    public long getCfgId() {
        return cfgId;
    }

    public void setCfgId(long cfgId) {
        this.cfgId = cfgId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getCenterCode() {
        return centerCode;
    }

    public void setCenterCode(String centerCode) {
        this.centerCode = centerCode;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLastSyncInfo() {
        return lastSyncInfo;
    }

    public void setLastSyncInfo(String lastSyncInfo) {
        this.lastSyncInfo = lastSyncInfo;
    }

    public String getSynchronize() {
        return synchronize;
    }

    public void setSynchronize(String synchronize) {
        this.synchronize = synchronize;
    }
}

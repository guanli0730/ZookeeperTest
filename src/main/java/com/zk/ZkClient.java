package com.zk;

import com.csf.bean.CsfServices;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaozw on 2018/2/6.
 */
public class ZkClient {

    //1.获取zk地址  todo 后续通过配置文件实现
    public final static String zkServices = "10.10.152.204:12181";
    //2.获取中心 todo 后续通过入参获取中心
    public final static String zkCenter = "cust";
    //3.获取zk保存的根路径
    public final static String storePath = "/busidomain/csf/centers";
    public final static String leftsprit = "/";
    public final static String clientCluster = "?appcluster=";
    public final static String policies = "policies/clients";
    public final static String services = "services";
    private static CuratorFramework zkClient = null;

    public ZkClient() {
        if (null == zkClient) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            zkClient = CuratorFrameworkFactory.newClient(zkServices, retryPolicy);
        }
        System.out.println(zkClient.getState());
        if( null!= zkClient.getState() && CuratorFrameworkState.LATENT.equals(zkClient.getState()) ){
            zkClient.start();
        }
    }

    public void closeZk(){
        zkClient.close();
    }

    public String createZkNode(String nodeStr) {
        String result = null;
        try {
            ACLBackgroundPathAndBytesable handle = (ACLBackgroundPathAndBytesable) zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT);
            result = (String) handle.forPath(nodeStr);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public Object isExistsNode(String nodeStr){
        Object result = null;
        try{
            result = zkClient.checkExists().forPath(nodeStr);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public List<String> getAllChilds(String nodeStr){
        List<String> result = new ArrayList<String>();
        try{
            result = zkClient.getChildren().forPath(nodeStr);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public Object deleteNode(String nodeStr){
        Object result = null;
        try{
            result = zkClient.delete().forPath(nodeStr);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public String appendServices(CsfServices csfService){
        StringBuffer stringBuffer = new StringBuffer();
        //没有用到额外信息表
        stringBuffer.append(ZkClient.storePath).append(ZkClient.leftsprit)
                .append(ZkClient.zkCenter).append(ZkClient.leftsprit)
                .append(ZkClient.services).append(ZkClient.leftsprit)
                .append(csfService.getServiceCode()).append("?balance_policy=")
                .append("&SRV_INTERFACE=").append(csfService.getSrvInterface())
        .append("&SRV_IMPL_CLASS=").append(csfService.getSrvImplClass())
        .append("&SRV_METHOD=").append(csfService.getSrvMethod())
        .append("&SRV_RETURN=").append(csfService.getSrvReturn())
        .append("&CENTER_CODE=").append(csfService.getCenterCode())
        .append("&PROTOCOL=").append(csfService.getProtocol())
        .append("&frontend_timeout=&route_type=&circuit_breaker_config=")
        .append("&SIGNATURE=").append(csfService.getExtA());
        return stringBuffer.toString();
    }

}

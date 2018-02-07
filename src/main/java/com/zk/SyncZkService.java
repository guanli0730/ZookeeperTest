package com.zk;

import com.csf.bean.CsfClientClusterMapping;
import com.csf.bean.CsfServices;
import com.ibatis.IbatisClient;

import java.util.*;

/**
 * Created by zhaozw on 2018/1/25.
 */
public class SyncZkService {
    /*
     *更新zk,主要执行操作
     * 1.通过表 select * from csf.CSF_CLIENT_CLUSTER_MAPPING where state ='U'   更新/policies/clients下的客户端策略，最后会更新表LAST_SYNC_INFO
     * 2.通过表 select a.*, b.service_code mapping_code, b.path_mapping
                  from csf.csf_srv_zk_mapping b, csf.csf_srv_service_info a
                 where a.service_code = b.service_code(+)
                   and a.status = 'U'
        select * from csf.CSF_SRV_DEGRADE;
        select * from csf.CSF_SRV_SERVICE_EXTEND_INFO;  这2个表不需要使用
        更新service下的服务
     * 3.通知各个客户端
     */


    private static ZkClient zkClient = null;
    private static IbatisClient ibatisClient = null;


    public static void main(String[] args) throws Exception {
        zkClient = new ZkClient();
        ibatisClient = new IbatisClient();
        //1.同步客户端策略
        syncZkClientCluster();
        //2.同步服务
        syncZkServices();
        //zkClient.closeZk();
    }

    public static void syncZkServices() {
        Map param = new HashMap();
        param.put("centerCode", ZkClient.zkCenter);
        CsfServices[] csfServices = null;
        Set<String> dbSrevices = new HashSet<String>();
        int countNum = ibatisClient.queryCount("selectServicesCountByCenterCode", ZkClient.zkCenter);
        int pageCount = 0;
        String pathStr = null;
        if (countNum > 500) {
            pageCount = countNum / 500 + 1;
            for (int i = 0; i < pageCount; i++) {
                param.put("startNum", i * 500);
                param.put("endNum", (i + 1) * 500 + 1);
                csfServices = (CsfServices[]) ibatisClient.queryCsfServices("selectAllServicesByCenterCode", param);
                for (CsfServices csfService : csfServices) {
                    pathStr = zkClient.appendServices(csfService);
                    addZkNode(pathStr);
                    dbSrevices.add(pathStr);
                }
            }
        } else {
            param.put("startNum", 0);
            param.put("endNum", 501);
            csfServices = (CsfServices[]) ibatisClient.queryCsfServices("selectAllServicesByCenterCode", param);
            for (CsfServices csfService : csfServices) {
                pathStr = zkClient.appendServices(csfService);
                addZkNode(pathStr);
                dbSrevices.add(pathStr);
            }
        }
        StringBuffer stringBufferPre = new StringBuffer();
        stringBufferPre.append(ZkClient.storePath).append(ZkClient.leftsprit)
                .append(ZkClient.zkCenter).append(ZkClient.leftsprit)
                .append(ZkClient.services).toString();
        List<String> zkMappings = zkClient.getAllChilds(stringBufferPre.toString());
        String serviceString = stringBufferPre.append(ZkClient.leftsprit).toString();
        StringBuffer stringBuffer = new StringBuffer();

        for (String zkMapping : zkMappings) {
            pathStr = stringBuffer.delete(0, stringBuffer.length()).append(serviceString).append(zkMapping).toString();
            if (!dbSrevices.contains(pathStr) && null != zkClient.isExistsNode(pathStr)) {
                zkClient.deleteNode(pathStr);
                System.out.println("删除服务：" + pathStr);
            }
        }

    }


    private static void addZkNode(String pathStr) {
        if (null == zkClient.isExistsNode(pathStr)) {
            zkClient.createZkNode(pathStr);
            System.out.println("新增zk节点：" + pathStr);
        }
    }

    public static void syncZkClientCluster() {
        Set<String> dbMappingSet = new HashSet<String>();
        StringBuffer stringBufferPre = new StringBuffer();
        stringBufferPre.append(ZkClient.storePath).append(ZkClient.leftsprit)
                .append(ZkClient.zkCenter).append(ZkClient.leftsprit)
                .append(ZkClient.policies);
        List<String> zkMappings = zkClient.getAllChilds(stringBufferPre.toString());
        //1.数据库有的都注册到ZK中 selectAllClientMapping
        CsfClientClusterMapping[] dbMappings = (CsfClientClusterMapping[]) ibatisClient.queryCsfClientClusterMapping("selectAllClientMapping", ZkClient.zkCenter);
        StringBuffer stringBuffer = new StringBuffer();
        String policiesStrigng = stringBufferPre.append(ZkClient.leftsprit).toString();
        String pathStr = null;
        for (CsfClientClusterMapping dbMapping : dbMappings) {
            //例如： /busidomain/csf/centers/cust/policies/clients/csf_client_order_app_g1?appcluster=csf_cluster_cust_app_g1
            stringBuffer.delete(0, stringBuffer.length());
            stringBuffer.append(policiesStrigng).append(dbMapping.getClientName()).append(ZkClient.clientCluster).append(dbMapping.getClusterName());
            pathStr = stringBuffer.toString();
            if (null == zkClient.isExistsNode(pathStr)) {
                zkClient.createZkNode(pathStr);
                System.out.println("新增政策：" + pathStr);
            }
            dbMappingSet.add(pathStr);
        }
        //2.数据库中没有的，zk中有的，则删除
        for (String zkMapping : zkMappings) {
            stringBuffer.delete(0, stringBuffer.length());
            stringBuffer.append(ZkClient.storePath).append(ZkClient.leftsprit)
                    .append(ZkClient.zkCenter).append(ZkClient.leftsprit)
                    .append(ZkClient.policies).append(ZkClient.leftsprit)
                    .append(zkMapping);
            pathStr = stringBuffer.toString();
            if (!dbMappingSet.contains(pathStr) && null != zkClient.isExistsNode(pathStr)) {
                zkClient.deleteNode(pathStr);
                System.out.println("删除政策：" + pathStr);
            }
        }
    }


}

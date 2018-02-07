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
     *����zk,��Ҫִ�в���
     * 1.ͨ���� select * from csf.CSF_CLIENT_CLUSTER_MAPPING where state ='U'   ����/policies/clients�µĿͻ��˲��ԣ�������±�LAST_SYNC_INFO
     * 2.ͨ���� select a.*, b.service_code mapping_code, b.path_mapping
                  from csf.csf_srv_zk_mapping b, csf.csf_srv_service_info a
                 where a.service_code = b.service_code(+)
                   and a.status = 'U'
        select * from csf.CSF_SRV_DEGRADE;
        select * from csf.CSF_SRV_SERVICE_EXTEND_INFO;  ��2������Ҫʹ��
        ����service�µķ���
     * 3.֪ͨ�����ͻ���
     */


    private static ZkClient zkClient = null;
    private static IbatisClient ibatisClient = null;


    public static void main(String[] args) throws Exception {
        zkClient = new ZkClient();
        ibatisClient = new IbatisClient();
        //1.ͬ���ͻ��˲���
        syncZkClientCluster();
        //2.ͬ������
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
                System.out.println("ɾ������" + pathStr);
            }
        }

    }


    private static void addZkNode(String pathStr) {
        if (null == zkClient.isExistsNode(pathStr)) {
            zkClient.createZkNode(pathStr);
            System.out.println("����zk�ڵ㣺" + pathStr);
        }
    }

    public static void syncZkClientCluster() {
        Set<String> dbMappingSet = new HashSet<String>();
        StringBuffer stringBufferPre = new StringBuffer();
        stringBufferPre.append(ZkClient.storePath).append(ZkClient.leftsprit)
                .append(ZkClient.zkCenter).append(ZkClient.leftsprit)
                .append(ZkClient.policies);
        List<String> zkMappings = zkClient.getAllChilds(stringBufferPre.toString());
        //1.���ݿ��еĶ�ע�ᵽZK�� selectAllClientMapping
        CsfClientClusterMapping[] dbMappings = (CsfClientClusterMapping[]) ibatisClient.queryCsfClientClusterMapping("selectAllClientMapping", ZkClient.zkCenter);
        StringBuffer stringBuffer = new StringBuffer();
        String policiesStrigng = stringBufferPre.append(ZkClient.leftsprit).toString();
        String pathStr = null;
        for (CsfClientClusterMapping dbMapping : dbMappings) {
            //���磺 /busidomain/csf/centers/cust/policies/clients/csf_client_order_app_g1?appcluster=csf_cluster_cust_app_g1
            stringBuffer.delete(0, stringBuffer.length());
            stringBuffer.append(policiesStrigng).append(dbMapping.getClientName()).append(ZkClient.clientCluster).append(dbMapping.getClusterName());
            pathStr = stringBuffer.toString();
            if (null == zkClient.isExistsNode(pathStr)) {
                zkClient.createZkNode(pathStr);
                System.out.println("�������ߣ�" + pathStr);
            }
            dbMappingSet.add(pathStr);
        }
        //2.���ݿ���û�еģ�zk���еģ���ɾ��
        for (String zkMapping : zkMappings) {
            stringBuffer.delete(0, stringBuffer.length());
            stringBuffer.append(ZkClient.storePath).append(ZkClient.leftsprit)
                    .append(ZkClient.zkCenter).append(ZkClient.leftsprit)
                    .append(ZkClient.policies).append(ZkClient.leftsprit)
                    .append(zkMapping);
            pathStr = stringBuffer.toString();
            if (!dbMappingSet.contains(pathStr) && null != zkClient.isExistsNode(pathStr)) {
                zkClient.deleteNode(pathStr);
                System.out.println("ɾ�����ߣ�" + pathStr);
            }
        }
    }


}

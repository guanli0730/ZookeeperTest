package com.ibatis;

import com.csf.bean.CsfClientClusterMapping;
import com.csf.bean.CsfServices;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaozw on 2018/2/6.
 */
public class IbatisClient {

    private static SqlMapClient sqlMapClient = null;
    private final static String ibatisXml = "SqlMapConfig.xml";

    public IbatisClient(){
        try {
            if (null == sqlMapClient) {
                Reader reader = Resources.getResourceAsReader(ibatisXml);
                sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
            }
        } catch (Exception e) {
            System.out.println("³õÊ¼»¯IbatisClient³ö´í£º"+e.getMessage());
        }
    }

    public  CsfClientClusterMapping[] queryCsfClientClusterMapping(String functioinStr,String param) {
        CsfClientClusterMapping[] results = null;
        try {
            List<CsfClientClusterMapping> objList = (List<CsfClientClusterMapping>) sqlMapClient.queryForList(functioinStr, param);
            results = objList.toArray(new CsfClientClusterMapping[objList.size()]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return results;
    }

    public CsfServices[] queryCsfServices(String functioinStr, Map param) {
        CsfServices[] results = null;
        try {
            List<CsfServices> objList = (List<CsfServices>) sqlMapClient.queryForList(functioinStr, param);
            results = objList.toArray(new CsfServices[objList.size()]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return results;
    }

    public int queryCount(String functionStr,String param){
        int countNum = 0;
        try{
            countNum = (Integer) sqlMapClient.queryForObject(functionStr,param);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return countNum;
    }


}

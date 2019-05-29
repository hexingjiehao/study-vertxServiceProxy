package com.xiongjie.kafka;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.security.JaasUtils;

import java.util.*;

public class TraditionalKafka {

    public static void main(String[] args) {
        createTopic2();

        deleteTopic2();

        createTopic2();
        searchTopic2();

        updateTopic2();
        searchTopic2();

        existTopic2();
    }

    //传统方法创建kafka的topic--有效
    public static void createTopic2() {
        ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        AdminUtils.createTopic(zkUtils, "mytopic", 1, 1, new Properties(), RackAwareMode.Enforced$.MODULE$);
        zkUtils.close();
    }

    //传统方法删除kafka的topic--有效
    public static void deleteTopic2() {
        ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        AdminUtils.deleteTopic(zkUtils, "mytopic");
        zkUtils.close();
    }

    //传统方法查询kafka的某个主题的topic属性--有效
    public static void searchTopic2() {
        ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), "mytopic");
        Iterator it = props.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry=(Map.Entry)it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(key + " = " + value);
        }
        zkUtils.close();
    }

    //传统方法修改kafka的topic--有效
    public static void updateTopic2() {
        ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), "mytopic");
        // 增加topic级别属性
        props.put("min.cleanable.dirty.ratio", "0.3");
        // 删除topic级别属性
        props.remove("max.message.bytes");
        // 修改topic 'test'的属性
        AdminUtils.changeTopicConfig(zkUtils, "mytopic", props);
        zkUtils.close();
    }

    //传统方法修改kafka的topic--有效
    public static void existTopic2() {
        ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        if(AdminUtils.topicExists(zkUtils,"mytopic")){
            System.out.println("主题存在");
        }else{
            System.out.println("主题不存在");
        }
        zkUtils.close();
    }

}

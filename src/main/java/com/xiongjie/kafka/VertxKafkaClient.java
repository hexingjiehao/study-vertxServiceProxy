package com.xiongjie.kafka;

import io.vertx.core.Vertx;
import io.vertx.kafka.admin.AdminUtils;
import io.vertx.kafka.client.common.PartitionInfo;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.Map;

public class VertxKafkaClient {

    public static void main(String[] args) {
//        createTopic();
//        deleteTopic();
//        changeTopic();
//        existTopic();
//        sendMessage();
//        partitionInf();
//        receiveMessage();
        demo();
    }

//---------------------topic------------------------

    //创建topic，注意导入jar包的版本和种类--异步操作
    public static void createTopic() {
        Vertx vertx = Vertx.vertx();
        AdminUtils adminUtils = AdminUtils.create(vertx, "localhost:2181", true);
        adminUtils.createTopic("vertxtopic", 2, 1, result -> {
            if (result.succeeded()) {
                System.out.println("创建topic成功");
            } else {
                System.out.println("创建topic失败： " + result.cause().getLocalizedMessage());
            }
            vertx.close();
        });
    }

    //异步操作
    public static void deleteTopic() {
        Vertx vertx = Vertx.vertx();
        AdminUtils adminUtils = AdminUtils.create(vertx, "localhost:2181", true);
        adminUtils.deleteTopic("vertxtopic", result -> {
            if (result.succeeded()) {
                System.out.println("删除topic成功");
            } else {
                System.out.println("删除topic失败： " + result.cause().getLocalizedMessage());
            }
            vertx.close();
        });
    }

    public static void changeTopic() {
        Vertx vertx = Vertx.vertx();
        AdminUtils adminUtils = AdminUtils.create(vertx, "localhost:2181", true);

        Map<String, String> properties = new HashMap<>();
        properties.put("delete.retention.ms", "1000");
        properties.put("retention.bytes", "1024");

        adminUtils.changeTopicConfig("vertxtopic", properties, result -> {
            if (result.succeeded()) {
                System.out.println("修改topic成功");
            } else {
                System.out.println("修改topic失败： " + result.cause().getLocalizedMessage());
            }
            vertx.close();
        });
    }

    public static void existTopic() {
        Vertx vertx = Vertx.vertx();
        AdminUtils adminUtils = AdminUtils.create(vertx, "localhost:2181", true);
        adminUtils.topicExists("vertxtopic", result -> {
            if (result.succeeded()) {
                System.out.println("topic存在与否？:"+result.result());
            } else {
                System.out.println("查找topic失败： " + result.cause().getLocalizedMessage());
            }
            vertx.close();
        });
    }

//---------------------producer------------------------

    public static void sendMessage() {
        Vertx vertx = Vertx.vertx();
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "1");

        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config);
        KafkaProducerRecord<String, String> record = KafkaProducerRecord.create("vertxtopic", "1111","hello,world");

        producer.write(record,ar ->{
            if(ar.succeeded()){
                System.out.println("消息发送成功："+ar.result());
            }else{
                System.out.println("消息发送失败:"+ar.cause().getLocalizedMessage());
            }
            vertx.close();
        });
    }

    public static void partitionInf() {
        Vertx vertx = Vertx.vertx();
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "1");

        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config);
        producer.partitionsFor("vertxtopic", ar -> {
            if (ar.succeeded()) {
                for (PartitionInfo partitionInfo : ar.result()) {
                    System.out.println(partitionInfo);
                }
            }
        });
    }

//---------------------consumer------------------------

    public static void receiveMessage() {
        Vertx vertx = Vertx.vertx();
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "my_group");
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");

        KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);
        consumer.subscribe("vertxtopic")
                .handler(ar ->{
                    System.out.println("接收到消息:"+ar.key()+":"+ar.value());
                    vertx.close();
                    consumer.close();   //不关闭则一直监听
                });
    }

    public static void demo() {
        Vertx vertx = Vertx.vertx();
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "my_group");
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");

        KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);
        consumer.subscribe("vertxtopic")
                .handler(ar ->{
                    System.out.println("接收到消息:"+ar.key()+":"+ar.value());
                });


        Map<String, String> pconfig = new HashMap<>();
        pconfig.put("bootstrap.servers", "localhost:9092");
        pconfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pconfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pconfig.put("acks", "1");

        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, pconfig);

        for(int i=0;i<10;i++) {
            KafkaProducerRecord<String, String> record = KafkaProducerRecord.create("vertxtopic", "1111-"+i, "hello,world-"+ i);
            producer.write(record);
        }
        producer.close();
    }
}

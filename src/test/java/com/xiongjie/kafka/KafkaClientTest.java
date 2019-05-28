package com.xiongjie.kafka;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(VertxUnitRunner.class)
public class KafkaClientTest {

    private Vertx vertx;

    @Test
    public void kafkaClientTest(TestContext context) {
        vertx =Vertx.vertx();
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "my_group");
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");

        KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);
        consumer.handler(record ->{
            System.out.println(record.headers());
            System.out.println(record.key());
            System.out.println(record.offset());
            System.out.println(record.partition());
            System.out.println(record.record());
            System.out.println(record.timestampType());
            System.out.println(record.topic());
            System.out.println(record.value());
        });

        consumer.subscribe("a-single-topic",ar ->{
            if(ar.succeeded()){
                System.out.println("消息接收成功！");
            }else{
                System.out.println("消息接收失败！");
            }
        });

        Map<String, String> pconfig = new HashMap<>();
        pconfig.put("bootstrap.servers", "localhost:9092");
        pconfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pconfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pconfig.put("acks", "1");;

        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, pconfig);;
        for (int i = 0; i < 5; i++) {
            KafkaProducerRecord<String, String> record = KafkaProducerRecord.create("a-single-topic", "发送消息：" + i);
            producer.write(record,ar ->{
                System.out.println("ok");
            });
        }
    }

}

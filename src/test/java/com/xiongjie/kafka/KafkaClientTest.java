package com.xiongjie.kafka;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(VertxUnitRunner.class)
public class KafkaClientTest {

    private Vertx vertx;

    @Test
    public void kafkaClientTest(TestContext context) {
        Async async = context.async();

        Vertx vertx = Vertx.vertx();

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaAdminClient kafkaAdminClient = KafkaAdminClient.create(vertx, properties);
        List<NewTopic> topicList=new ArrayList<>();
        topicList.add(new NewTopic("mytopic",0, (short) 1));

        kafkaAdminClient.createTopics(topicList, ar -> {
            if (ar.succeeded()) {
                System.out.println("topic创建成功");
            } else {
                System.out.println("topic创建失败");
            }
            vertx.close();
            async.complete();
        });
    }
}

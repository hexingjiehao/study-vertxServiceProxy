package com.xiongjie.authCommon;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RunWith(VertxUnitRunner.class)
public class VertxAuthCommonTest {

    private Vertx vertx;

    @Before
    public void init(TestContext context) {
        vertx = Vertx.vertx();
        new AuthCommonUtil().initRouteAndAuth(vertx);
    }

    @After
    public void destroy(TestContext context) {
        vertx.close();
    }

    /**
     * yaml文件中主要是针对URL进行参数和结果的限制
     * @param context
     */
    @Test
    public void authoCommonTest(TestContext context) {
        JsonObject resources=new JsonObject()
                .put("material", new JsonObject()
                .put("roles", new JsonArray()
                        .add("read")));

        JsonObject pwdJson=new JsonObject()
                .put("resource_access",resources);
        String token=String.format("header.%s.signature", Base64.getUrlEncoder().encodeToString(pwdJson.encode().getBytes(StandardCharsets.UTF_8)));

        Async async = context.async();
        Vertx vertx=Vertx.vertx();
        WebClient client = WebClient.create(vertx);
        client.get(8879,"localhost","/hello")
                .putHeader("Authorization","Bearer " + token)
                .send(ar ->{
                    if(!ar.succeeded()){
                        System.out.println("失败");
                    }else{
                        System.out.println(ar.result().body());
                    }
            async.complete();
        });
    }
}

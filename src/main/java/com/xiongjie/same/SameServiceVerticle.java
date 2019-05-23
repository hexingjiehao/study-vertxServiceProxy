package com.xiongjie.same;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

import java.util.Random;

public class SameServiceVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.get("/").handler(routingContext -> {
            int random= new Random().nextInt(10);
            System.out.println(this);
            if(random<5) {
                routingContext.response().end("随机调用服务1：value="+random);
            }else {
                routingContext.response().end("随机调用服务2：value="+random);
            }
        });
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8881);
    }
}

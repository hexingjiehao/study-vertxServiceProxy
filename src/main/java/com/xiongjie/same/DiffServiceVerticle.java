package com.xiongjie.same;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

public class DiffServiceVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.get("/").handler(routingContext -> {
            routingContext.response().end("你好");
        });
        router.get("/hello").handler(routingContext -> {
            routingContext.response().end("大家好");
        });
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8880);
    }
}

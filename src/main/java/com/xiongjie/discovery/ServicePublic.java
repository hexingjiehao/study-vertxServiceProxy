package com.xiongjie.discovery;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

public class ServicePublic extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.get("/").handler(ar ->{
            ar.response().end("hello,world");
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8883);
    }
}

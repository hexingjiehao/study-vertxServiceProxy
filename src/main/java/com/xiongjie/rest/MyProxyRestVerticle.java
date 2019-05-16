package com.xiongjie.rest;

import com.xiongjie.eb.MyProxyService;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MyProxyRestVerticle extends AbstractVerticle {

    private MyProxyService myProxyService;

    @Override
    public void start() throws Exception {
        myProxyService = MyProxyService.createProxy(vertx);

        Router router = Router.router(vertx);
        router.get("/").handler(this::indexHandler);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8884);
    }

    private void indexHandler(RoutingContext routingContext) {
        myProxyService.sayHello(ar -> {
            routingContext.response().end(ar.result());
        });
    }

}

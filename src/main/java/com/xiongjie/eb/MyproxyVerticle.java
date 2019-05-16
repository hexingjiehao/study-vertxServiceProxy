package com.xiongjie.eb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ServiceBinder;

public class MyproxyVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        new ServiceBinder(vertx).setAddress("helloProxy").register(MyProxyService.class,new MyProxyServiceImpl());
        startFuture.complete();
    }
}
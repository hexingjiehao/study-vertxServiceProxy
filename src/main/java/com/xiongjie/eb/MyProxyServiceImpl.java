package com.xiongjie.eb;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class MyProxyServiceImpl implements MyProxyService{

    @Override
    public void sayHello(Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture("helloProxy 实现类调用"));
    }
}
